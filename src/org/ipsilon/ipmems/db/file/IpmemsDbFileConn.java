package org.ipsilon.ipmems.db.file;

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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * IPMEMS database file store connection.
 * @author Dmitry ovchinnikov
 */
public class IpmemsDbFileConn implements Closeable {
	/**
	 * Constructs the file store.
	 * @param dir Store directory.
	 */
	public IpmemsDbFileConn(File dir) {
		directory = dir;
		if (!directory.exists() && !directory.mkdirs())
			throw new IllegalStateException("Base directory creation error");
		File dataStoreDir = new File(directory, "dataStore");
		if (!dataStoreDir.exists()) dataStoreDir.mkdir();
		String[] vs = {"current", "report", "tmp"};
		for (String v: vs) {
			File vd = new File(dataStoreDir, v);
			if (!vd.exists()) vd.mkdir();
		}
		File loggingDir = new File(directory, "logging");
		if (!loggingDir.exists()) loggingDir.mkdir();
		fm.put("dataStore", dataStoreDir);
		fm.put("logging", loggingDir);
	}
	
	/**
	 * Get the store directory.
	 * @return Store directory.
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * Get the special directory.
	 * @param key Directory key.
	 * @return Directory.
	 */
	public File dir(String key) {
		return fm.get(key);
	}
	
	@Override
	public void close() throws IOException {
	}
		
	/**
	 * Calls the code synchronously.
	 * @param <T> Return type.
	 * @param dir Directory.
	 * @param c Code.
	 * @return Result object.
	 * @throws IOException An I/O exception.
	 */
	public <T> T call(String dir, Callable<T> c) throws IOException {
		synchronized(fm.get(dir)) {
			try {
				return c.call();
			} catch (Exception x) {
				if (x instanceof IOException) throw (IOException)x;
				else throw new IOException(x);
			}
		}
	}
			
	private final File directory;
	private final HashMap<String,File> fm = new HashMap<String,File>(4);
}
