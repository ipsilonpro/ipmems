package org.ipsilon.ipmems.db.file;

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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.db.IpmemsDbAbstractServer;
import org.ipsilon.ipmems.db.IpmemsDbCloseableGateWrapper;
import org.ipsilon.ipmems.db.IpmemsDbGateWrapper;

/**
 * IPMEMS database file server.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDbFileServer extends IpmemsDbAbstractServer {
	@Override
	public void init(Object... args) {
		super.init(args);
		setFacility("dataStore", IpmemsDbFileDataStore.class);
		setFacility("logging", IpmemsDbFileLogging.class);
		File f = new File(substituted("dir", Ipmems.sst("dataDirectory")));
		for (String k: get(String.class, "dbs", "db,arch").split(","))
			putGateObject(k, new IpmemsDbFileConn(new File(f, k)));
	}

	@Override
	public boolean isRunning() {
		return started;
	}

	@Override
	public void start() {
		started = true;
	}

	@Override
	public void stop() {
		started = false;
	}

	@Override
	public boolean isClosed() {
		return fm.isEmpty();
	}

	@Override
	public Set<String> getObjectNames(String db, String type) {
		return Collections.emptySet();
	}

	@Override
	public IpmemsDbFileConn gateObject(String db) {
		return (IpmemsDbFileConn)super.gateObject(db);
	}

	@Override
	public IpmemsDbGateWrapper wrap(Properties p) throws Exception {
		if (isLocalCallsEnabled() && p.containsKey("db")) {
			IpmemsDbFileConn c = gateObject(p.getProperty("db"));
			return new IpmemsDbCloseableGateWrapper(c, false);
		} else return null;
	}

	@Override
	public void close() throws IOException {
		super.close();
	}
	
	private volatile boolean started;
}
