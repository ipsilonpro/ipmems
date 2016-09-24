package org.ipsilon.ipmems.web;

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

import java.net.ServerSocket;
import java.net.Socket;
import org.ipsilon.ipmems.logging.IpmemsLoggers;

/**
 * IPMEMS standard Web server.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsWebServer extends IpmemsAbstractWebServer {
	@Override
	public void run() {
		ServerSocket ss;
		try {
			ss = new ServerSocket(getPort());
			ss.setReuseAddress(true);
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Server socket error", x);
			return;
		}
		try {
			while (true) {
				Socket s = ss.accept();
				IpmemsLoggers.info(getLogName(), "{0} New client", s);
				processClient(s);
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Socket creating error", x);
		}
	}

	@Override
	public String getName() {
		return get(String.class, "name", "webServer");
	}

	@Override
	public String getVar() {
		return get(String.class, "var", getName());
	}
}
