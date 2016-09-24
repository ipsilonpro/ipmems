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

import com.ipsilon.ipmems.IpmemsSecureTcpClient;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;

/**
 * IPMEMS abstract RCLI client.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsAbstractRcliClient extends 
		IpmemsSecureTcpClient implements IpmemsRcliClient {
	/**
	 * Default constructor.
	 */
	public IpmemsAbstractRcliClient() {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the abstract RCLI client.
	 * @param ps Properties.
	 */
	public IpmemsAbstractRcliClient(Map ps) {
		super(ps);
	}

	@Override
	public boolean connect() {
		if (!containsKey("port")) put("port", 23666);
		printMessage("Connecting to {0}...", this);
		try {
			boolean c = super.connect();
			if (!c) throw new IllegalStateException();
			printStream = new PrintStream(
					socket.getOutputStream(), true, "UTF-8");
			reader = new InputStreamReader(socket.getInputStream(), "UTF-8");
			return true;
		} catch (Exception x) {
			printMessage("Unable to create the socket: {0}", x);
			try {
				close();
			} catch (Exception y) {}
			return false;
		}
	}

	@Override
	public void close() throws IOException {
		if (printStream != null) try {
			printStream.close();
		} catch (Exception x) {} finally {
			printStream = null;
		}
		if (reader != null) try {
			reader.close();
		} catch (Exception x) {} finally {
			reader = null;
		}
		super.close();
	}
	
	/**
	 * Print stream.
	 */
	public PrintStream printStream;	
	
	/**
	 * Reader.
	 */
	public Reader reader;
}
