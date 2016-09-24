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

import com.ipsilon.ipmems.rata.data.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.data.IpmemsIntData;
import org.ipsilon.ipmems.data.IpmemsMapData;
import org.ipsilon.ipmems.data.IpmemsThrowableData;
import org.ipsilon.ipmems.logging.IpmemsLogEventData;
import org.ipsilon.ipmems.logging.IpmemsLoggersListener;
import org.ipsilon.ipmems.logging.IpmemsRemoteItf;
import org.ipsilon.ipmems.swingmems.IpmemsGuiPasswordInput;
import org.ipsilon.ipmems.swingmems.IpmemsSwingUtil;
import org.ipsilon.ipmems.util.IpmemsAdm;
import org.ipsilon.ipmems.util.IpmemsFileNavigator;
import org.ipsilon.ipmems.util.IpmemsGzippedExternalizable;

/**
 * IPMEMS RATA GUI client.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsGuiRataClient extends
		IpmemsAbstractRataClient implements
		IpmemsRemoteItf, IpmemsAdm, Runnable {
	/**
	 * Default constructor.
	 */
	public IpmemsGuiRataClient() {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the RATA GUI client.
	 * @param props Client properties.
	 */
	public IpmemsGuiRataClient(Map props) {
		super(props);
	}

	@Override
	public void printBin(byte[] data) {
		frame.getOutput().append(data);
	}

	@Override
	public void print(IpmemsRataRsErrResult r, long dur) {
		frame.getResults().addResult(r, dur);
	}

	@Override
	public void print(IpmemsRataRsResult r, long dur) {
		frame.getResults().addResult(r, dur);
	}

	@Override
	public void printMessage(String msg, Object... args) {
		String txt = IpmemsIntl.message(msg, args);
		frame.getResults().addTextResult(txt);
	}

	@Override
	public void printError(String msg, Throwable t, Object... args) {
		String txt = IpmemsIntl.message(msg, args);
		frame.getResults().addErrorResult(txt, t);
	}

	@Override
	public String getKey() {
		return "gui";
	}

	@Override
	public Map<String,Set<String>> getMethodMap() {
		try {
			write(new IpmemsRataRqMm());
			IpmemsRataRs rs = read();
			if (rs instanceof IpmemsRataRsErr)
				throw new IllegalStateException(
						((IpmemsRataRsErr)rs).getThrown());
			else if (rs instanceof IpmemsRataRsMm)
				return ((IpmemsRataRsMm)rs).getMethodMap();
			else throw new UnsupportedOperationException();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}

	@Override
	public Object invoke(String obj, String method) throws Exception {
		try {
			write(new IpmemsRataRqInvokeMethod(obj, method));
			IpmemsRataRs r = read();
			if (r instanceof IpmemsRataRsErr)
				throw new IllegalStateException(
						((IpmemsRataRsErr)r).getThrown());
			else if (r instanceof IpmemsRataRsInvokeMethod)
				return ((IpmemsRataRsInvokeMethod)r).getResult();
			else throw new UnsupportedOperationException();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}
	
	/**
	 * Get the associated frame.
	 * @return Associated frame.
	 */
	public IpmemsGuiRataFrame getFrame() {
		return frame;
	}
	
	private int selectEngine(final Object[] engines) throws Exception {
		final AtomicInteger ai = new AtomicInteger();
		IpmemsSwingUtil.invoke(new Runnable() {
			@Override
			public void run() {	
				IpmemsRataEngDialog d = new IpmemsRataEngDialog(frame, engines);
				d.setVisible(true);
				ai.set(d.getValueIndex());
			}
		});
		return ai.get();
	}
	
	private void status(String text, Object ... args) {
		frame.status(IpmemsIntl.message(text, args));
	}
	
	@Override
	public boolean connect() {
		try {
			IpmemsSwingUtil.invoke(new Runnable() {
				@Override
				public void run() {
					frame = new IpmemsGuiRataFrame(IpmemsGuiRataClient.this);
					frame.setVisible(true);
				}
			});
			status("Connecting...");
			Thread.sleep(1000L);
		} catch (Exception x) {
			return false;
		}
		if (!super.connect()) return false; else try {
			status("Reading server data...");
			IpmemsMapData sm = read(IpmemsMapData.class);
			if (sm == null) return false;
			serverInfo = sm.getData();
			status("Writing client data...");
			IpmemsMapData cmd = new IpmemsMapData();
			cmd.setData(Ipmems.getMap());
			output.writeObject(cmd);
			output.flush();
			IpmemsGuiPasswordInput pi = new IpmemsGuiPasswordInput();
			status("Writing auth data...");
			output.writeObject(new IpmemsRataAuthData(pi));
			output.flush();
			status("Reading scripting data...");
			final IpmemsMapData md = read(IpmemsMapData.class);
			if (md == null) return false;
			Object[] mimes = md.getData().keySet().toArray();
			Object[] names = md.getData().values().toArray();
			int i = names.length == 1 ? 0 : selectEngine(names);
			if (i < 0) return false;
			mime = mimes[i].toString();
			frame.getCmdLine().setMime(mime);
			status("Writing scripting data...");
			IpmemsIntData id = new IpmemsIntData();
			id.setData(i);
			output.writeObject(id);
			output.flush();
			frame.getCmdLine().enableInput(true);
			frame.hideStatus();
			return true;
		} catch (Exception x) {
			status("Error");
			printError("Connection error", x);
			try {
				printMessage("{0}", input.readObject());
			} catch (Exception xx) {
				printError("Remote error", xx);
			}
			return false;
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				Externalizable e = read(Externalizable.class);
				if (e instanceof IpmemsGzippedExternalizable)
					e = ((IpmemsGzippedExternalizable)e).getObject();
				if (e instanceof IpmemsRataRs) q.put((IpmemsRataRs)e);
				else if (e instanceof IpmemsThrowableData)
					printError("", ((IpmemsThrowableData)e).getData());
				else if (e instanceof IpmemsRataLogData) {
					IpmemsRataLogData d = (IpmemsRataLogData)e;
					if (d.state()) {
						IpmemsRataLogger lg = new IpmemsRataLogger(d);
						loggers.put(d.getKey(), lg);
						for (IpmemsLoggersListener l: ls)
							l.added(new IpmemsLogEventData(l, lg));
					} else {
						IpmemsRataLogger lg = loggers.remove(d.getKey());
						if (lg != null) for (IpmemsLoggersListener l: ls)
							l.removed(new IpmemsLogEventData(l, lg));
						lg.close();
					}
				} else if (e instanceof IpmemsRataLogRecord) {
					IpmemsRataLogRecord rd = (IpmemsRataLogRecord)e;
					IpmemsRataLogger lg = loggers.get(rd.getKey());
					if (lg != null) lg.log(rd.getRecord());
				} else if (e instanceof IpmemsRataBinaryData) {
					printBin(((IpmemsRataBinaryData)e).getData());
				} else if (e instanceof IpmemsRataLogRecords) {
					IpmemsRataLogRecords rs = (IpmemsRataLogRecords)e;
					IpmemsRataLogger lg = loggers.get(rs.getKey());
					if (lg != null) lg.log(rs.getRecords());
				}
			}
		} catch (Exception x) {
			printError("I/O exception", x);
			frame.getCmdLine().enableInput(false);
		}
	}

	@Override
	public void start() {
		new Thread(this).start();
	}

	@Override
	public IpmemsFileNavigator getFileInterface() {
		return new IpmemsRataFileNavigator(this);
	}

	@Override
	public String getMainLogText() throws IOException {
		write(new IpmemsRataRqFile("@{jarDir}/ipmems.log"));
		try {
			IpmemsRataRs r = read();
			if (r instanceof IpmemsRataRsFile)
				return ((IpmemsRataRsFile)r).getContents();
			else if (r instanceof IpmemsRataRsErr) {
				printError("Main log error", ((IpmemsRataRsErr)r).getThrown());
				return "";
			} else return "--";
		} catch (Exception x) {
			printError("Main log error", x);
			return "";
		}
	}

	@Override
	public void addListener(IpmemsLoggersListener l) {
		ls.add(l);
	}

	@Override
	public void removeListener(IpmemsLoggersListener l) {
		ls.remove(l);
	}
	
	@Override
	public void log(boolean state) {
		try {
			write(new IpmemsRataRqLog(state));
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}
	
	/**
	 * Sets the STR mode state.
	 * @param state STR mode state.
	 */
	public void strMode(boolean state) {
		try {
			write(new IpmemsRataRqStrMode(state));
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}
	
	/**
	 * Sets the gzipped state.
	 * @param state Gzipped state.
	 */
	public void gzipped(boolean state) {
		try {
			write(new IpmemsRataRqGzipped(state));
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}
		
	/**
	 * Reads a response.
	 * @return Response.
	 * @throws IOException Any exception. 
	 */
	public IpmemsRataRs read() throws IOException {
		try {
			return q.take();
		} catch (InterruptedException x) {
			throw new InterruptedIOException(x.getMessage());
		}
	}
	
	/**
	 * Get the selected MIME.
	 * @return Selected MIME.
	 */
	public String getMime() {
		return mime;
	}

	/**
	 * Get the server info.
	 * @return Server info.
	 */
	public Map getServerInfo() {
		return serverInfo;
	}
		
	private IpmemsGuiRataFrame frame;
	private String mime;
	private Map serverInfo;
	private ConcurrentLinkedQueue<IpmemsLoggersListener> ls =
			new ConcurrentLinkedQueue<IpmemsLoggersListener>();
	private Map<String,IpmemsRataLogger> loggers =
			new TreeMap<String,IpmemsRataLogger>();
	private SynchronousQueue<IpmemsRataRs> q = 
			new SynchronousQueue<IpmemsRataRs>();
}
