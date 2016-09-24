package org.ipsilon.ipmems.sch;

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

import java.util.List;
import java.util.Map;

/**
 * IPMEMS scheduler task queue.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public interface IpmemsTaskQueue extends IpmemsObjItf {
	/**
	 * Get the string representation.
	 * @return String representation.
	 */
	@Override
	public String toString();
	
	/**
	 * Get threads count.
	 * @return Threads count.
	 */
	public int getThreads();
	
	/**
	 * Get maximum number of threads.
	 * @return Maximum number of threads.
	 */
	public int getMaxThreads();
	
	/**
	 * Get the task groups.
	 * @return Task groups.
	 */
	public List<? extends IpmemsTaskGroup> getGroups();
	
	/**
	 * Get the tasks array.
	 * @return Task array.
	 */
	public List<? extends IpmemsTask> getTasks();
		
	/**
	 * Get the current queue size.
	 * @return Current queue size.
	 */
	public int getSize();
	
	/**
	 * Get the max queue size.
	 * @return Max queue size.
	 */
	public int getMaxSize();
	
	/**
	 * Query the I/O device.
	 * @param o Output configuration.
	 * @param i Input configuration.
	 * @param obj User object.
	 * @param args Debug arguments.
	 * @return Query result.
	 * @throws Exception Any exception.
	 */
	public Map<String,Object> query(
			Map<String,Object> o,
			List<Map<String,Object>> i,
			Map<String,Object> obj,
			Object ... args) throws Exception;
	
	/**
	 * Query the I/O device.
	 * @param o Output configuration.
	 * @param i Input configuration.
	 * @param obj User object.
	 * @param args Debug arguments.
	 * @return Query result.
	 * @throws Exception Any exception.
	 */
	public Map<String,Object> query(
			List<Map<String,Object>> o,
			List<List<Map<String,Object>>> i,
			Map<String,Object> obj,
			Object ... args) throws Exception;
	
	/**
	 * Query the I/O device.
	 * @param t Task for execution.
	 * @param args Debug arguments.
	 * @return Execution result.
	 * @throws Exception Any exception.
	 */
	public Map<String,Object> query(
			IpmemsTask t,
			Object ... args) throws Exception;
	
	/**
	 * Vector query to the I/O device.
	 * @param t Task for execution.
	 * @param args Debug arguments.
	 * @return Execution result.
	 * @throws Exception Any exception.
	 */
	public Map<String,Object> vectorQuery(
			IpmemsTask t,
			Object ... args) throws Exception;
}
