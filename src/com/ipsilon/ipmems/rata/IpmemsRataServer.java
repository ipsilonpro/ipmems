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

package com.ipsilon.ipmems.rata;

import com.ipsilon.ipmems.IpmemsSslUtil;
import com.ipsilon.ipmems.rata.data.IpmemsRataAuthData;
import com.ipsilon.ipmems.rata.data.IpmemsRataRq;
import com.ipsilon.ipmems.rata.data.IpmemsRataRs;
import com.ipsilon.ipmems.rata.data.IpmemsRataRsErr;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsLib;
import org.ipsilon.ipmems.data.IpmemsIntData;
import org.ipsilon.ipmems.data.IpmemsMapData;
import org.ipsilon.ipmems.data.IpmemsThrowableData;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.net.IpmemsAbstractTcpServer;
import org.ipsilon.ipmems.password.IpmemsAbstractPasswordInput;
import org.ipsilon.ipmems.password.IpmemsPasswordInput;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS abstract RATA server.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataServer extends IpmemsAbstractTcpServer implements
		HandshakeCompletedListener {
	@Override
	public void process(Socket s) throws Exception {
		ObjectInputStream is = null;
		ObjectOutputStream os = null;
		IpmemsRataContext ctx = null;
		try {
			os = new ObjectOutputStream(s.getOutputStream());
			is = new ObjectInputStream(s.getInputStream());
			IpmemsMapData smd = new IpmemsMapData();
			smd.setData(getSrvMap());
			os.writeObject(smd);
			os.flush();
			IpmemsMapData cm = (IpmemsMapData)is.readObject();
			IpmemsRataAuthData ad = (IpmemsRataAuthData)is.readObject();
			auth(cm.getData(), ad);
			Map<String,String> em = new LinkedHashMap<String,String>();
			for (String id: IpmemsScriptEngines.getEngineIds()) {
				IpmemsScriptEngine e = IpmemsScriptEngines.getEngine(id);
				em.put(e.getDefaultMime(), e.toString());
			}
			IpmemsMapData emd = new IpmemsMapData();
			emd.setData(em);
			os.writeObject(emd);
			os.flush();
			IpmemsIntData ei = (IpmemsIntData)is.readObject();
			IpmemsScriptEngine e = IpmemsScriptEngines.getEngine(ei.getData());
			ctx = new IpmemsRataContext(this, cm.getData(), e, s, is, os);
			while (true) {
				IpmemsRataRq in = ctx.in();
				if (in == null) break;
				IpmemsLoggers.info(getLogName(), "{0} Read {1}", s, in);
				IpmemsRataRs out;
				try {
					out = IpmemsRataData.exec(ctx, in);
				} catch (Exception x) {
					out = new IpmemsRataRsErr(x);
				}
				if (out != null) ctx.out(out);
				IpmemsLoggers.info(getLogName(), "{0} Write {1}", s, out);
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "{0} Error", x, s);
			if (os != null)	{
				IpmemsThrowableData d = new IpmemsThrowableData();
				d.setData(x);
				os.writeObject(d);
				os.flush();
			}
		} finally {
			if (ctx != null) try {ctx.close();} catch (Exception x) {}
			if (os != null) try {os.close();} catch (Exception x) {}
			if (is != null) try {is.close();} catch (Exception x) {}
			((SSLSocket)s).removeHandshakeCompletedListener(this);
		}
	}
	
	protected void auth(Map cm, IpmemsRataAuthData d) throws Exception {
		IpmemsPasswordInput pi = IpmemsAbstractPasswordInput.getDefault();
		if (!pi.getUser().equals(d.getName()))
			throw new IllegalAccessException("Invalid name");
		if (!Arrays.equals(pi.getPassword(), d.getPassword()))
			throw new IllegalAccessException("Invalid password");
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
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "Creation error", x);
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
	public int getPort() {
		return get(Integer.class, "port", 23111);
	}

	@Override
	public String getName() {
		return "rataServer";
	}

	@Override
	public String getLogName() {
		return "rata";
	}
	
	private Map<String,Object> getSrvMap() {
		HashMap<String,Object> m = new HashMap<String,Object>(Ipmems.getMap());
		m.put("logo", IpmemsLib.getLogo());
		return m;
	}
}
