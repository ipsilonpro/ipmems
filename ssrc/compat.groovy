import groovy.sql.Sql;
import org.ipsilon.ipmems.db.IpmemsDbSqlServer;

calendarAddDay = {inc, t -> new Date().clearTime() + inc};
calendarAddMonth = {inc, t ->
	def c = Calendar.getInstance().clearTime();
	c[c.DATE] = 1;
	c.add(c.MONTH, inc);
	return c.time;
};
calendarAddYear = {inc, t ->
	def c = Calendar.getInstance().clearTime();
	c[c.DATE] = 1;
	c[c.MONTH] = 0;
	c.add(c.YEAR, inc);
	return c.time;
};

ipmems_tcp_init = {q, r ->
	if (q.containsKey("io")) {
		def io = q.get("io");
		if (q.get("regen", true) && !io.active) {
			info("usr", "{0} Regen I/O", q.name)
			q.remove("io");
			io.close();
		}
	}
	if (!q.containsKey("io")) {
		def props = [:];
		if (q.containsKey("url")) props.put("host", q.url);
		if (q.containsKey("port")) props.put("port", q.port);
		if (q.containsKey("socketProps")) props.put("port", q.socketProps);
		def io = IpmemsSocketIO.newInstance(props);
		info("usr", "{0} Connecting to {1}", q.name, io);
		io.connect();
		q.put("io", io);
		info("usr", "{0} Connected to {1}", q.name, io);
	}
	return r;
};

rxtx_init = {q, r ->
	if (q.containsKey("io")) {
		def io = q.get("io");
		if (q.get("regen", true) && !io.active) {
			info("usr", "{0} Regen I/O", q.name)
			q.remove("io");
			io.close();
		}
	}
	if (!q.containsKey("io")) {
		def props = [:];
		["port", "baud", "dataBits", "stopBits", "fc", "parity"].each{
			if (q.containsKey(it)) props.put(it, q[it]);
		}
		def io = IpmemsRxTxIO.newInstance(props);
		info("usr", "{0} Connecting to {1}", q.name, io);
		io.connect();
		q.put("io", io);
		info("usr", "{0} Connected to {1}", q.name, io);
	}
	return r;
};

if (dbServer instanceof IpmemsDbSqlServer) {
	for (def k in dbServer.databaseNames)
		userMap.put(k + "Sql", new Sql(dbServer.gateObject(k)));
}

for (def f in dbServer.facilitySet)
	IpmemsDbAbstractServer.metaClass."get${f.capitalize()}" = {fcl, db ->
		return gate(fcl, db);
	}.curry(f);
