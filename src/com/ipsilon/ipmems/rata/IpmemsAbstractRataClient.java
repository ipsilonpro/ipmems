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

import com.ipsilon.ipmems.IpmemsSecureTcpClient;
import com.ipsilon.ipmems.rata.data.IpmemsRataRq;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;
import org.ipsilon.ipmems.data.IpmemsThrowableData;
import org.ipsilon.ipmems.util.IpmemsGzippedExternalizable;

/**
 * IPMEMS abstract RATA client.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsAbstractRataClient extends
		IpmemsSecureTcpClient implements IpmemsRataClient {
	/**
	 * Constructs default abstract RATA client.
	 * @throws IOException An I/O exception.
	 */
	public IpmemsAbstractRataClient() {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the abstract RATA client with custom properties.
	 * @param props Custom properties.
	 * @throws IOException An I/O exception.
	 */
	public IpmemsAbstractRataClient(Map props) {
		super(props);
	}

	@Override
	public boolean connect() {
		if (!containsKey("port")) put("port", 23111);
		try {
			boolean c = super.connect();
			if (!c) throw new IllegalStateException();
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			return true;
		} catch (Exception x) {
			printError("Connection error", x);
			try {
				close();
			} catch (Exception y) {}
			return false;
		}
	}
	
	@Override
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}
	
	@Override
	public <T extends Externalizable> T read(Class<T> c) throws Exception {
		Object o = input.readObject();
		if (o instanceof IpmemsGzippedExternalizable)
			o = ((IpmemsGzippedExternalizable)o).getObject();
		if (o instanceof IpmemsThrowableData) {
			printError("Remote error", ((IpmemsThrowableData)o).getData());
			return null;
		} else return c.cast(o);
	}
	
	/**
	 * Writes the object.
	 * @param o A request.
	 * @throws IOException An I/O exception. 
	 */
	@Override
	public synchronized void write(IpmemsRataRq o) throws IOException {
		output.writeObject(gzipped ? new IpmemsGzippedExternalizable(o) : o);
		output.flush();
	}

	@Override
	public void close() {
		if (output != null) try {
			output.close();
		} catch (Exception x) {}
		if (input != null) try {
			input.close();
		} catch (Exception x) {}
		try {
			socket.close();
		} catch (Exception x) {
			printError("Closing error", x);
		}
	}

	@Override
	public boolean isGzipped() {
		return gzipped;
	}

	@Override
	public void setGzipped(boolean gz) {
		gzipped = gz;
	}
	
	/**
	 * Object output.
	 */
	protected volatile ObjectOutputStream output;
	
	/**
	 * Object input.
	 */
	protected volatile ObjectInputStream input;
	
	/**
	 * Gzipped state.
	 */
	protected volatile boolean gzipped;
}
