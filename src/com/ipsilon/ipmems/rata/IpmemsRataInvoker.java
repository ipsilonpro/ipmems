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

package com.ipsilon.ipmems.rata;

import org.ipsilon.ipmems.net.IpmemsTcpClientUtil;

/**
 * IPMEMS RATA invoker.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataInvoker {
	/**
	 * Entry point.
	 * @param args Command-line arguments.
	 * @throws Exception An exception.
	 */
	public static void main(String ... args) throws Exception {
		IpmemsTcpClientUtil.start(
				IpmemsRataClient.class,
				IpmemsConsoleRataClient.class,
				args);
	}
}
