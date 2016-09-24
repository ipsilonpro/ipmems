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

package com.ipsilon.ipmems.web;

import com.ipsilon.ipmems.IpmemsSslUtil;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.web.IpmemsAbstractWebServer;

/**
 * IPMEMS secure web server.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsSecureWebServer extends 
		IpmemsAbstractWebServer implements 
		HandshakeCompletedListener {
	@Override
	public String getVar() {
		return get(String.class, "var", "webServer");
	}

	@Override
	public String getName() {
		return get(String.class, "name", "secureWebServer");
	}

	@Override
	public void handshakeCompleted(HandshakeCompletedEvent event) {
		IpmemsLoggers.info(getLogName(), "{0} New client", event.getSocket());
	}

	@Override
	public void run() {
		SSLServerSocket ss;
		try {
			ss = (SSLServerSocket)
					IpmemsSslUtil.ssf().createServerSocket(getPort());
			ss.setReuseAddress(true);
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Server creation error", x);
			return;
		}
		try {
			while (true) {
				SSLSocket cs = (SSLSocket)ss.accept();
				try {
					cs.addHandshakeCompletedListener(this);
					processClient(cs);
				} finally {
					cs.removeHandshakeCompletedListener(this);
				}
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Socket creating error", x);
		}
	}
}
