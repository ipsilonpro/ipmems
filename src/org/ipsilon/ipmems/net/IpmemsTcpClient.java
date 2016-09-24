package org.ipsilon.ipmems.net;

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
import java.net.Socket;
import org.ipsilon.ipmems.util.IpmemsPropertizedItf;

/**
 * IPMEMS TCP client interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsTcpClient extends IpmemsPropertizedItf, Closeable {
	/**
	 * Initializes the client.
	 * @param args Arguments.
	 */
	public void init(Object ... args);
	
	/**
	 * Connects to the target host.
	 * @return Connection status.
	 */
	public boolean connect() throws IOException;
	
	/**
	 * Disconnects from remote server.
	 * @throws IOException An I/O exception.
	 */
	public void disconnect() throws IOException;
	
	/**
	 * Get the connection status.
	 * @return Connection status.
	 */
	public boolean isConnected();
	
	/**
	 * Get the client key.
	 * @return Client key.
	 */
	public String getKey();
	
	/**
	 * Starts the TCP client.
	 */
	public void start();
	
	/**
	 * Get the associated socket.
	 * @return Associated socket.
	 */
	public Socket getSocket();
}
