package org.ipsilon.ipmems.db;

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

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS DB abstract server.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsDbSqlAbstractServer extends 
		IpmemsDbAbstractServer implements IpmemsDbSqlServer {
	@Override
	public void init(Object... args) {
		setFacility("sqlItf", IpmemsDbStdSqlItf.class);
	}
	
	@Override
	public Connection connect(String url, Properties p) throws SQLException {
		return getDriver().connect(url, p);
	}

	@Override
	public String toString() {
		return getName() + ":" + getPort();
	}

	@Override
	public Connection gateObject(String db) {
		return (Connection)super.gateObject(db);
	}

	@Override
	public IpmemsDbSqlGate gate(String facility, String db) {
		return (IpmemsDbSqlGate)super.gate(facility, db);
	}
	
	protected Properties dbProps(Properties p) {
		Properties ps = new Properties();
		for (String k: ((Properties)p).stringPropertyNames())
			if (k.startsWith("db."))
				ps.setProperty(k.substring(3), ((Properties)p).getProperty(k));
		if (!ps.containsKey("user")) ps.setProperty("user", "SA");
		if (!ps.containsKey("password")) ps.setProperty("password", "");
		return ps;
	}

	@Override
	public IpmemsDbGateWrapper wrap(Properties p) throws Exception {
		if (p.containsKey("driver") && p.containsKey("url")) {
			Properties props = dbProps(p);
			String dc = p.getProperty("driver");
			Driver d = IpmemsScriptEngines.<Driver>loadClass(dc).newInstance();
			Connection c = d.connect(p.getProperty("url"), props);
			return new IpmemsDbSqlWrapper(c);
		} else if (p.containsKey("url")) {
			Properties props = dbProps(p);
			Connection c = connect(p.getProperty("url"), props);
			return new IpmemsDbSqlWrapper(c);
		} else if (isLocalCallsEnabled() && p.containsKey("db")) {
			Connection c = gateObject(p.getProperty("db"));
			return new IpmemsDbSqlWrapper(c, false);
		} else return null;
	}

	@Override
	public void close() throws IOException {
		super.close();
		clearCache();
		for (Object o: gom.values()) try {
			((Connection)o).close();
		} catch (Exception x) {}
		gom.clear();
	}
}
