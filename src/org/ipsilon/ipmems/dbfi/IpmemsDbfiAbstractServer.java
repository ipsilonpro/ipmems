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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import org.ipsilon.ipmems.db.IpmemsDbGate;
import org.ipsilon.ipmems.db.IpmemsDbGateWrapper;
import org.ipsilon.ipmems.db.IpmemsDbServer;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.net.IpmemsAbstractTcpServer;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS DBFI abstract server.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsDbfiAbstractServer extends IpmemsAbstractTcpServer {
	@Override
	public void init(Object... args) {
		super.init(args);
	}

	private <T> Class<? extends T> c(
			Class<T> c, String name, Class<? extends T> def) {
		if (containsKey(name)) {
			try {
				return IpmemsScriptEngines.loadClass(get(String.class, name));
			} catch (Exception x) {
				IpmemsLoggers.warning(getLogName(), "{0}", x, c.getName());
				return def;
			}
		} else return def;
	}

	@Override
	public String getLogName() {
		return "dbfi";
	}
	
	@Override
	public int getPort() {
		return get(Integer.class, "port", 23333).intValue();
	}

	/**
	 * Authenticates a client.
	 * @param props Properties.
	 * @param d DBFI class.
	 * @param s Socket.
	 * @param i Input stream.
	 * @param o Output stream.
	 * @return Auth result.
	 * @throws Exception An exception.
	 */
	public boolean auth(Properties props, IpmemsDbfi d, Socket s,
			DataInputStream i, DataOutputStream o) throws Exception {
		boolean b;
		String user = props.getProperty("user", "ipmems");
		String password = props.getProperty("password", "");
		Map<Object,Object> m = new LinkedHashMap<Object,Object>();
		m.put("user", user);
		fireEvent("start", s, m);
		IpmemsDbfiAuthentication a;
		if (props.containsKey("auth")) {
			Class<IpmemsDbfiAuthentication> c = IpmemsScriptEngines.loadClass(
					props.getProperty("auth"));
			a = c.newInstance();
		} else a = new IpmemsDbfiStdAuthentication();
		b = a.auth(user, password);
		IpmemsLoggers.info(getLogName(), "{0} Auth {1}", s, b);
		o.write(b ? 1 : 0);
		m.put("auth", b);
		fireEvent("stop", s, m);
		return b;
	}

	@SuppressWarnings("unchecked")
	public void process(Properties props, IpmemsDbfi d, Socket s,
			DataInputStream i, DataOutputStream o) {
		IpmemsDbGateWrapper gst = null;
		Map<String,?> cs = Collections.singletonMap("conn", null);
		fireEvent("start", s, cs);
		IpmemsDbServer ds = (IpmemsDbServer)IpmemsScriptEngines.get("dbServer");
		try {
			gst = ds.wrap(props);
			fireEvent("stop", s, Collections.singletonMap("conn", true));
		} catch (Exception x) {
			fireEvent("stop", s, Collections.singletonMap("conn", false));
		}
		HashSet<IpmemsDbGate> gs = new HashSet<IpmemsDbGate>();
		IpmemsLoggers.fine(getLogName(), "{0} Conn {1}", s, gst != null);
		if (gst != null) try {
			o.write(1);
			while (true) {
				int n = i.readInt();
				if (n < 0) break;
				byte[] msg = new byte[n];
				i.readFully(msg);
				Map map = d.decodeMap(msg);
				fireEvent("start", s, map);
				Map rm;
				if (map.containsKey("method") && map.containsKey("arg")) {
					final IpmemsDbGate tgt;
					try {
						String m = map.containsKey("target") ?
								map.get("target").toString() : "sql";
						tgt = ds.getFacility(m).newInstance();
						tgt.setGateObject(gst.getGateObject());
						gs.add(tgt);
					} catch (Exception z) {
						IpmemsLoggers.warning(getLogName(), "{0} Target", z, s);
						printError(s, o, z, d);
						continue;
					}
					String mt = String.valueOf(map.get("method"));
					try {
						rm = tgt.gf(mt, (Map)map.get("arg"));
						printMap(s, o, rm, d);
					} catch (Exception z) {
						IpmemsLoggers.warning(getLogName(), "{0} Call", z, s);
						printError(s, o, z, d);
						continue;
					}
				} else printMap(s, o, Collections.EMPTY_MAP, d);
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "{0} Processing", x, s);
		} else try {
			o.write(0);
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "{0} Disconnect", x, s);
		}
		for (IpmemsDbGate g: gs) try {
			g.close();
		} catch (Exception x) {}
		try {
			gst.close();
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "{0} Closing gate error", x, s);
		}
		gs.clear();
	}

	@SuppressWarnings("unchecked")
	public void printError(Socket s, DataOutputStream o, Throwable x,
			IpmemsDbfi d) throws Exception {
		Map m = Collections.singletonMap("error", x);
		byte[] data = d.encodeMap(m);
		o.writeInt(data.length);
		o.write(data);
		o.flush();
		fireEvent("stop", s, m);
	}

	public void printMap(Socket s, DataOutputStream o,
			Map m, IpmemsDbfi dbfi) throws Exception {
		byte[] data = dbfi.encodeMap(m);
		o.writeInt(data.length);
		o.write(data);
		o.flush();
		fireEvent("stop", s, m);
	}
	
	protected Properties readClientMap(DataInputStream i) throws IOException {
		byte[] data = new byte[i.readInt()];
		i.readFully(data);
		Properties props = new Properties();
		props.load(new ByteArrayInputStream(data));
		return props;
	}
}
