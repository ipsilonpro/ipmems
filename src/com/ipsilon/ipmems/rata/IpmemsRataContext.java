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

import com.ipsilon.ipmems.rata.data.IpmemsRataLogData;
import com.ipsilon.ipmems.rata.data.IpmemsRataLogRecord;
import com.ipsilon.ipmems.rata.data.IpmemsRataLogRecords;
import com.ipsilon.ipmems.rata.data.IpmemsRataRq;
import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.ipsilon.ipmems.logging.*;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.ipsilon.ipmems.util.IpmemsGzippedExternalizable;

/**
 * IPMEMS RATA context.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataContext implements IpmemsLoggersListener, Closeable {
	/**
	 * Constructs the RATA context.
	 * @param sr RATA server.
	 * @param m Client map.
	 * @param eng Scripting engine.
	 * @param s Socket.
	 * @param i Object input.
	 * @param o Object output.
	 */
	public IpmemsRataContext(
			IpmemsRataServer sr,
			Map m,
			IpmemsScriptEngine eng,
			Socket s,
			ObjectInput i,
			ObjectOutput o) throws Exception {
		srv = sr;
		clientMap = m;
		socket = s;
		input = i;
		output = o;
		engine = eng;
		mstream = new IpmemsRataOutputStream(this);
		Map<Object,Object> b = new HashMap<Object,Object>();
		ps = engine.printStream(mstream, true);
		b.put("out", ps);
		interpreter = engine.makeInterpreter(b);
		ex = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	/**
	 * Get the client map.
	 * @return Client map.
	 */
	public Map getClientMap() {
		return clientMap;
	}

	/**
	 * Get the socket listener.
	 * @return Socket listener.
	 */
	public IpmemsRataServer getServer() {
		return srv;
	}

	/**
	 * Get the associated interpreter.
	 * @return Associated interpreter.
	 */
	public IpmemsInterpreter getInterpreter() {
		return interpreter;
	}

	@Override
	public void removed(IpmemsLogEventData data) {
		for (Iterator<Handler> i = hs.iterator(); i.hasNext(); ) {
			Handler h = i.next();
			if (h.getKey().equals(data.getLogger().getKey())) {
				h.close();
				i.remove();
				break;
			}
		}
		out(new IpmemsRataLogData(false, data.getLogger().getKey()));
	}

	@Override
	public void added(IpmemsLogEventData data) {
		out(new IpmemsRataLogData(true, data.getLogger().getKey()));
		hs.add(new Handler(data));
	}
	
	/**
	 * Monitors the log.
	 * @param state Monitoring state.
	 */
	public synchronized void monitorLog(boolean state) {
		if (oldState != state) {
			if (state) IpmemsLoggers.addLogListener(this);
			else IpmemsLoggers.removeLogListener(this);
			oldState = state;
		}
	}

	/**
	 * Get the string mode state.
	 * @return String mode state.
	 */
	public boolean isStrMode() {
		return strMode;
	}

	/**
	 * Sets the string mode.
	 * @param sm String mode.
	 */
	public void setStrMode(boolean sm) {
		strMode = sm;
	}
	
	/**
	 * Get the object in the IN-queue.
	 * @return An object in IN-queue.
	 */
	public IpmemsRataRq in() {
		try {
			Externalizable e = (Externalizable)input.readObject();
			if (e instanceof IpmemsGzippedExternalizable)
				e = ((IpmemsGzippedExternalizable)e).getObject();
			return (IpmemsRataRq)e;
		} catch (EOFException x) {
			return null;
		} catch (InterruptedIOException x) {
			return null;
		} catch (Exception x) {
			IpmemsLoggers.warning(srv.getLogName(), "{0} In", x, this);
			return null;
		}
	}
	
	/**
	 * Writes an object into the OUT-queue.
	 * @param o An object.
	 */
	public void out(final Externalizable o) {ex.submit(new Runnable() {
		@Override
		public void run() {try {
			output.writeObject(gzipped ?new IpmemsGzippedExternalizable(o) : o);
			output.flush();
		} catch (Exception x) {
			IpmemsLoggers.warning(srv.getLogName(), "{0} Out", x, this);
		}}		
	});}
	
	@Override
	public void close() throws IOException {
		monitorLog(false);
		ex.shutdown();
		for (Handler h: hs) h.close();
		hs.clear();
		clientMap.clear();
		if (ps != null) ps.close();
		interpreter.close();
	}

	/**
	 * Get the gzipped state.
	 * @return Gzipped state.
	 */
	public boolean isGzipped() {
		return gzipped;
	}

	/**
	 * Sets the gzipped state.
	 * @param zs Gzipped state.
	 */
	public void setGzipped(boolean zs) {
		gzipped = zs;
	}

	@Override
	public String toString() {
		return socket + ":" + srv.getPort();
	}

	private final IpmemsRataServer srv;
	private final IpmemsScriptEngine engine;
	private final IpmemsInterpreter interpreter;
	private final IpmemsRataOutputStream mstream;
	private final Map clientMap;
	private final Socket socket;
	private final ObjectInput input;
	private final ObjectOutput output;
	private final ThreadPoolExecutor ex;
	private final Collection<Handler> hs = new ConcurrentLinkedQueue<Handler>();
	private final PrintStream ps;
	private volatile boolean strMode;
	private volatile boolean gzipped;
	private volatile boolean oldState;
	
	private final class Handler extends IpmemsLogAbstractHandler {
		public Handler(IpmemsLogEventData d) {
			data = d;
			data.getLogger().addHandler((IpmemsLogHandler)this);
		}

		public IpmemsLogEventData getData() {
			return data;
		}

		@Override
		public boolean publish(IpmemsLogRec r) {
			if (!super.publish(r)) return false; else {
				out(new IpmemsRataLogRecord(data.getLogger().getKey(), r));
				return true;
			}
		}

		@Override
		public IpmemsLogRec[] publish(IpmemsLogRec[] records) {
			IpmemsLogRec[] rs = super.publish(records);
			out(new IpmemsRataLogRecords(data.getLogger().getKey(), rs));
			return rs;
		}
		
		public String getKey() {
			return data.getLogger().getKey();
		}
		
		@Override
		public void close() {
			data.getLogger().removeHandler(this);
		}
		
		private final IpmemsLogEventData data;
	}
}
