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

import java.io.Closeable;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import org.ipsilon.ipmems.IpmemsService;

/**
 * IPMEMS Database Server Interface.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public interface IpmemsDbServer extends 
		IpmemsDbFacilities, IpmemsService, Closeable {
	/**
	 * Closed state.
	 * @return Closed state.
	 */
	public boolean isClosed();
	
	/**
	 * Get the gate.
	 * @param facility Facility name.
	 * @param db Database name.
	 * @return Gate to facility.
	 */
	public IpmemsDbGate gate(String facility, String db);
		
	/**
	 * Database names.
	 * @return Database names set.
	 */
	public Collection<String> getDatabaseNames();
		
	/**
	 * Get the gate object.
	 * @param db Database name.
	 * @return Gate object.
	 */
	public Object gateObject(String db);
	
	/**
	 * Get the database object names.
	 * @param db Database name.
	 * @param type Object type (e.g. "TABLE").
	 * @return Object names set.
	 */
	public Set<String> getObjectNames(String db, String type);
		
	/**
	 * Generates a gate object by client map.
	 * @param p Client map.
	 * @return Gate structure.
	 * @throws Exception An exception.
	 */
	public IpmemsDbGateWrapper wrap(Properties p) throws Exception;
		
	/**
	 * Checks whether the local calls are enabled.
	 * @return Check state.
	 */
	public boolean isLocalCallsEnabled();
	
	/**
	 * Clears the facility cache.
	 */
	public void clearCache();	
}
