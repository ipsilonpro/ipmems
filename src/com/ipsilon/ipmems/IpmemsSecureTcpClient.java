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

package com.ipsilon.ipmems;

import java.io.IOException;
import java.util.Map;
import javax.net.ssl.SSLSocket;
import org.ipsilon.ipmems.net.IpmemsAbstractTcpClient;

/**
 * IPMEMS abstract SSL TCP client.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsSecureTcpClient extends IpmemsAbstractTcpClient {
	/**
	 * Default constructor.
	 */
	public IpmemsSecureTcpClient() {
	}
	
	/**
	 * Constructs the secure TCP client.
	 * @param ps Properties map.
	 */
	public IpmemsSecureTcpClient(Map ps) {
		super(ps);
	}

	@Override
	public boolean connect() throws IOException {
		boolean c = super.connect();
		if (c) {
			if (!(socket instanceof SSLSocket)) {
				String h = get(String.class, "host", "localhost");
				int p = get(Integer.class, "port");
				socket = IpmemsSslUtil.sf().createSocket(socket, h, p, true);
				return true;
			} else return true;
		} else return false;
	}

	@Override
	public SSLSocket getSocket() {
		return (SSLSocket)super.getSocket();
	}
}
