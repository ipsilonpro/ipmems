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

import java.io.IOException;
import java.net.*;
import java.net.Proxy.Type;
import static java.net.Proxy.Type.DIRECT;
import java.util.Map;
import org.ipsilon.ipmems.util.IpmemsDynInvoke;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS abstract TCP client.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsAbstractTcpClient extends 
		IpmemsPropertized implements IpmemsTcpClient {
	/**
	 * Default constructor.
	 */
	public IpmemsAbstractTcpClient() {
	}
	
	/**
	 * Constructs the abstract TCP client.
	 * @param ps Properties map.
	 */
	public IpmemsAbstractTcpClient(Map ps) {
		super(ps);
	}

	@Override
	public void init(Object... args) {
		for (int i = 0; i < args.length; i += 2) if (i < args.length - 1)
			put(String.valueOf(args[i]), args[i + 1]);
	}

	@Override
	public boolean connect() throws IOException {
		if (containsKey("socket")) {
			socket = get(Socket.class, "socket");
		} else if (containsKey("port")) {
			String host = get(String.class, "host", "localhost");
			int port = get(Integer.class, "port");
			if (containsKey("proxy")) {
				Type type = get(Type.class, "proxy", DIRECT);
				String ph = get(String.class, "proxyHost", "localhost");
				int pp = get(Integer.class, "proxyPort", 10080);
				Proxy proxy = new Proxy(type, new InetSocketAddress(ph, pp));
				socket = new Socket(proxy);
			}
			if (containsKey("interface")) {
				if (socket == null) socket = new Socket();
				String name = get(String.class, "interface");
				NetworkInterface ni = NetworkInterface.getByName(name);
				int index = get(Integer.class, "interfaceIndex", 0);
				InterfaceAddress ia = ni.getInterfaceAddresses().get(index);
				int p = get(Integer.class, "bindPort", 0);
				socket.bind(new InetSocketAddress(ia.getAddress(), p));
			} else if (containsKey("bindAddress")) {
				if (socket == null) socket = new Socket();
				String bh = get(String.class, "bindAddress", "localhost");
				int bp = get(Integer.class, "bindPort", 0);
				socket.bind(new InetSocketAddress(bh, bp));
			}
			if (socket == null) socket = new Socket(host, port);
		} else return false;
		if (!socket.isConnected()) {
			String host = get(String.class, "host", "localhost");
			int port = get(Integer.class, "port");
			if (containsKey("timeout")) {
				int to = get(Integer.class, "timeout");
				socket.connect(new InetSocketAddress(host, port), to);
			} else socket.connect(new InetSocketAddress(host, port));
		}
		if (containsKey("socketProps"))
			IpmemsDynInvoke.fill("err", socket, (Map)get("socketProps"));
		return socket.isConnected();
	}

	@Override
	public boolean isConnected() {
		Socket s = socket;
		return s != null && !s.isClosed();
	}

	@Override
	public void disconnect() throws IOException {
	}

	@Override
	public void start() {
	}

	@Override
	public void close() throws IOException {
		try {
			socket.close();
		} catch (Exception x) {
		} finally {
			socket = null;
		}
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

	@Override
	public String toString() {
		Socket s = socket;
		return s == null ? getMap().toString() :
				s.getInetAddress().getHostName() + ":" + s.getPort();
	}
	
	/**
	 * Associated socket.
	 */
	protected volatile Socket socket;
}
