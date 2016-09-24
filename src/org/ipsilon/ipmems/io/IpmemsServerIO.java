package org.ipsilon.ipmems.io;

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
import java.io.IOException;
import java.util.concurrent.Callable;
import org.ipsilon.ipmems.util.IpmemsPropertizedItf;

/**
 * IPMEMS server side I/O.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsServerIO extends 
		Closeable, Callable<IpmemsIO>, IpmemsPropertizedItf {
	/**
	 * Get the server object.
	 * @return Server object.
	 */
	public Object getServerObject();
	
	/**
	 * Initialization of the server.
	 * @throws IOException An I/O exception.
	 */
	public void init() throws IOException;
	
	/**
	 * Get the multi-client flag.
	 * @return Multi-client flag.
	 */
	public boolean isMultiClient();
	
	/**
	 * Restarts the server I/O.
	 */
	public void restart() throws IOException;
}
