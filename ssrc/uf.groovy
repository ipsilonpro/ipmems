ipmems_db_sync = {t, r ->
	def port = t.get("port", 23333);
	def host = t.get("url", "localhost");
	def props = t.get("props", [host: host, port: port]);
	def client = IpmemsDbfiClient.newInstance(props);
	def cns = client.connect();
	if (cns) {
		info("db", "{0} Connected to {1}", t, client.toString());
	} else return;
	def dbnames = dbServer.databaseNames.toArray();
	def ldbname = t.get("ldbname", dbnames[0]);
	def cm = [:];
	if (t.containsKey("cmap")) cm = t["cmap"]; else {
		def rdbname = t.get("rdbname", ldbname);
		def dbport = t.get("dbport", dbServer.port);
		def drvname = t.get("drvname", dbServer.driverName);
		def prot = t.get("dbprot", dbServer.protocol);
		def dburl = "jdbc:$drvname:$prot://localhost:$dbport/$rdbname";
		def user = t.get("user", "ipmems");
		def password = t.eval("password", "");
		cm = [url: dburl, user: user, password: password];
	}
	def dbprops = t.get("dbprops", [user: "SA", password: ""]);
	dbprops.each({k, v -> cm["db." + k] = v});
	try {
		client.sendClientMap(cm);
	} catch (x) {
		warning("db", "{0} Sync error", x, t);
		return null;
	}
	def lds = dbServer.gate("dataStore", ldbname);
	def table = t.get("table", "NUM");
	def lprefix = t.get("prefix", "CURRENT");
	def rprefix = t.get("rprefix", lprefix);
	def idsm = lds.ids(table: table, prefix: lprefix, dyn: t.get("dyn", true));
	if (!idsm.containsKey("ids")) {
		warning("db", "{0} Fetching ids error {1}", t.name, idsm);
		client.close();
		return;
	}
	fine("db", "{0} Ids {1}", t.name, idsm.ids);
	def lvm = client.query(target: "dataStore", method: "last", arg: [
		table: table, prefix: rprefix, dyn: t.get("rdyn", true), ids: idsm.ids
	]);
	if (!lvm.containsKey("result")) {
		warning("db", "{0} Fetching after error {1}", t.name, lvm);
		client.close();
		return;
	}
	fine("db", "{0} Fetch after {1}", t.name, lvm.result);
	def max = t.get("max", Byte.MAX_VALUE);
	def rs = [];
	client.disconnect();
	client.close();
	idsm.ids.each{if (!lvm.result.containsKey(it)) lvm.result.put(it, 0L);}
	lvm.result.each({id, tv ->
		def avm = lds.after(
			table: table, prefix: lprefix, id: id, t: tv, max: max);
		if (!avm.containsKey("rows")) {
			warning("db", "{0} Empty {1} {2}", t.name, id, avm);
			return;
		}
		finest("db", "{0} Preparing {1}:{2} {3}", t.name, id, tv, avm.rows);
		rs.addAll(avm["rows"].collect{[it.id, it.t, it.val]});
	});
	lvm.clear();
	try {
		if (!client.connect()) {
			warning("db", "{0} Not connected", t);
			return;
		} else {
			info("db", "{0} Connected to {1}", t, client);
		}
		client.sendClientMap(cm);
	} catch (x) {
		warning("db", "{0} Sync error", x, t);
		return;
	}
	fine("db", "{0} Fetched {1}", t, rs.size());
	def sr = client.query(target: "dataStore", method: "store", arg: [
		rows: rs,
		prefix: rprefix,
		table: table,
		dyn: t.get("rdyn", true),
		check: t.get("rce", false)
	]);
	rs.clear();
	if (!sr.containsKey("updateCount")) {
		warning("db", "{0} Data store error {1} {2}", t.name, id, sr);
	} else {
		info("db", "{0} Updated {1}", t.name, sr.updateCount);
	}	
	client.disconnect();
	client.close();
	return [:];
};

ipmems_db_write = {t, m ->
	if (m == Void) return m;
	if (m == null) {
		warning("usr", "{0} No data", t.name);
		return m;
	}
	if (m.containsKey("err") && m["err"] != Void) {
		warning("usr", "{0} {1}", t.name, m["err"]);
		return m;
	}
	def data = [];
	if (t.containsKey("tags")) {
		def ts = m.containsKey("time") ? m.time :
			t.containsKey("time") ? t.eval("time") : new Date();
		t.get("tags").each{name, id ->
			if (m[name] == null) warning("usr", "{0} Empty tag {1}", t, name);
			else if (m[name] != Void) {
				if (id instanceof List) id.eachWithIndex{q, i ->
					def v = m[name][i];
					if (v != Void) data << [q, ts, v];
				}; else data << [id, ts, m[name]]; 
			}
		};
	} else if (t.containsKey("vtags")) {
		t.get("vtags").each{name ->
			if (m[name] == null) warning("usr", "{0} Empty tag {1}", t, name);
			else if (m[name] != Void) data.addAll(m[name]);
		};
	} else return m;
	def ds = dbServer.gate("dataStore", t.get("db", "db"));
	def u = ds.store(
		prefix: t.get("prefix", "CURRENT"),
		check: t.get("ce", false),
		table: t.get("table", "NUM"),
		dyn: t.get("dyn", true),
		rows: data
	);
	info("usr", "{0} {1} {2}", t.name, u, data);
	return m;
}