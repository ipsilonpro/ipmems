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

package com.ipsilon.ipmems.rcli;

import com.ipsilon.ipmems.IpmemsSslUtil;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.IpmemsLib;
import org.ipsilon.ipmems.IpmemsStrings;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.net.IpmemsAbstractTcpServer;
import org.ipsilon.ipmems.password.IpmemsAbstractPasswordInput;
import org.ipsilon.ipmems.password.IpmemsPasswordInput;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * RCLI server.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRcliServer extends 
		IpmemsAbstractTcpServer implements HandshakeCompletedListener {
	@Override
	public void init(Object... args) {
		super.init(args);
	}
	
	@Override
	public void process(Socket s) throws Exception {
		IpmemsInterpreter i = null;
		InputStream is = null;
		OutputStream os = null;
		PrintStream ps = null;
		try {
			IpmemsPasswordInput pi = IpmemsAbstractPasswordInput.getDefault();
			is = s.getInputStream();
			os = s.getOutputStream();
			byte[] bImMode = IpmemsIOLib.next(is, (byte)'\n');
			boolean imMode = Boolean.parseBoolean(
					new String(bImMode, "ISO-8859-1").trim());
			byte[] bUser = IpmemsIOLib.next(is, (byte)'\n');
			String user = new String(bUser, "UTF-8").trim();
			boolean userFlag = true;
			boolean mimeFlag = true;
			String eUser = pi.getUser();
			char[] ePsw = pi.getPassword();
			if (!eUser.equals(user)) userFlag = false;
			byte[] bPsw = IpmemsIOLib.next(is, (byte)'\n');
			char[] password = new String(bPsw, "UTF-8").trim().toCharArray();
			boolean pswFlag;
			try {
				pswFlag = Arrays.equals(ePsw, password);
			} catch (Exception xx) {
				pswFlag = false;
			}
			Map<String,IpmemsScriptEngine> es = 
					new LinkedHashMap<String,IpmemsScriptEngine>();
			byte d = imMode ? (byte)'\n' : 0x0C;
			write(imMode, os, IpmemsIntl.message("Select the script type"));
			os.flush();
			String[] names = IpmemsScriptEngines.getEngineIds();
			for (int j = 0; j < names.length; j++) {
				IpmemsScriptEngine e = IpmemsScriptEngines.getEngine(names[j]);
				if (j == 0) es.put("", e);
				es.put(e.getDefaultMime(), e);
				if (imMode) {
					os.write(Integer.toString(j + 1).getBytes("ISO-8859-1"));
					os.write(". ".getBytes("ISO-8859-1"));
				}
				os.write(e.getDefaultMime().getBytes("UTF-8"));
				if (j < names.length - 1) os.write((byte)'\n');
			}
			os.write(d);
			os.flush();
			write(imMode, os, IpmemsIntl.message(
					"Enter engine type [{0}]:", names[0]));
			byte[] bMime = IpmemsIOLib.next(is, d);
			String mime = new String(bMime, "ISO-8859-1").trim();
			int engIndex;
			IpmemsScriptEngine e = null;
			if (imMode) try {
				engIndex = mime.isEmpty() ? 0 : Integer.parseInt(mime) - 1;
				e = IpmemsScriptEngines.getEngine(names[engIndex]);
				ps = e.printStream(os, true);
				Map<Object,Object> b = new HashMap<Object,Object>();
				b.put("out", ps);
				i = e.makeInterpreter(b);
			} catch (Exception x) {
				mimeFlag = false;
				IpmemsLoggers.warning(getLogName(), "{0} Mimes", x, s);
			} else try {
				e = es.get(mime);
				ps = e.printStream(os, true);
				Map<Object,Object> b = new HashMap<Object,Object>();
				b.put("out", ps);
				i = e.makeInterpreter(b);
			} catch (Exception x) {
				mimeFlag = false;
				IpmemsLoggers.warning(getLogName(), "{0} Mime", x, s);
			}
			ps.println(get("logo", IpmemsLib.getLogo()));
			ps.print(IpmemsIntl.string("Scripting engine") + ": ");
			ps.print(e);
			if (imMode) ps.println();
			if (imMode) ps.println(IpmemsStrings.repeat('-', 80));
			ps.write(d);
			if (imMode) ps.print(get("prompt", ">") + " ");
			ps.flush();
			if (!userFlag) {
				write(imMode, os, "UNKNOWN_USER");
				throw new IllegalAccessException("User");
			}
			if (!pswFlag) {
				write(imMode, os, "INVALID_PASSWORD");
				throw new IllegalAccessException("Password");
			}
			if (!mimeFlag) {
				write(imMode, os, "INVALID_MIME_TYPE");
				throw new IllegalStateException("Mime");
			}
			processStreams(imMode, i, s, is, ps);
		} finally {
			if (ps != null) try {ps.close();} catch (Exception x) {}
			if (os != null) try {os.close();} catch (Exception x) {}
			if (is != null) try {is.close();} catch (Exception x) {}
			((SSLSocket)s).removeHandshakeCompletedListener(this);
		}
	}
	
	private void processStreams(boolean im, IpmemsInterpreter i, Socket s,
			InputStream is, PrintStream ps) throws Exception {
		if (im) while (true) {
			byte[] bLine = IpmemsIOLib.next(is, (byte)'\n');
			if (bLine == null) break;
			String line = new String(bLine, "UTF-8").trim();
			if ("exit".equals(line) || "quit".equals(line)) break;
			IpmemsLoggers.info(getLogName(), "{0} Entered {1}", s, line);
			try {
				Object r = i.eval("rcliCommand", line);
				ps.println(r);
			} catch (Exception x) {
				x.printStackTrace(ps);
			}
			ps.print(get("prompt", ">") + " ");
		} else while (true) {
			byte[] bLine = IpmemsIOLib.next(is, (byte)0x0C);
			if (bLine == null) break;
			String line = new String(bLine, "UTF-8").trim();
			if ("exit".equalsIgnoreCase(line) || 
					"quit".equalsIgnoreCase(line)) break;
			IpmemsLoggers.info(getLogName(), "{0} Entered {1}", s, line);
			try {
				Object r = i.eval("rcliCommand", line);
				ps.print(r);
			} catch (Exception x) {
				x.printStackTrace(ps);
			}
			ps.write(0x0C);
			ps.flush();
		}
	}
		
	private void write(boolean i, OutputStream os, String s) throws Exception {
		if (i) {
			os.write(s.getBytes("UTF-8"));
			os.write('\n');
			os.flush();
		} else {
			os.write(s.getBytes("UTF-8"));
			os.write(0x0C);
			os.flush();
		}
	}
	
	@Override
	public int getPort() {
		return get(Integer.class, "port", 23666).intValue();
	}

	@Override
	public String getName() {
		return "rcliServer";
	}

	@Override
	public String getLogName() {
		return "rcli";
	}
	
	@Override
	public void run() {
		SSLServerSocket ss;
		try {
			ss = (SSLServerSocket)
					IpmemsSslUtil.ssf().createServerSocket(getPort());
			ss.setNeedClientAuth(true);
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Server creation error", x);
			return;
		}
		try {
			while (true) {
				SSLSocket cs = (SSLSocket)ss.accept();
				cs.addHandshakeCompletedListener(this);
				processClient(cs);
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Socket creation error", x);
		}
	}

	@Override
	public void handshakeCompleted(HandshakeCompletedEvent event) {
		IpmemsLoggers.info(getLogName(), "{0} New client", event.getSocket());
	}
}
