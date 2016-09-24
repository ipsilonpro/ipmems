package org.ipsilon.ipmems.tnt;

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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.IpmemsLib;
import org.ipsilon.ipmems.IpmemsStrings;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.net.IpmemsAbstractTcpServer;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS telnet server.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsTelnetServer extends IpmemsAbstractTcpServer {
	@Override
	public void init(Object... args) {
		super.init(args);
	}

	@Override
	public void process(final Socket s) throws Exception {
		IpmemsScriptEngine e;
		IpmemsInterpreter i;
		InputStream is = null;
		PrintStream ps = null;
		OutputStream os = null;
		try {
			is = s.getInputStream();
			os = s.getOutputStream();
			os.write(IpmemsStrings.repeat('-', 40).getBytes("ISO-8859-1"));
			os.write('\n');
			String[] names = IpmemsScriptEngines.getEngineIds();
			writeln(os, IpmemsIntl.message("Enter script type") + ": ");
			for (int j = 0; j < names.length; j++)
				writeln(os, Integer.toString(j + 1) + ". " + names[j]);
			os.write("> [1]: ".getBytes("ISO-8859-1"));
			String n = new String(IpmemsIOLib.next(is, (byte)'\n'),
					"ISO-8859-1").trim();
			n = n.isEmpty() ? "1" : n;
			String key = names[Integer.parseInt(n) - 1];
			e = IpmemsScriptEngines.getEngine(key);
			ps = e.printStream(os, true);
			Map<Object,Object> b = new HashMap<Object,Object>();
			b.put("out", ps);
			i = e.makeInterpreter(b);
			ps.println();
			processStreams(i, s, is, ps);
		} finally {
			if (ps != null) try {ps.close();} catch (Exception x) {}
			if (os != null) try {os.close();} catch (Exception x) {}
			if (is != null) try {is.close();} catch (Exception x) {}
			if (s != null) try {s.close();} catch (Exception x) {}
		}
	}
	
	public void writeln(OutputStream os, String s) throws Exception {
		os.write(s.getBytes("UTF-8"));
		os.write(System.getProperty("line.separator").getBytes("UTF-8"));
		os.flush();
	}
		
	/**
	 * Run the server.
	 */
	@Override
	public void run() {
		ServerSocket ss;
		try {
			ss = new ServerSocket(getPort());
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Creating socket server", x);
			return;
		}
		try {
			while (true) {
				Socket s = ss.accept();
				IpmemsLoggers.info(getLogName(), "{0} New client", s);
				processClient(s);
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Socket creating", x);
		}
	}
				
	/**
	 * Process the TELNET streams.
	 * @param sh Groovy shell.
	 * @param s Client socket.
	 * @param q Scanner.
	 * @param p Print stream.
	 */
	private void processStreams(IpmemsInterpreter i, Socket s, 
			InputStream is, PrintStream p) throws Exception {
		p.println(get("logo", IpmemsLib.getLogo()));
		p.println(IpmemsStrings.repeat('-', 40));
		p.print(get("prompt", ">") + " ");
		while (true) {
			String line = new String(
					IpmemsIOLib.next(is, (byte)'\n'), "UTF-8").trim();
			if ("exit".equals(line) || "quit".equals(line)) break;
			IpmemsLoggers.info(getLogName(), "{0} entered {1}", s, line);
			try {
				Object r = i.eval("telnetCommand", line);
				p.println(r);
			} catch (Exception x) {
				x.printStackTrace(p);
			}
			p.print(get("prompt", ">") + " ");
		}
	}

	@Override
	public String getVar() {
		return "telnetServer";
	}

	@Override
	public String getName() {
		return "telnetServer";
	}

	@Override
	public String getLogName() {
		return "tnt";
	}

	@Override
	public int getPort() {
		return get(Integer.class, "port", 23777);
	}
}
