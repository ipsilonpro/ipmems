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

/**
 * IPMEMS scheduler task group.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public interface IpmemsTaskGroup extends IpmemsObjItf {
	/**
	 * Get the string representation.
	 * @return String representation.
	 */
	@Override
	public String toString();
	
	/**
	 * Get the tasks.
	 * @return Task array.
	 */
	public List<? extends IpmemsTask> getTasks();
	
	/**
	 * Get the task groups.
	 * @return Task groups.
	 */
	public List<? extends IpmemsTaskGroup> getGroups();
	
	/**
	 * Get the task queue.
	 * @return Task queue.
	 */
	public IpmemsTaskQueue getTaskQueue();	
}
