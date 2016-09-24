import org.ipsilon.ipmems.*;
import org.ipsilon.ipmems.dbfi.*;

public class DbSyncLogger implements IpmemsObserver {
	public DbSyncLogger(def s, def d) {
		server = s;
		db = d;
	}
	
	@Override
	public void event(Object src, Object ... o) {
		if (src instanceof Ipmems) {
			if (o[0] instanceof IpmemsDbfiServer) o[0].addObserver(this);
		} else if (src instanceof IpmemsDbfiServer) {
			if (!(o[0] in ["start", "stop"])) return;
			def dblog = server.gate("logging", db);
			if (dblog == null) return;
			def m = o[2];
			def a = o[1].inetAddress;
			if (o[0] == "start") {
				if (m.containsKey("user")) {
					dblog.put(rows: [[aid: a, msg: "AUTH_IN {0}", args: [m.user]]]);
				} else if (m.containsKey("conn")) {
					dblog.put(rows: [[aid: a, msg: "CONN_IN {0}", args: [m.conn]]]);
				} else if (m.method == "last") {
					dblog.put(rows: [[aid: a, msg: "LAST_IN {0}", args: [m]]]);
				} else if (m.method == "store") {
					dblog.put(rows: [[aid: a, msg: "STOR_IN {0}", args: [m.arg.rows.size()]]]);
					finest("db", "{0} STORE {1}", a, m);
				}
			} else if (o[0] == "stop") {
				if (m.containsKey("auth")) {
					dblog.put(rows: [[aid: a, msg: "AUTH_OUT {0}", args: [m.auth]]]);
				} else if (m.containsKey("conn")) {
					dblog.put(rows: [[aid: a, msg: "CONN_OUT {0}", args: [m.result]]]);
				} else if (m.containsKey("rows")) {
					dblog.put(rows: [[aid: a, msg: "ROWS_OUT {0}", args: [m.rows.size()]]]);
				} else if (m.containsKey("updateCount")) {
					dblog.put(rows: [[aid: a, msg: "UPDT_OUT {0}", args: [m.updateCount]]]);
					finest("db", "{0} UPDATE {1}", a, m);
				}
			}				
		}
	}

	public static synchronized def start(server, db) {
		if (observer == null) 
			Ipmems.MONITOR.addObserver(observer = new DbSyncLogger(server, db));
	}
	
	public static synchronized def stop() {
		if (observer != null) {
			if (IpmemsLib.userMap.containsKey("dbfiServer"))
				IpmemsLib.userMap.dbfiServer.removeObserver(observer);
			Ipmems.MONITOR.removeObserver(observer);
			observer = null;
		}
	}
	
	private final def server;
	private final def db;
	
	private static def observer;
}