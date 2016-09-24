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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import org.ipsilon.ipmems.util.IpmemsDynInvoke;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS socket I/O object.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsSocketIO extends IpmemsPropertized implements IpmemsIO {
	/**
	 * Default constructor.
	 */
	public IpmemsSocketIO() {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the socket I/O object.
	 * @param props Properties.
	 */
	public IpmemsSocketIO(Map props) {
		super(props);
	}
	
	/**
	 * Constructs the socket I/O object.
	 * @param s Socket.
	 * @param props Properties.
	 */
	public IpmemsSocketIO(Socket s, Map props) {
		super(props);
		socket = s;
		initSocketProps();
	}
	
	private void initSocketProps() {
		if (containsKey("socketProps"))
			IpmemsDynInvoke.fill(getLogName(), socket, (Map)get("socketProps"));
	}

	@Override
	public void connect() throws IOException {
		socket = new Socket(getHost(), getPort());
		initSocketProps();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}
	
	/**
	 * Get the log name.
	 * @return Log name.
	 */
	public String getLogName() {
		return get(String.class, "logName", "err");
	}
	
	/**
	 * Get the socket host.
	 * @return Socket host.
	 */
	public String getHost() {
		return socket != null ? socket.getInetAddress().toString() :
				get(String.class, "host", "localhost");
	}
	
	/**
	 * Get the port.
	 * @return Port.
	 */
	public int getPort() {
		return socket != null ? socket.getPort() :
				get(Integer.class, "port", 0);
	}

	@Override
	public Socket getTransceiver() {
		return socket;
	}

	@Override
	public void close() throws IOException {
		if (socket != null) try {
			socket.close();
		} catch (IOException x) {
			throw x;
		} finally {
			socket = null;
		}
	}

	@Override
	public boolean isActive() throws IOException {
		return !socket.isClosed() && socket.isConnected() &&
				!socket.isInputShutdown() && !socket.isOutputShutdown();
	}

	@Override
	public String toString() {
		return socket != null ? socket.toString() : getMap().toString();
	}
	
	private Socket socket;	
}
