package org.ipsilon.ipmems.logging;

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

/**
 * IPMEMS Logger Interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsLoggerItf extends Comparable<IpmemsLoggerItf> {
	/**
	 * Adds a handler.
	 * @param h A handler.
	 */
	public void addHandler(IpmemsLogHandler h);
	
	/**
	 * Removes the handler.
	 * @param h Log handler.
	 */
	public void removeHandler(IpmemsLogHandler h);
	
	/**
	 * Get the logger's key.
	 * @return Logger's key.
	 */
	public String getKey();
	
	/**
	 * Logs the record.
	 * @param r Logs the record.
	 */
	public void log(IpmemsLogRec r);
	
	/**
	 * Logs the array of records.
	 * @param rs Records.
	 */
	public void log(IpmemsLogRec[] rs);
}
