package org.ipsilon.ipmems;

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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.ipsilon.ipmems.db.IpmemsDbAddress;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.prot.IpmemsProtBinding;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsCollections;
import org.ipsilon.ipmems.util.IpmemsDynInvoke;
import org.ipsilon.ipmems.util.IpmemsFile;

/**
 * IPMEMS library object.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLib {
	/**
	 * Get the user objects.
	 * @param c User object class.
	 * @param f Classes list file.
	 * @return User objects.
	 * @throws IOException An I/O exception.
	 */
	public static <T> List<T> ol(Class<T> c, File f) throws IOException {
		BufferedReader r = null;
		ArrayList<T> l = new ArrayList<T>();
		try {
			r = new BufferedReader(
					new InputStreamReader(new FileInputStream(f), "UTF-8"));
			while (true) {
				String line = r.readLine();
				if (line == null) break;
				line = line.trim();
				if (line.startsWith("#")) continue;
				try {
					Class<T> cls = IpmemsScriptEngines.loadClass(line);
					l.add(c.cast(cls.newInstance()));
				} catch (Exception x) {}
			}
		} finally {
			if (r != null) r.close();
			return l;
		}
	}
	
	/**
	 * Get the user objects.
	 * @param c User object class.
	 * @param url Classes list URL.
	 * @return User objects.
	 * @throws IOException An I/O exception.
	 */
	public static <T> List<T> ol(Class<T> c, URL url) throws IOException {
		BufferedReader r = null;
		ArrayList<T> l = new ArrayList<T>();
		try {
			r = new BufferedReader(
					new InputStreamReader(url.openStream(), "UTF-8"));
			while (true) {
				String line = r.readLine();
				if (line == null) break;
				line = line.trim();
				if (line.startsWith("#")) continue;
				try {
					Class<T> cls = IpmemsScriptEngines.loadClass(line);
					l.add(c.cast(cls.newInstance()));
				} catch (Exception x) {}
			}
		} finally {
			if (r != null) r.close();
			return l;
		}
	}
	
	/**
	 * Get the user objects map.
	 * @param c User object class.
	 * @param f Classes list file.
	 * @return User object map.
	 * @throws IOException An I/O exception.
	 */
	public static Map<String,Object> om(Class<?> c, File f) throws IOException {
		BufferedReader r = null;
		Map<String,Object> m = new LinkedHashMap<String,Object>();
		try {
			r = new BufferedReader(
					new InputStreamReader(new FileInputStream(f), "UTF-8"));
			while (true) {
				String line = r.readLine();
				if (line == null) break;
				line = line.trim();
				if (line.startsWith("#")) continue;
				try {
					Class<?> cls = IpmemsScriptEngines.loadClass(line);
					m.put(line, c.cast(cls.newInstance()));
				} catch (Exception x) {
					m.put(line, x);
				}
			}
		} finally {
			if (r != null) r.close();
			return m;
		}
	}
	
	/**
	 * Get the user objects map.
	 * @param c User object class.
	 * @param u Classes list URL.
	 * @return User objects map.
	 * @throws IOException An I/O exception.
	 */
	public static Map<String,Object> om(Class<?> c, URL u) throws IOException {
		BufferedReader r = null;
		Map<String,Object> m = new LinkedHashMap<String,Object>();
		try {
			r = new BufferedReader(
					new InputStreamReader(u.openStream(), "UTF-8"));
			while (true) {
				String line = r.readLine();
				if (line == null) break;
				line = line.trim();
				if (line.startsWith("#")) continue;
				try {
					Class<?> cls = IpmemsScriptEngines.loadClass(line);
					m.put(line, c.cast(cls.newInstance()));
				} catch (Exception x) {
					m.put(line, x);
				}
			}
		} finally {
			if (r != null) r.close();
			return m;
		}
	}
	
	/**
	 * Get the driver map.
	 * @param c User object class.
	 * @param url Drivers url.
	 * @param libs Libraries.
	 * @return Drivers map.
	 * @throws IOException An I/O exception.
	 */
	public static Map<String,Object> om(
			Class<?> c, URL url, URL ... libs) throws IOException {
		BufferedReader r = null;
		Map<String,Object> m = new LinkedHashMap<String,Object>();
		URLClassLoader cl = new URLClassLoader(libs);
		try {
			r = new BufferedReader(
					new InputStreamReader(url.openStream(), "UTF-8"));
			while (true) {
				String line = r.readLine();
				if (line == null) break;
				line = line.trim();
				if (line.startsWith("#")) continue;
				try {
					Class<?> cls = cl.loadClass(line);
					m.put(line, c.cast(cls.newInstance()));
				} catch (Exception x) {
					m.put(line, x);
				}
			}
		} finally {
			if (r != null) r.close();
			return m;
		}
	}
	
	/**
	 * Get the file.
	 * @param dir Directory.
	 * @param name File name.
	 * @return A file.
	 */
	public static File f(String dir, String name) {
		return new File(Ipmems.substituted(dir), Ipmems.substituted(name));
	}
	
	/**
	 * Get the file.
	 * @param path File path.
	 * @return A file.
	 */
	public static File f(String path) {
		return new File(Ipmems.substituted(path));
	}
	
	/**
	 * Get the script engines class.
	 * @return Script engines class.
	 */
	public static Class<IpmemsScriptEngines> ses() {
		return IpmemsScriptEngines.class;
	}
	
	/**
	 * Get the script engines.
	 * @return Script engines.
	 */
	public static Class<IpmemsScriptEngines> getSes() {
		return ses();
	}
	
	/**
	 * Get the IPMEMS strings class.
	 * @return IPMEMS strings class.
	 */
	public static Class<IpmemsStrings> strs() {
		return IpmemsStrings.class;
	}
	
	/**
	 * Get the IPMEMS strings class.
	 * @return IPMEMS strings class.
	 */
	public static Class<IpmemsStrings> getStrs() {
		return strs();
	}
	
	/**
	 * Get the IPMEMS utilities class.
	 * @return IPMEMS utilities class.
	 */
	public static Class<IpmemsUtil> u() {
		return IpmemsUtil.class;
	}
	
	/**
	 * Get the utilities class.
	 * @return Utilities class.
	 */
	public static Class<IpmemsUtil> getU() {
		return u();
	}
	
	/**
	 * Get the IPMEMS intl class.
	 * @return IPMEMS intl class.
	 */
	public static Class<IpmemsIntl> intl() {
		return IpmemsIntl.class;
	}
	
	/**
	 * Get the IPMEMS intl class.
	 * @return Intl class.
	 */
	public static Class<IpmemsIntl> getIntl() {
		return intl();
	}
	
	/**
	 * Get the IPMEMS collections class.
	 * @return IPMEMS collections class.
	 */
	public static Class<IpmemsCollections> cs() {
		return IpmemsCollections.class;
	}
	
	/**
	 * Get the collections class.
	 * @return Collections class.
	 */
	public static Class<IpmemsCollections> getCs() {
		return cs();
	}
	
	/**
	 * Get the IPMEMS dyn invoke class.
	 * @return IPMEMS dyn invoke class.
	 */
	public static Class<IpmemsDynInvoke> di() {
		return IpmemsDynInvoke.class;
	}
	
	/**
	 * Get the dyn invoke class.
	 * @return Dyn invoke class.
	 */
	public static Class<IpmemsDynInvoke> getDi() {
		return di();
	}
	
	/**
	 * Get the file utilities class.
	 * @return File utilities class.
	 */
	public static Class<IpmemsFile> fu() {
		return IpmemsFile.class;
	}
	
	/**
	 * Get the file utilities class.
	 * @return File utilities class.
	 */
	public static Class<IpmemsFile> getFu() {
		return fu();
	}
	
	/**
	 * Loads the class.
	 * @param name Class name.
	 * @return Loaded class.
	 * @throws Exception Any exception.
	 */
	public static Class<?> load(String name) throws Exception {
		return IpmemsScriptEngines.loadClass(name);
	}
	
	/**
	 * Get the IPMEMS loggers.
	 * @return IPMEMS loggers.
	 */
	public static Class<IpmemsLoggers> ls() {
		return IpmemsLoggers.class;
	}
	
	/**
	 * Get the IPMEMS loggers class.
	 * @return IPMEMS loggers class.
	 */
	public static Class<IpmemsLoggers> getLs() {
		return ls();
	}
	
	/**
	 * Get the IPMEMS monitor.
	 * @return IPMEMS monitor.
	 */
	public static Ipmems getMonitor() {
		return Ipmems.MONITOR;
	}
	
	/**
	 * Get the DB address utility class.
	 * @return DB address utility class.
	 */
	public static Class<IpmemsDbAddress> da() {
		return IpmemsDbAddress.class;
	}

	/**
	 * Get the DB address utility class.
	 * @return DB address utility class.
	 */
	public static Class<IpmemsDbAddress> getDa() {
		return da();
	}
	
	/**
	 * Get the numeric id.
	 * @param id String id.
	 * @return Numeric id.
	 */
	public static long id(String id) {
		return IpmemsDbAddress.decode(id);
	}
	
	/**
	 * Get the protocol library class.
	 * @return Protocol library class.
	 */
	public static Class<IpmemsProtBinding> getProt() {
		return IpmemsProtBinding.class;
	}
		
	/**
	 * Get the user map.
	 * @return User map.
	 */
	public static Map<String,Object> getUserMap() {
		return IpmemsScriptEngines.userMap;
	}
	
	/**
	 * Executes the file.
	 * @param file File.
	 * @return Result.
	 * @throws Exception An exception.
	 */
	public static Object ef(String file) throws Exception {
		String sfile = Ipmems.substituted(file);
		String ext = IpmemsStrings.lastPart(sfile, '.');
		IpmemsScriptEngine e = IpmemsScriptEngines.getEngineByScriptExt(ext);
		IpmemsInterpreter i = null;
		try {
			i = e.makeInterpreter();
			return i.eval(new File(sfile));
		} catch (Exception x) {
			throw x;
		} finally {
			if (i != null) i.close();
		}
	}
	
	/**
	 * Executes the file in the main interpreter.
	 * @param file A file.
	 * @return Result.
	 * @throws Exception An exception.
	 */
	public static Object lef(String file) throws Exception {
		String sfile = Ipmems.substituted(file);
		String ext = IpmemsStrings.lastPart(sfile, '.');
		IpmemsScriptEngine e = IpmemsScriptEngines.getEngineByScriptExt(ext);
		try {
			return e.getMainInterpreter().eval(new File(sfile));
		} catch (Exception x) {
			throw x;
		}
	}
	
	/**
	 * Get the substituted system property.
	 * @param k Value key.
	 * @return Substituted system property.
	 */
	public static String sst(String k) {
		return Ipmems.sst(k);
	}
	
	/**
	 * Get the substituted system property.
	 * @param k System property key.
	 * @param d Default value.
	 * @return Substituted system property.
	 */
	public static String sst(String k, String d) {
		return Ipmems.sst(k, d);
	}
	
	/**
	 * Get the version as string.
	 * @return String version.
	 */
	public static String getVersion() {
		int v = Integer.parseInt(
				Ipmems.class.getPackage().getSpecificationTitle());
		return (v / 1000000) + "." + (v % 1000000 / 1000) + "." + (v % 1000);
	}
	
	/**
	 * Get the full version.
	 * @return Full version.
	 */
	public static String getFullVersion() {
		StringBuilder sb = new StringBuilder(getVersion());
		sb.append(':');
		sb.append(getBuild());
		sb.append(" (");
		sb.append(getBuildDate());
		sb.append(')');
		return sb.toString();
	}
	
	/**
	 * Get the IPMEMS logo.
	 * @return IPMEMS logo.
	 */
	public static String getLogo() {
		StringBuilder sb = new StringBuilder(
				Ipmems.class.getPackage().getImplementationTitle());
		sb.append(' ');
		sb.append(getFullVersion());
		return sb.toString();
	}
		
	/**
	 * Get the build number as string.
	 * @return Build number as string.
	 */
	public static String getBuild() {
		return Ipmems.class.getPackage().getSpecificationVersion();
	}
	
	/**
	 * Get the build date.
	 * @return Build date.
	 */
	public static String getBuildDate() {
		return Ipmems.class.getPackage().getImplementationVersion();
	}	
}
