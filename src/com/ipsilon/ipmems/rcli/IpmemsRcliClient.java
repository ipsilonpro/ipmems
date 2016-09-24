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

package com.ipsilon.ipmems.rcli;

import org.ipsilon.ipmems.net.IpmemsTcpClient;

/**
 * IPMEMS RCLI client.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public interface IpmemsRcliClient extends IpmemsTcpClient {
	/**
	 * Prints the error message.
	 * @param msg Message.
	 * @param args Message arguments.
	 */
	public void printMessage(String msg, Object ... args);
	
	/**
	 * Prints the error message.
	 * @param msg Message.
	 * @param t Exception.
	 * @param args Message arguments.
	 */
	public void printError(String msg, Throwable t, Object ... args);	
}
