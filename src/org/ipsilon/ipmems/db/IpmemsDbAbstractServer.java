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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import org.ipsilon.ipmems.IpmemsAbstractService;
import org.ipsilon.ipmems.logging.IpmemsLoggers;

/**
 * IPMEMS abstract database server.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsDbAbstractServer extends 
		IpmemsAbstractService implements IpmemsDbServer {

	@Override
	public Class<? extends IpmemsDbGate> getFacility(String name) {
		return fm.get(name);
	}

	@Override
	public void setFacility(String name, Class<? extends IpmemsDbGate> c) {
		fm.put(name, c);
	}

	@Override
	public Set<String> getFacilitySet() {
		return fm.keySet();
	}

	@Override
	public boolean supports(String name) {
		return fm.containsKey(name);
	}

	@Override
	public String getLogName() {
		return "db";
	}

	@Override
	public String getName() {
		return "dbServer";
	}

	@Override
	public String getVar() {
		return getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IpmemsDbGate gate(String facility, String db) {
		Class<? extends IpmemsDbGate> c = fm.get(facility);
		if (c == null) return null; else synchronized(fom) {
			String key = facility + ":" + db;
			IpmemsDbGate f = fom.get(key);
			if (f != null) return f; else try {
				f = c.newInstance();
				f.setGateObject(gateObject(db));
				fom.put(key, f);
				return f;
			} catch (Exception x) {
				IpmemsLoggers.warning(getLogName(), "Gate error: {0}", x, key);
				return null;
			}
		}
	}

	@Override
	public Object gateObject(String db) {
		return gom.get(db);
	}

	/**
	 * Puts the gate object.
	 * @param db Database key.
	 * @param obj Gate object.
	 */
	protected void putGateObject(String db, Object obj) {
		gom.put(db, obj);
	}

	@Override
	public Collection<String> getDatabaseNames() {
		return gom.keySet();
	}

	@Override
	public void clearCache() {
		synchronized(fom) {
			fom.clear();
		}
	}

	@Override
	public void close() throws IOException {
		fm.clear();
	}

	@Override
	public boolean isLocalCallsEnabled() {
		return get(Boolean.class, "localCalls", true);
	}
	
	protected final Map<String,Class<? extends IpmemsDbGate>> fm = new 
			ConcurrentSkipListMap<String,Class<? extends IpmemsDbGate>>();
	private final Map<String,IpmemsDbGate> fom = 
			new HashMap<String,IpmemsDbGate>(8);
	protected final Map<String,Object> gom = new HashMap<String,Object>(4);
}
