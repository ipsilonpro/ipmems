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

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * IPMEMS scheduler task.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public interface IpmemsTask extends IpmemsObjItf, Runnable {
	/**
	 * Get the period in ms.
	 * @return Period.
	 */
	public long getPeriod();
	
	/**
	 * Get the execution delay.
	 * @return Execution delay.
	 */
	public long getDelay();
	
	/**
	 * Get the activation date.
	 * @return Activation date.
	 */
	public Date getDate();
	
	/**
	 * Get the periodic flag.
	 * @return Periodic flag.
	 */
	public boolean isPeriodic();
	
	/**
	 * Get the alarm flag.
	 * @return Alarm flag.
	 */
	public boolean isAlarm();	
		
	/**
	 * Get the associated task queue.
	 * @return Associated task queue.
	 */
	public IpmemsTaskQueue getTaskQueue();
		
	/**
	 * Get call queue flag.
	 * @return Call queue flag.
	 */
	public boolean isCallQueue();
			
	/**
	 * Get the killable flag.
	 * @return Killable flag.
	 */
	public boolean isKillable();
	
	/**
	 * Get the delayed state of the task.
	 * @return Delayed flag value.
	 */
	public boolean isDelayed();
	
	/**
	 * Get the realtime flag.
	 * @return Realtime flag.
	 */
	public boolean isRealtime();
	
	/**
	 * Get the timeout.
	 * @return Execution timeout limit.
	 */
	public long getTimeout();
	
	/**
	 * Get the time unit.
	 * @return Time unit.
	 */
	public TimeUnit getTimeUnit();	
}
