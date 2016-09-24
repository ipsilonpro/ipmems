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
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.*;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.json.IpmemsJsonUtil;
import org.ipsilon.ipmems.logging.IpmemsLogRec;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsCollections;
import org.ipsilon.ipmems.util.IpmemsDynInvoke;

/**
 * Main invoker class.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class Ipmems extends 
		IpmemsObservable implements Thread.UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		synchronized(System.err) {
			System.err.println(t);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Process the IPMEMS command-line.
	 * @param a Command-line arguments.
	 */
	public static void cmd(String[] a) {
		File f = new File(JAR_DIR, "ipmems.properties");
		InputStreamReader r = null;
		Properties props = new Properties();
		if (f.isFile() && f.canRead()) try {
			r = new InputStreamReader(new FileInputStream(f), "UTF-8");
			props.load(r);
		} catch (Exception x) {
			x.printStackTrace(System.err);
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
		for (String k: props.stringPropertyNames()) {
			if (k.isEmpty()) continue;
			if (k.endsWith("]")) {
				int i = k.lastIndexOf('[');
				String q = k.substring(0, i);
				String c = k.substring(i + 1, k.length() - 1);
				String v = props.getProperty(k);
				try {
					if (c.equals(boolean.class.getName()))
						p.put(q, Boolean.valueOf(v));
					else if (c.equals(int. class.getName()))
						p.put(q, Integer.decode(v));
					else if (c.equals(long.class.getName()))
						p.put(q, Long.decode(v));
					else if (c.equals(short.class.getName()))
						p.put(q, Short.decode(v));
					else if (c.equals(byte.class.getName()))
						p.put(q, Byte.decode(v));
					else if (c.equals(float.class.getName()))
						p.put(q, Float.valueOf(v));
					else if (c.equals(double.class.getName()))
						p.put(q, Double.valueOf(v));
					else if (c.equals(char.class.getName()))
						p.put(q, v.charAt(0));
					else if (c.equals("json"))
						p.put(q, IpmemsJsonUtil.parse(v));
					else p.put(q, v);
				} catch (Exception x) {
					p.put(q, v);
				}
			} else p.put(k, props.getProperty(k));
		}
		props.clear();
		p.put("jarDir", JAR_DIR.toString());
		String jarDirUrlStr = JAR_URL.toExternalForm();
		if (jarDirUrlStr.endsWith("/"))
			jarDirUrlStr = jarDirUrlStr.substring(0, jarDirUrlStr.length() - 1);
		p.put("jarDirUrl", jarDirUrlStr);
		String tz = IpmemsCollections.arg(a, "z", "tz", null);
		String ll = IpmemsCollections.arg(a, "q", "loggingLevel", null);
		String lc = IpmemsCollections.arg(a, "l", "locale", null);
		if (tz != null) p.put("timeZone", tz);
		if (ll != null)	p.put("loggingLevel", ll);
		if (lc != null) p.put("locale", lc);
		if (!p.containsKey("dataDirectory"))
			p.put("dataDirectory", new File(JAR_DIR, "data").toString());
		if (!p.containsKey("scriptsDirectory"))
			p.put("scriptsDirectory", new File(JAR_DIR, "scripts").toString());
		if (!p.containsKey("webDirectory"))
			p.put("webDirectory", new File(JAR_DIR, "web").toString());
		if (p.containsKey("timeZone")) try {
			TimeZone.setDefault(TimeZone.getTimeZone(sst("timeZone")));
		} catch (Exception x) {
			x.printStackTrace(System.err);
		}
		if (p.containsKey("locale")) try {
			Object[] ps = p.get("locale").toString().split("[_-]");
			Class<?>[] cs = new Class<?>[ps.length];
			Arrays.fill(cs, String.class);
			Locale.setDefault(Locale.class.getConstructor(cs).newInstance(ps));
		} catch (Exception x) {
			x.printStackTrace(System.err);
		}
		if (p.containsKey("loggingLevel")) try {
			loggingLevel = IpmemsLogRec.getLevelIndex(
					p.remove("loggingLevel").toString());
		} catch (Exception x) {
			x.printStackTrace(System.err);
		}
	}
	
	/**
	 * Main method.
	 * @param args Command line arguments.
	 * @throws Exception An exception.
	 */
	public static void main(String[] args) throws Exception {
		if (help(Ipmems.class, args)) return;
		pid();
		log();
		cmd(args);
		boot();
		rc();
		auto();
	}
	
	private static void boot() {
		ServiceLoader<IpmemsInit> sl = ServiceLoader.load(
				IpmemsInit.class,
				IpmemsScriptEngines.getDefaultClassLoader());
		for (IpmemsInit init: sl) try {
			init.start();
		} catch (Exception x) {
			x.printStackTrace(System.err);
		}
		sl.reload();
	}
	
	private static void auto() {
		TreeMap<Integer,IpmemsAuto> m = null;
		ServiceLoader<IpmemsAuto> sl = ServiceLoader.load(
				IpmemsAuto.class,
				IpmemsScriptEngines.getDefaultClassLoader());
		try {
			for (IpmemsAuto al: sl)	{
				if (m == null) m = new TreeMap<Integer,IpmemsAuto>();
				m.put(al.getOrder(), al);
			}
			if (m != null) for (IpmemsAuto al: m.values()) try {
				al.start();
				IpmemsLoggers.info("sys", "<{0}> OK", al.getName());
			} catch (Exception y) {
				IpmemsLoggers.warning("err", "<{0}>", y, al.getName());
			}
		} catch (Exception x) {
			x.printStackTrace(System.err);
		} finally {
			if (m != null) m.clear();
			sl.reload();
		}
	}
		
	private static void log() throws Exception {
		File logFile = new File(JAR_DIR, "ipmems.log");
		boolean a = !(logFile.exists() && logFile.length() > 0xFFFFFFL);
		PrintStream pstr = new PrintStream(
				new FileOutputStream(logFile, a), true, "UTF-8");
		IpmemsIOLib.close(System.out, System.in, System.err);
		System.setOut(pstr);
		System.setErr(pstr);
		System.setIn(null);
		System.out.print("# ");
		System.out.print(new Date());
		System.out.println(" " + IpmemsLib.getLogo());
	}
		
	private static void rc() {
		try {
			File f = new File(JAR_DIR, "rc.json");
			if (!f.isFile() || !f.canRead()) return;
			Map<String,Map<String,Object>> m = IpmemsJsonUtil.parse(f);
			M: for (Map.Entry<String,Map<String,Object>> e: m.entrySet()) {
				Map<String,Object> v = e.getValue();
				try {
					String cl = String.valueOf(v.get("class"));
					String mt = String.valueOf(v.get("method"));
					Class<?> c = IpmemsScriptEngines.loadClass(cl);
					if (v.containsKey("args")) {
						Object[] a = ((List)v.get("args")).toArray();
						IpmemsDynInvoke.invokeStatic(c, mt, a);
					} else IpmemsDynInvoke.invokeStatic(c, mt);
					IpmemsLoggers.info("sys", "[{0}] OK", e.getKey());
				} catch (Exception y) {
					IpmemsLoggers.warning("err", "[{0}]", y, e.getKey());
				}
			}
			m.clear();
		} catch (Exception x) {
			IpmemsLoggers.severe("err", "RC", x);
		}
	}	
	
	/**
	 * Shows the help screen.
	 * @param s Resource store.
	 * @param args Arguments.
	 * @throws Exception An exception.
	 * @return Need show help flag.
	 */
	public static boolean help(Class<?> s, String[] args) throws Exception {
		boolean flag = false;
		for (String k: args) if ("-h".equals(k) || "--help".equals(k)) {
			flag = true;
			break;
		}
		if (!flag) return flag;
		System.out.println(IpmemsLib.getLogo());
		System.out.println();
		String lang = Locale.getDefault().getLanguage();
		String name = String.format("help_%s.txt", lang);
		URL url = s.getResource(name);
		if (url == null) url = s.getResource("help.txt");
		System.out.println(IpmemsIOLib.getText(url));
		return flag;
	}
	
	/**
	 * Checks if the properties map has a property with given key.
	 * @param key Property key.
	 * @return Property existence boolean result.
	 */
	public static boolean has(String key) {
		return p.containsKey(key);
	}
	
	/**
	 * Get the property value.
	 * @param key Property key.
	 * @return Property value.
	 */
	public static Object get(String key) {
		final Object v = p.get(key);
		return v instanceof String ? substituted((String)v) : v;
	}
	
	/**
	 * Get the non-substituted property value.
	 * @param key Property key.
	 * @return Property value.
	 */
	public static Object raw(String key) {
		return p.get(key);
	}
	
	/**
	 * Returns the substituted option value by given key.
	 * @param key Key.
	 * @param d Default value.
	 * @return Option value.
	 */
	public static String sst(String key, String d) {
		return substituted(p.containsKey(key) ? String.valueOf(p.get(key)) : d);
	}
	
	/**
	 * Returns the substituted option value by given key.
	 * @param key Key.
	 * @return Option value.
	 */
	public static String sst(String key) {
		return sst(key, null);
	}
	
	/**
	 * Returns the option value by given key.
	 * @param key Key.
	 * @param d Default value.
	 * @return Option value.
	 */
	public static Object get(String key, Object d) {
		Object v = p.containsKey(key) ? p.get(key) : d;
		return v instanceof String ? substituted((String)v) : v;
	}
	
	/**
	 * Get the substituted string.
	 * @param s Source string.
	 * @return Substituted string.
	 */
	public static String substituted(String s) {
		if (s.indexOf('@') < 0) return s; else {
			StringBuilder sb = new StringBuilder(s);
			for (int i = 0, l = 0, c = 0; c < 16 && i < sb.length(); i++)
				if (	sb.charAt(i) == '@' &&
						i + 2 < sb.length() &&
						sb.charAt(i + 1) == '{' &&
						(l = sb.indexOf("}", i + 2)) > 0) {
					String k = sb.substring(i + 2, l);
					if (k.isEmpty()) {
						sb.setCharAt(i, '!');
						i = l + 1;
						c = 0;
						continue;
					}
					Object v = p.get(k);
					if (v == null) v = System.getProperty(k);
					if (v == null) {
						sb.setCharAt(i, '!');
						i = l + 1;
						c = 0;
					} else {
						String r = v.toString();
						sb.replace(i, l + 1, r);
						c++;
					}
				}
			s = sb.toString();
		}
		return s;
	}
		
	/**
	 * Returns the typed option value by given key.
	 * @param <T> Option type.
	 * @param cl Option type class.
	 * @param key Key.
	 * @param def Default value.
	 * @return Option value.
	 */
	public static <T> T get(Class<T> cl, String key, T def) {
		try {
			return IpmemsCollections.cast(cl, get(key, def));
		} catch (Exception x) {
			throw new IllegalArgumentException("(" + cl + ")" + key, x);
		}
	}
	
	/**
	 * Get the property thread unsafe map.
	 * @return Property map.
	 */
	public static Map<String,Object> getMap() {
		return p;
	}
		
	private static void pid() throws Exception {
		String n = ManagementFactory.getRuntimeMXBean().getName();
		String pId = n.substring(0, n.indexOf('@'));
		File f = new File(JAR_DIR, "ipmems.pid");
		IpmemsIOLib.setText(f, pId);
		f.deleteOnExit();
	}
		
	/**
	 * Starts the service.
	 * @param cn Class name.
	 * @param args Arguments.
	 * @throws Exception An exception.
	 */
	public static void startService(String cn, List args) throws Exception {
		Class<IpmemsService> c = IpmemsScriptEngines.loadClass(cn);
		IpmemsService s = c.newInstance();
		if (IpmemsScriptEngines.has(s.getVar())) {
			IpmemsService v =(IpmemsService)IpmemsScriptEngines.get(s.getVar());
			if (!v.isRunning()) v.start();
		} else {
			IpmemsLoggers.initLogger(s.getLogName());
			s.init(args.toArray());
			s.start();
			IpmemsScriptEngines.bind(s.getVar(), s);
			MONITOR.fireEvent(s);
		}
	}
	
	/**
	 * Starts the service.
	 * @param var Variable name.
	 * @throws Exception An exception.
	 */
	public static void stopService(String var) throws Exception {
		if (IpmemsScriptEngines.has(var))
			((IpmemsService)IpmemsScriptEngines.get(var)).stop();
	}	
	
	private final static Map<String,Object> p =	new TreeMap<String,Object>();

	/**
	 * Logging level.
	 */
	public static int loggingLevel = Integer.MIN_VALUE;
	
	/**
	 * Current jar file directory.
	 */
	public static final File JAR_DIR;
	
	/**
	 * Current jar file directory URL.
	 */
	public static final URL JAR_URL;
	
	static {
		File file = null;
		URL url = null;
		try {
			url = Ipmems.class.getProtectionDomain().
					getCodeSource().getLocation();
			file = new File(url.toURI()).getParentFile();
			url = file.toURI().toURL();
		} catch (Exception x) {}
		JAR_DIR = file;
		JAR_URL = url;
	}
				
	/**
	 * Mime map.
	 */
	public static final Properties MIMES = new Properties();
	
	static {
		InputStream is = null;
		try {
			File mimes = new File(JAR_DIR, "mimes.properties");
			if (mimes.exists()) {
				is = new FileInputStream(mimes);
				MIMES.load(is);
			}
		} catch (Exception x) {
			x.printStackTrace(System.err);
		} finally {
			if (is != null) try {is.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * IPMEMS service creation and uncaught exception monitor.
	 */
	public static final Ipmems MONITOR = new Ipmems();
	
	static {
		Thread.setDefaultUncaughtExceptionHandler(MONITOR);
	}
}
