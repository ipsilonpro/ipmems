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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import static java.util.Collections.EMPTY_MAP;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS server socket I/O.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsServerSocketIO extends 
		IpmemsPropertized implements IpmemsServerIO {
	/**
	 * Default constructor.
	 */
	public IpmemsServerSocketIO() {
		this(EMPTY_MAP);
	}
	
	/**
	 * Constructs the server socket I/O object.
	 * @param props Server properties.
	 */
	public IpmemsServerSocketIO(Map props) {
		super(props);
	}

	@Override
	public ServerSocket getServerObject() {
		return server;
	}

	@Override
	public IpmemsSocketIO call() throws Exception {
		Socket s = !containsKey("socketFunc") ? server.accept() :
				(Socket)IpmemsScriptEngines.call(get("socketFunc"), this);
		IpmemsSocketIO io = new IpmemsSocketIO(s,
				get(Map.class, "clientProps", EMPTY_MAP));
		if (containsKey("trigger"))
			IpmemsScriptEngines.call(get("trigger"), this, io);
		return io;
	}

	@Override
	public void init() throws IOException {
		if (!containsKey("port")) {
			if (containsKey("func")) try {
				server = (ServerSocket)
						IpmemsScriptEngines.call(get("func"), this);
			} catch (Exception x) {
				throw new InvalidPropertiesFormatException(x);
			} else return;
		}
		if (containsKey("backlog")) {
			if (containsKey("inetAddress"))
				server = new ServerSocket(
						get(Integer.class, "port"),
						get(Integer.class, "backlog"),
						get(InetAddress.class, "inetAddress"));
			else
				server = new ServerSocket(
						get(Integer.class, "port"),
						get(Integer.class, "backlog"));
		} else  server = new ServerSocket(get(Integer.class, "port"));
		if (containsKey("init")) try {
			IpmemsScriptEngines.call(get("init"), this);
		} catch (Exception x) {
			throw new InvalidPropertiesFormatException(x);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			server.close();
		} finally {
			server = null;
		}
	}

	@Override
	public boolean isMultiClient() {
		return true;
	}

	@Override
	public void restart() throws IOException {
		try {
			server.close();
		} finally {
			server = null;
		}
		init();
	}

	@Override
	public String toString() {
		if (server != null) return server.toString();
		else {
			String h = get(String.class, "inetAddress", "localhost");
			int p = get(Integer.class, "port", 0);
			return h + ":" + p;
		}
	}
	
	private ServerSocket server;
}
