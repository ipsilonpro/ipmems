package org.ipsilon.ipmems.hsqlsrv;

/*
 * IPMEMS, the universal cross-platform data acquisition software.
 * Copyright (C) 2011, 2012 ipsilon-pro LLC.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.util.*;
import static java.util.Collections.EMPTY_MAP;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.jdbc.JDBCDriver;
import org.hsqldb.server.Server;
import static org.hsqldb.server.ServerConstants.SERVER_STATE_ONLINE;
import static org.hsqldb.server.ServerConstants.SERVER_STATE_SHUTDOWN;
import org.hsqldb.server.WebServer;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.db.IpmemsDbSqlAbstractServer;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.password.IpmemsPasswordInput;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsCollections;

/**
 * IPMEMS HSQLDB server wrapper class.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsHsqlServer extends 
		IpmemsDbSqlAbstractServer implements Runnable {
	@Override
	public void init(Object... args) {
		super.init(args);
		setFacility("dataStore", IpmemsHsqlDataStore.class);
		setFacility("mapStore", IpmemsHsqlMapStore.class);
		setFacility("logging", IpmemsHsqlLogging.class);
		for (String k: get(String.class, "dbs", "db,arch").split(","))
			putGateObject(k, null);
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public void run() {
		server.shutdown();
		try {
			while (server.getState() != SERVER_STATE_SHUTDOWN)
				Thread.sleep(1000L);
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Shutdown interrupted", x);
		}
		IpmemsLoggers.info(getLogName(), "Shutdown process finished");
	}
	
	/**
	 * Get the base directory.
	 * @return Base directory.
	 */
	public File getDir() {
		return new File(substituted("dir", "@{dataDirectory}"));
	}
		
	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public void start() {
		if (server != null && isRunning()) return;
		Map dbm;
		try {
			dbm = (Map)get(Map.class, "conf", EMPTY_MAP);
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "DB configuration error", x);
			dbm = EMPTY_MAP;
		}
		try {
			String log = substituted("logFile", "@{jarDir}/db.log");
			FileOutputStream fos = new FileOutputStream(log);
			printStream = new PrintStream(fos, true, "UTF-8");
			printWriter = new PrintWriter(
					new OutputStreamWriter(fos, "UTF-8"), true);
			server = get(String.class, "protocol", "hsql").startsWith("http") ?
					new WebServer() : new Server();
			server.setErrWriter(printWriter);
			server.setLogWriter(printWriter);
			server.setTrace(get(Boolean.class, "trace", false));
			server.setSilent(get(Boolean.class, "silent", true));
			server.setTls(get(Boolean.class, "secure", false));
			server.setDaemon(get(Boolean.class, "daemon", false));
			server.setPort(get(Integer.class, "port", 23999));
			server.setRestartOnShutdown(get(Boolean.class, "restart", false));
			server.setNoSystemExit(get(Boolean.class, "noSystemExit", true));
			if (containsKey("address")) 
				server.setAddress(substituted("address", "localhost"));
			if (containsKey("webRoot")) 
				server.setWebRoot(substituted("webRoot", "localhost"));
			if (containsKey("webPage")) 
				server.setDefaultWebPage(substituted("webPage", "/"));
			String dbPath = substituted("dir", "@{dataDirectory}");
			int i = 0;
			for (String db: getDatabaseNames()) {
				Map m = IpmemsCollections.value(Map.class, dbm, db, EMPTY_MAP);
				String n = IpmemsCollections.value(String.class, m, "name", db);
				String p = IpmemsCollections.value(String.class, m, "path", 
						new File(dbPath, db).toString());
				server.setDatabaseName(i, n);
				server.setDatabasePath(i, p);
				i++;
			}
			Runtime.getRuntime().addShutdownHook(new Thread(this));
			server.start();
			for (int j = 0; j < get(Integer.class, "timeout", 14400); j++) {
				if (isOnline() || !isClosed()) break;
				Thread.sleep(1000L);
			}
		} catch (Exception x) {
			closeStreams();
			throw new IllegalStateException(x);
		}
		for (String db: getDatabaseNames()) try {
			Map m = IpmemsCollections.value(Map.class, dbm, db, EMPTY_MAP);
			String n = IpmemsCollections.value(String.class, m, "name", db);
			Properties ps = new Properties();
			if (m.containsKey("params")) {
				Map p = (Map)m.get("params");
				for (Object k: p.keySet()) 
					ps.setProperty(k.toString(), p.get(k).toString());
			}
			if (m.containsKey("passwordInput")) {
				Class<IpmemsPasswordInput> cpi = IpmemsScriptEngines.loadClass(
						String.valueOf(m.get("passwordInput")));
				IpmemsPasswordInput pi = cpi.newInstance();
				pi.setUserData(new Object[] {db, ps});
				ps.setProperty("user", pi.getUser());
				ps.setProperty("password", new String(pi.getPassword()));
			}
			if (!ps.containsKey("user")) ps.setProperty("user", "SA");
			if (!ps.containsKey("password")) ps.setProperty("password", "");
			String url = String.format("jdbc:hsqldb:%s://localhost:%d/%s",
					server.getProtocol().toLowerCase(),
					server.getPort(), n);
			Connection c = connect(url, ps);
			putGateObject(db, c);
			IpmemsLoggers.info(getLogName(), "[{0}] {1} OK", db, url);
			if (!getObjectNames(db, "table").isEmpty()) continue;
			if (IpmemsCollections.value(Boolean.class, m, "init", true)) {
				String s = Ipmems.substituted(IpmemsCollections.value(
						String.class, m, "script",
						"@{scriptsDirectory}/init.sql"));
				File sql = new File(s);
				if (sql.isFile() && sql.canRead()) {
					InputStreamReader r = null;
					try {
						r = new InputStreamReader(
								new FileInputStream(sql), "UTF-8");
						executeSql(getDir(), c, r, printStream, sql.toString());
					} catch (Exception xx) {
						IpmemsLoggers.warning(getLogName(),	"Sql {0}", xx, sql);
					} finally {
						if (r != null) try {r.close();} catch (Exception xx) {}
					}
				}
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Connection error {0}", x, db);
		}
	}
		
	private void closeStreams() {
		if (printWriter != null) try {
				printWriter.close();
		} catch (Exception xx) {
		} finally {
			printWriter = null;
		}
		if (printStream != null) try {
			printStream.close();
		} catch (Exception x) {
		} finally {
			printStream = null;
		}
	}

	@Override
	public void stop() {
		if (!isRunning()) return;
		try {
			server.shutdown();
		} catch (Exception x) {
			IpmemsLoggers.severe(getLogName(), "Unable to stop DB server", x);
		} finally {
			closeStreams();
		}
	}

	@Override
	public boolean isClosed() {
		return server != null && server.getState() == SERVER_STATE_SHUTDOWN;
	}
	
	private boolean isOnline() {
		return server != null && server.getState() == SERVER_STATE_ONLINE;
	}

	@Override
	public int getPort() {
		return server != null ? server.getPort() : 23999;
	}

	@Override
	public String getProtocol() {
		return server.getProtocol().toLowerCase();
	}

	@Override
	public String getDriverName() {
		return "HSQLDB";
	}

	@Override
	public Driver getDriver() {
		return driver;
	}

	@Override
	public boolean isRunning() {
		try {
			server.checkRunning(true);
			return true;
		} catch (Exception x) {
			return false;
		}
	}

	@Override
	public Set<String> getObjectNames(String db, String type) {
		Connection c = gateObject(db);
		if (c == null) return Collections.emptySet();
		LinkedHashSet<String> s = new LinkedHashSet<String>();
		ResultSet rs = null;
		try {				
			if ("table".equalsIgnoreCase(type)) {
				rs = c.getMetaData().getTables(
						null, null, "%", new String[] {"TABLE"});
				while (rs.next()) s.add(rs.getString("TABLE_NAME"));
			} else if ("view".equalsIgnoreCase(type)) {
				rs = c.getMetaData().getTables(
						null, null, "%", new String[] {"VIEW"});
				while (rs.next()) s.add(rs.getString("TABLE_NAME"));
			} else if ("procedure".equalsIgnoreCase(type)) {
				rs = c.getMetaData().getProcedures(null, null, "%");
				while (rs.next()) s.add(rs.getString("PROCEDURE_NAME"));
			} else if ("function".equalsIgnoreCase(type)) {
				rs = c.getMetaData().getFunctions(null, null, "%");
				while (rs.next()) s.add(rs.getString("FUNCTION_NAME"));
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "?({0},{1})", x, db, type);
		} finally {
			if (rs != null) try {rs.close();} catch (Exception x) {}
		}
		return s;
	}
	
	private static void executeSql(File dir, Connection c, 
			Reader r, PrintStream ps, String label) {
		try {
			SqlFile sf = new SqlFile(r, label, ps, "UTF-8", false, dir);
			sf.setConnection(c);
			sf.setContinueOnError(true);
			sf.execute();
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "Sql file execution", x);
		}
	}
	
	private Server server;
	private PrintWriter printWriter;
	private PrintStream printStream;
	private final JDBCDriver driver = new JDBCDriver();
}
