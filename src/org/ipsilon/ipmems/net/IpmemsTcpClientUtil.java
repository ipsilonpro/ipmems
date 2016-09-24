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

import java.io.File;
import java.util.Map;
import java.util.ServiceLoader;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.json.IpmemsJsonUtil;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.password.IpmemsConsolePasswordInput;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsCollections;

/**
 * IPMEMS TCP client utilities.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsTcpClientUtil extends IpmemsCollections {
	/**
	 * Start the client.
	 * @param itf Client interface.
	 * @param def Default client class.
	 * @param args Arguments.
	 * @throws Exception An exception.
	 */
	public static void start(
			Class<? extends IpmemsTcpClient> itf,
			Class<? extends IpmemsTcpClient> def,
			String[] args) throws Exception {
		if (Ipmems.help(itf, args)) return;
		Ipmems.cmd(args);
		String host = arg(args, "t", "host", null);
		Integer port = arg(args, Integer.class, "p", "port", null);
		String proxyType = arg(args, "T", "proxyType", null);
		String proxyHost = arg(args, "H", "proxyHost", null);
		Integer proxyPort = arg(args, Integer.class, "P", "proxyPort", null);
		for (String k: args) if ("-r".equals(k)) {
			IpmemsConsolePasswordInput.READLINE = true;
			break;
		}
		IpmemsTcpClient cl = null;
		ServiceLoader<? extends IpmemsTcpClient> sl = ServiceLoader.load(
				itf, IpmemsScriptEngines.getDefaultClassLoader());
		String mode = IpmemsCollections.arg(args, "m", "mode", "auto");
		for (IpmemsTcpClient c: sl) if (c.getKey().equals(mode)) {
			cl = c;
			break;
		}
		if (cl == null) cl = def.newInstance();
		String pf = arg(args, "f", "file", null);
		if (pf != null) {
			String udir = System.getProperty("user.dir");
			Map ps = (Map)IpmemsJsonUtil.parse(new File(udir, pf));
			for (Object k: ps.keySet())	cl.put(k.toString(), ps.get(k));
		}
		String oc = arg(args, "c", "conf", null);
		if (oc != null) {
			Map ps = (Map)IpmemsJsonUtil.parse(oc);
			for (Object k: ps.keySet())	cl.put(k.toString(), ps.get(k));
		}
		if (host != null) cl.put("host", host);
		if (port != null) cl.put("port", port);
		if (proxyType != null) cl.put("proxyType", proxyType);
		if (proxyHost != null) cl.put("proxyHost", proxyHost);
		if (proxyPort != null) cl.put("proxyPort", proxyPort);
		if (cl.connect()) cl.start();
	}
	
	/**
	 * Start the local client.
	 * @param cn Class name.
	 * @param srv Server name.
	 */
	public static void start(String cn, String srv) {
		try {
			start(IpmemsScriptEngines.<IpmemsTcpClient>loadClass(cn), srv);
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "Client error", x);
		}
	}
	
	/**
	 * Start the local client.
	 * @param itf Client interface.
	 * @param srv Server name.
	 */
	public static void start(Class<? extends IpmemsTcpClient> itf, String srv) {
		final Class<? extends IpmemsTcpClient> c = itf;
		final String s = srv;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					IpmemsTcpClient cl = c.newInstance();
					Object sv = IpmemsScriptEngines.userMap.get(s);
					if (sv instanceof IpmemsAbstractTcpServer)
						cl.put("port", ((IpmemsAbstractTcpServer)sv).getPort());
					if (cl.connect()) cl.start();
				} catch (Exception x) {
					IpmemsLoggers.warning("err", "Start client error", x);
				}
			}
		}).start();
	}	
}
