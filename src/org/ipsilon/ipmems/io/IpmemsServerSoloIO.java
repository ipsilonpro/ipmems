package org.ipsilon.ipmems.io;

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

import java.io.IOException;
import java.util.Map;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS solo server I/O object.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsServerSoloIO extends 
		IpmemsPropertized implements IpmemsServerIO {
	/**
	 * Default constructor.
	 */
	public IpmemsServerSoloIO() {
	}
	
	/**
	 * Constructs the 
	 * @param props 
	 */
	public IpmemsServerSoloIO(Map props) {
		super(props);
	}

	@Override
	public Object getServerObject() {
		return this;
	}

	@Override
	public void init() throws IOException {
	}

	@Override
	public boolean isMultiClient() {
		return false;
	}

	@Override
	public void restart() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public IpmemsIO call() throws Exception {
		Object cl = get("ioClass");
		IpmemsIO io;
		if (cl instanceof String) {
			Class<?> c = IpmemsScriptEngines.loadClass((String)cl);
			io = (IpmemsIO)c.newInstance();
		} else if (cl instanceof Class<?>) {
			io = (IpmemsIO)((Class<?>)cl).newInstance();
		} else io = null;
		if (io != null) io.getMap().putAll(getMap());
		io.connect();
		return io;
	}
}
