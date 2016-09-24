package org.ipsilon.ipmems.dbfi;

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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.net.IpmemsAbstractTcpClient;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS DBFI client.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDbfiClient extends 
		IpmemsAbstractTcpClient implements IpmemsDbfiClientItf {
	/**
	 * Default constructor.
	 */
	public IpmemsDbfiClient() throws Exception {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the client and connects to the remote server.
	 * @param props Client properties.
	 */
	public IpmemsDbfiClient(Map props) throws Exception {
		super(props);
	}
	
	@Override
	public void init(Object... args) {
		super.init(args);
	}
	
	@Override
	public final boolean connect() throws IOException {
		if (containsKey("dbfi")) try {
			dbfi = IpmemsScriptEngines.<IpmemsDbfi>
					loadClass(get(String.class, "dbfi")).newInstance();
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "{0} DBFI error", x, this);
			return false;
		} else dbfi = new IpmemsDbfiGzippedExt();
		boolean c = super.connect();
		if (c) {
			inputStream = new DataInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
			return true;
		} else return false;
	}
	
	@Override
	public void sendClientMap(Map props) throws IOException {
		Properties p = new Properties();
		for (Object k: props.keySet())
			p.setProperty(String.valueOf(k), String.valueOf(props.get(k)));
		p.setProperty("encoding", "UTF-8");
		if (!(dbfi instanceof IpmemsDbfiGzippedExt))
			p.setProperty("dbfi", dbfi.getClass().getName());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		p.store(bos, "");
		outputStream.writeInt(bos.size());
		outputStream.write(bos.toByteArray());
		outputStream.flush();
		int c = inputStream.read();
		if (c <= 0) throw new IllegalStateException("Auth error");
		c = inputStream.read();
		if (c <= 0) throw new IllegalStateException("Connection error");
	}
	
	@Override
	public Map query(Map data) throws Exception {
		byte[] d = dbfi.encodeMap(data);
		outputStream.writeInt(d.length);
		outputStream.write(d);
		outputStream.flush();
		dbfi.setEncoding("UTF-8");
		d = new byte[inputStream.readInt()];
		inputStream.readFully(d);
		return dbfi.decodeMap(d);
	}
	
	@Override
	public void close() throws IOException {
		try {
			outputStream.close();
		} catch (Exception x) {
		} finally {
			outputStream = null;
		}
		try {
			inputStream.close();
		} catch (Exception x) {
		} finally {
			inputStream = null;
		}
		super.close();
	}

	@Override
	public void disconnect() throws IOException {
		outputStream.writeInt(-1);
		outputStream.flush();
	}

	@Override
	public String getKey() {
		return "dbfi";
	}
	
	/**
	 * Current DBFI.
	 */
	protected IpmemsDbfi dbfi;
	
	/**
	 * Last error.
	 */
	protected Exception lastError;
	
	/**
	 * Input stream.
	 */
	protected DataInputStream inputStream;
	
	/**
	 * Output stream.
	 */
	protected DataOutputStream outputStream;
}
