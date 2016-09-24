package org.ipsilon.ipmems.res;

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

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * IPMEMS Binary File Resource Bundle.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsResBundle extends ResourceBundle {
	/**
	 * Constructs the resource bundle.
	 * @param ipr IPR-file.
	 * @throws IOException An I/O exception.
	 */
	public IpmemsResBundle(File ipr) throws IOException {
		data = new RandomAccessFile(ipr, "r");
	}
	
	private int size() {
		try {
			synchronized(data) {
				data.seek(0);
				return data.readInt();
			}
		} catch (Exception x) {
			return 0;
		}
	}
	
	private String getKey(int index) {
		try {
			synchronized(data) {
				data.seek(4 + index * 16);
				data.seek(data.readLong());
				return data.readUTF();
			}
		} catch (Exception x) {
			return null;
		}
	}
	
	private String getValue(int index) {
		try {
			synchronized(data) {
				data.seek(12 + index * 16);
				data.seek(data.readLong());
				return data.readUTF();
			}
		} catch (Exception x) {
			return null;
		}
	}
	
	@Override
	protected String handleGetObject(String key) {
		if (key == null) throw new NullPointerException();
		if (size() == 0) return null; else {
			int min = 0;
			int max = size() - 1;
			int idx = -1;
			while (min < max) {
				int m = (min + max) / 2;
				String k = getKey(m);
				if (k == null) break;
				if (k.compareTo(key) == 0) {
					idx = m;
					break;
				} else if (key.compareTo(k) > 0) min = m + 1;
				else max = m - 1;
			}
			return idx >= 0 ? getValue(idx) : null;
		}
	}

	@Override
	public Enumeration<String> getKeys() {
		return new Enumeration<String>() {
			@Override
			public boolean hasMoreElements() {
				return index >= 0 && index < size();
			}

			@Override
			public String nextElement() {
				try {
					synchronized(data) {
						data.seek(4 + index * 16);
						data.seek(data.readLong());
						String result = data.readUTF();
						index++;
						return result;
					}
				} catch (Exception x) {
					throw new IllegalStateException(x);
				}
			}
			
			private int index;
		};
	}
	
	/**
	 * Makes the binary resource from a properties file.
	 * @param f Properties file.
	 * @throws Exception An exception.
	 */
	public static void toBin(File f) throws Exception {
		if (!f.getName().endsWith(".properties"))
			throw new IllegalArgumentException("Not a properties file");
		Reader r = null;
		RandomAccessFile d = null;
		try {
			r = new InputStreamReader(new FileInputStream(f), "UTF-8");
			Properties p = new Properties();
			p.load(r);
			TreeMap<String,String> m = new TreeMap<String,String>();
			for (String k: p.stringPropertyNames())	m.put(k, p.getProperty(k));
			String[] ks = m.keySet().toArray(new String[m.size()]);
			File ipr = new File(f.getAbsoluteFile().getParentFile(),
					f.getName().replace(".properties", ".ipr"));
			d = new RandomAccessFile(ipr, "rw");
			d.writeInt(m.size());
			d.seek(4 + m.size() * 16);
			long[] ki = new long[m.size()];
			long[] vi = new long[m.size()];
			for (int i = 0; i < ks.length; i++) {
				ki[i] = d.getFilePointer();
				d.writeUTF(ks[i]);
				vi[i] = d.getFilePointer();
				d.writeUTF(m.get(ks[i]));
			}
			d.seek(4);
			for (int i = 0; i < ks.length; i++) {
				d.writeLong(ki[i]);
				d.writeLong(vi[i]);
			}
		} catch (Exception x) {
			throw x;
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
			if (d != null) try {d.close();} catch (Exception x) {}
		}
	}
	
	private final RandomAccessFile data;
}
