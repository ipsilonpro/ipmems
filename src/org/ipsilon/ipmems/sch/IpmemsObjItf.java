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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.ipsilon.ipmems.util.IpmemsPropertizedItf;

/**
 * IPMEMS scheduler object.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public interface IpmemsObjItf extends IpmemsPropertizedItf, Callable<Long> {	
	/**
	 * Get the scheduler object name.
	 * @return Scheduler object name.
	 */
	public String getName();
		
	/**
	 * Get the scheduler object parent.
	 * @return Scheduler object parent.
	 */
	public IpmemsObjItf getParent();
		
	/**
	 * Get the object key.
	 * @return Object key.
	 */
	public String id();
			
	/**
	 * Get the parameter keys array.
	 * @return Array of parameter keys.
	 */
	public Set<String> getKeys();
				
	/**
	 * Get object function.
	 * @return Object function.
	 */
	public Object getFunction();
		
	/**
	 * Get the root scheduler.
	 * @return Root scheduler.
	 */
	public IpmemsObjItf getRoot();
		
	/**
	 * Get the object by path.
	 */ 
	public IpmemsObjItf getObject(String path);
	
	/**
	 * Starts the object.
	 */
	public void start();
	
	/**
	 * Stops the object.
	 */
	public void stop();
	
	/**
	 * Drains all the properties into a new map.
	 * @param m Local map.
	 * @return New map.
	 */
	public Map<String,Object> drain(Map<String,Object> m);
}
