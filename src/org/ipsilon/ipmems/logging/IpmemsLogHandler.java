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

import java.io.Closeable;

/**
 * IPMEMS log handler.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsLogHandler extends Closeable {
	/**
	 * Publishes the log record.
	 * @param record Log record.
	 */
	public boolean publish(IpmemsLogRec record);
	
	/**
	 * Publishes the log records.
	 * @param records Log records.
	 * @return Log records.
	 */
	public IpmemsLogRec[] publish(IpmemsLogRec[] records);
		
	/**
	 * Get current logging level.
	 * @return Logging level.
	 */
	public int getLevel();
	
	/**
	 * Set the current logging level.
	 * @param l Logging level.
	 */
	public void setLevel(int l);
	
	/**
	 * Get the logging filter.
	 * @return Logging filter.
	 */
	public IpmemsLogFilter getFilter();
	
	/**
	 * Set the logging filter.
	 * @param f Logging filter.
	 */
	public void setFilter(IpmemsLogFilter f);
}
