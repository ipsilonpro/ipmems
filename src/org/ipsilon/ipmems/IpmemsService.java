package org.ipsilon.ipmems;

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

import org.ipsilon.ipmems.util.IpmemsPropertizedItf;

/**
 * IPMEMS service interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsService extends 
		IpmemsObservableItf, IpmemsPropertizedItf {
	/**
	 * Initialize the service.
	 * @param args Arguments.
	 */
	public void init(Object ... args);
	
	/**
	 * Get the service name.
	 * @return Service name.
	 */
	public String getName();
	
	/**
	 * Get the associated log name.
	 * @return Associated log name.
	 */
	public String getLogName();
	
	/**
	 * Checks whether the running state of the service is on.
	 * @return Running state.
	 */
	public boolean isRunning();
	
	/**
	 * Get the service variable name.
	 * @return Service variable name.
	 */
	public String getVar();
	
	/**
	 * Get the service version.
	 * @return Service version.
	 */
	public String getVersion();
	
	/**
	 * Starts the service.
	 */
	public void start();
	
	/**
	 * Stops the service.
	 */
	public void stop();
	
	/**
	 * Restarts the service.
	 */
	public void restart();	
}
