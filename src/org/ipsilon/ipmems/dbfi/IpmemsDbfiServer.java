package org.ipsilon.ipmems.dbfi;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * DBFI simple server.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDbfiServer extends IpmemsDbfiAbstractServer {
	@Override
	public void process(Socket s) throws Exception {
		DataInputStream is = null;
		DataOutputStream os = null;
		try {
			is = new DataInputStream(s.getInputStream());
			os = new DataOutputStream(s.getOutputStream());
			Properties p = readClientMap(is);
			IpmemsDbfi d;
			if (p.containsKey("dbfi")) {
				Class<IpmemsDbfi> c = IpmemsScriptEngines.loadClass(
						p.getProperty("dbfi"));
				d = c.newInstance();
			} else d = new IpmemsDbfiGzippedExt();
			d.setEncoding(p.getProperty("encoding", "UTF-8"));
			if (auth(p, d, s, is, os)) process(p, d, s, is, os);
		} finally {
			if (os != null) try {os.close();} catch (Exception xx) {}
			if (is != null) try {is.close();} catch (Exception xx) {}
			try {s.close();} catch (Exception xx) {}
		}
	};
	
	@Override
	public void run() {
		try {
			ServerSocket ss = new ServerSocket(getPort());
			while (true) {
				Socket s = ss.accept();
				IpmemsLoggers.info(getLogName(), "{0} New client", s);
				processClient(s);
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Server error", x);
		}
	}

	@Override
	public String getName() {
		return "dbfiServer";
	}
}
