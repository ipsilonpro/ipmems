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
import java.io.EOFException;
import java.io.Externalizable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.data.IpmemsIntData;
import org.ipsilon.ipmems.data.IpmemsMapData;
import org.ipsilon.ipmems.data.IpmemsThrowableData;
import org.ipsilon.ipmems.password.IpmemsConsolePasswordInput;
import org.ipsilon.ipmems.util.IpmemsGzippedExternalizable;

/**
 * IPMEMS console RATA client.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsConsoleRataClient extends IpmemsAbstractRataClient {
	/**
	 * Default constructor.
	 */
	public IpmemsConsoleRataClient() {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the console RATA client.
	 * @param props Client properties.
	 */
	public IpmemsConsoleRataClient(Map props) {
		super(props);
	}
		
	@Override
	public void printError(String m, Throwable t, Object... args) {
		System.err.println(IpmemsIntl.message(m, args));
		if (t != null) t.printStackTrace(System.err);
	}

	@Override
	public void printMessage(String msg, Object... args) {
		if (args == null) System.out.println(IpmemsIntl.message(msg));
		else System.out.println(IpmemsIntl.message(msg, args));
	}

	@Override
	public void print(IpmemsRataRsErrResult r, long dur) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		r.getObject().printStackTrace(pw);
		pw.close();
		printMessage("[{0} | {1} ms] {2}", r.getDuration(), dur, sw.toString());
	}

	@Override
	public void print(IpmemsRataRsResult r, long dur) {
		printMessage("[{0} | {1} ms] {2}", r.getDuration(), dur, r.getObject());
	}

	@Override
	public void printBin(byte[] data) {
		System.out.write(data, 0, data.length);
	}

	@Override
	public String getKey() {
		return "console";
	}
	
	private void printInfo(Map sm) {
		String ver = String.valueOf(sm.get("logo"));
		printMessage("Remote system: {0}", ver);
		printMessage("---");
	}
	
	@Override
	public boolean connect() {
		if (!super.connect()) return false; else try {
			IpmemsMapData sm = read(IpmemsMapData.class);
			if (sm == null) return false;
			printInfo(sm.getData());
			IpmemsMapData cmd = new IpmemsMapData();
			cmd.setData(Ipmems.getMap());
			output.writeObject(cmd);
			output.flush();
			IpmemsConsolePasswordInput pi = new IpmemsConsolePasswordInput();
			output.writeObject(new IpmemsRataAuthData(pi));
			output.flush();
			IpmemsMapData md = read(IpmemsMapData.class);
			if (md == null) return false;
			printMessage("Remote engine list: ");
			int i = 0;
			for (Object o: md.getData().values())
				printMessage("{0}. {1}", ++i, o);
			printMessage("---");
			String eis = System.console().readLine("Engine [1]: ").trim();
			i = eis.isEmpty() ? 0 : Integer.decode(eis) - 1;
			IpmemsIntData id = new IpmemsIntData();
			id.setData(i);
			output.writeObject(id);
			output.flush();
			printMessage("---");
			output.writeObject(new IpmemsRataRqStrMode(true));
			output.flush();
			return true;
		} catch (Exception x) {
			try {
				printMessage("{0}", input.readObject());
			} catch (Exception xx) {
				printError("Remote error", xx);
			}
			printError("Connection error", x);
			return false;
		}
	}

	@Override
	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.print("> ");
					while (true) {
						Externalizable o = (Externalizable)input.readObject();
						if (o instanceof IpmemsGzippedExternalizable)
							o = ((IpmemsGzippedExternalizable)o).getObject();
						long dur = System.currentTimeMillis() - last;
						if (o instanceof IpmemsRataRsErrResult) {
							print((IpmemsRataRsErrResult)o, dur);
							System.out.print("> ");
						} else if (o instanceof IpmemsRataRsResult) {
							print((IpmemsRataRsResult)o, dur);
							System.out.print("> ");
						} else if (o instanceof IpmemsRataBinaryData) {
							printBin(((IpmemsRataBinaryData)o).getData());
						} else if (o instanceof IpmemsThrowableData) {
							Throwable t = ((IpmemsThrowableData)o).getData();
							printError("Error", t);
						}
					}
				} catch (EOFException x) {
					printMessage("Closed by remote side");
				} catch (Exception x) {
					printMessage("Bye");
				}
			}
		}).start();
		try {
			while (true) {
				String line = System.console().readLine();
				if (line == null || "exit".equals(line)) break;
				if (!socket.isClosed()) {
					last = System.currentTimeMillis();
					write(new IpmemsRataRqCmd(line));
				}
			}
			close();
		} catch (Exception x) {
			printError("Console error", x);
		}
	}
	
	private volatile long last;
}
