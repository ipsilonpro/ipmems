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

import java.util.Collections;
import java.util.Map;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.password.IpmemsConsolePasswordInput;

/**
 * IPMEMS console RCLI client.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsConsoleRcliClient extends IpmemsAbstractRcliClient {
	/**
	 * Default constructor.
	 */
	public IpmemsConsoleRcliClient() {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the console RCLI client.
	 * @param ps Client properties.
	 */
	public IpmemsConsoleRcliClient(Map ps) {
		super(ps);
	}

	@Override
	public void printMessage(String msg, Object... args) {
		System.out.println(IpmemsIntl.message(msg, args));
	}

	@Override
	public void printError(String msg, Throwable t, Object... args) {
		System.err.println(IpmemsIntl.message(msg, args));
		if (t != null) t.printStackTrace(System.err);
	}

	@Override
	public boolean connect() {
		if (!super.connect()) return false; else try {
			IpmemsConsolePasswordInput pi = new IpmemsConsolePasswordInput();
			String user = pi.getUser();
			char[] password = pi.getPassword();
			printStream.println(true);
			printStream.println(user);
			printStream.println(password);
			return true;
		} catch (Exception x) {
			printMessage("Authentication error on {0}", this);
			try {
				close();
			} catch (Exception y) {}
			return false;
		}
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String msg = "Bye";
				try {
					while (true) {
						int l = reader.read();
						if (l < 0) {
							msg = "Closed by remote side.";
							break;
						}
						System.out.print((char)l);
						System.out.flush();
					}
				} catch (Exception x) {
				} finally {
					printMessage(msg);
				}
			}
		}).start();
		while (true) {
			String line = System.console().readLine();
			if (line == null || "exit".equals(line)) break;
			if (isConnected()) printStream.println(line);
		}
		try {
			close();
		} catch (Exception x) {}
	}

	@Override
	public String getKey() {
		return "console";
	}	
}
