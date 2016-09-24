package org.ipsilon.ipmems.scripting;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.util.IpmemsFile;

/**
 * IPMEMS script engines.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsScriptEngines {
	/**
	 * Get the engine names.
	 * @return Current engine names.
	 */
	public static String[] getEngineIds() {
		String[] ns = new String[engs.length];
		for (int i = 0; i < engs.length; i++) ns[i] = engs[i].getId();
		return ns;
	}
	
	/**
	 * Get engine names.
	 * @return Engine names.
	 */
	public static String[] getEngineNames() {
		String[] ns = new String[engs.length];
		for (int i = 0; i < engs.length; i++) ns[i] = engs[i].toString();
		return ns;
	}
	
	/**
	 * Get an engine by name.
	 * @param id Engine id.
	 * @return Script engine.
	 */
	public static IpmemsScriptEngine getEngine(String id) {
		for (IpmemsScriptEngine e: engs) if (e.getId().equals(id)) return e;
		return null;
	}
	
	/**
	 * Get an engine by index.
	 * @param idx Engine index.
	 * @return Script engine.
	 */
	public static IpmemsScriptEngine getEngine(int idx) {
		return engs[idx];
	}
	
	/**
	 * Get an engine by script extension.
	 * @param ext Script extension.
	 * @return Engine object.
	 */
	public static IpmemsScriptEngine getEngineByScriptExt(String ext) {
		for (IpmemsScriptEngine e: engs)
			if (e.getScriptExtensions().contains(ext)) return e;
		return null;
	}
	
	/**
	 * Get an engine by web script extension.
	 * @param ext Script extension.
	 * @return Engine object.
	 */
	public static IpmemsScriptEngine getEngineByWebExt(String ext) {
		for (IpmemsScriptEngine e: engs)
			if (e.getWebExtensions().contains(ext)) return e;
		return null;
	}
	
	/**
	 * Get the script MIME by script extension.
	 * @param ext Script extension.
	 * @return Script MIME.
	 */
	public static String getScriptMimeByExt(String ext) {
		for (IpmemsScriptEngine e: engs)
			if (e.getMimeBinding().containsKey(ext)) 
				return e.getMimeBinding().get(ext);
		return null;
	}
	
	/**
	 * Get all the web extensions.
	 * @return All the web extensions.
	 */
	public static List<String> getWebExtensions() {
		ArrayList<String> wes = new ArrayList<String>();
		for (IpmemsScriptEngine e: engs) wes.addAll(e.getWebExtensions());
		return wes;
	}
	
	/**
	 * Get all the script extensions.
	 * @return All the script extensions.
	 */
	public static List<String> getScriptExtensions() {
		ArrayList<String> ses = new ArrayList<String>();
		for (IpmemsScriptEngine e: engs) ses.addAll(e.getScriptExtensions());
		return ses;
	}
		
	/**
	 * Find function by name.
	 * @param name Function name.
	 * @return Function object or null.
	 */
	public static Object findFunctionByName(String name) {
		for (IpmemsScriptEngine e: engs) {
			Object func = e.getMainInterpreter().getFunction(name);
			if (func != null) return func;
		}
		return null;
	}
	
	/**
	 * Find the script by name.
	 * @param name Script name.
	 * @return Script object or null.
	 */
	public static Object findScriptByName(String name) {
		for (IpmemsScriptEngine e: engs) {
			Object scr = e.getMainInterpreter().getScript(name);
			if (scr != null) return scr;
		}
		return null;
	}
	
	/**
	 * Checks whether the specified object is callable.
	 * @param o Any object.
	 * @return Callable status.
	 */
	public static boolean isCallable(Object o) {
		for (IpmemsScriptEngine e: engs)
			if (e.getMainInterpreter().isCallable(o)) return true;
		return false;
	}
	
	/**
	 * Checks whether the specified object is a function.
	 * @param o Any object.
	 * @return isFunction status.
	 */
	public static boolean isFunction(Object o) {
		for (IpmemsScriptEngine e: engs)
			if (e.getMainInterpreter().isFunction(o)) return true;
		return false;
	}
	
	/**
	 * Checks whether the specified object is a script.
	 * @param o Any object.
	 * @return isScript status.
	 */
	public static boolean isScript(Object o) {
		for (IpmemsScriptEngine e: engs)
			if (e.getMainInterpreter().isFunction(o)) return true;
		return false;
	}
	
	/**
	 * Calls the object.
	 * @param o Any object.
	 * @param args Arguments.
	 * @return Call result.
	 * @throws Exception Any exception.
	 */
	public static Object call(Object o, Object ... args) throws Exception {
		for (IpmemsScriptEngine e: engs) {
			IpmemsInterpreter intr = e.getMainInterpreter();
			if (intr.isCallable(o)) return intr.call(o, args);
		}
		return o;
	}
	
	/**
	 * Get the curried function.
	 * @param o Any object.
	 * @param args Arguments.
	 * @return Curried function or null.
	 * @throws Exception Any exception.
	 */
	public static Object curry(Object o, Object ... args) throws Exception {
		for (IpmemsScriptEngine e: engs) {
			IpmemsInterpreter intr = e.getMainInterpreter();
			if (intr.isCallable(o)) return intr.curry(o, args);
		}
		return null;
	}
	
	/**
	 * Composes the function.
	 * @param f1 First function.
	 * @param f2 Second function.
	 * @return Composed function or null.
	 */
	public static Object compose(Object f1, Object f2) {
		for (IpmemsScriptEngine e: engs) {
			IpmemsInterpreter intr = e.getMainInterpreter();
			if (intr.isCallable(f1) && intr.isCallable(f2))
				return intr.compose(f1, f2);
		}
		return null;
	}
	
	/**
	 * Compose the first function and another curried function.
	 * @param f1 First function.
	 * @param f2 Second function.
	 * @param a Arguments.
	 * @return Composed function.
	 * @throws Exception An exception.
	 */
	public static Object composeCurry(
			Object f1, Object f2, Object ... a) throws Exception {
		return compose(f1, curry(f2, a));
	}
	
	/**
	 * Checks whether the specified object is propertized.
	 * @param o Any object.
	 * @return Propertized status.
	 */
	public static boolean isPropertized(Object o) {
		for (IpmemsScriptEngine e: engs)
			if (e.getMainInterpreter().isPropertized(o)) return true;
		return false;
	}
	
	/**
	 * Checks whether the specified object is delegatized.
	 * @param o Any object.
	 * @return Delegatized status.
	 */
	public static boolean isDelegatized(Object o) {
		for (IpmemsScriptEngine e: engs)
			if (e.getMainInterpreter().isDelegatized(o)) return true;
		return false;
	}
	
	/**
	 * Sets the object property.
	 * @param o Any object.
	 * @param name Property name.
	 * @param v Property value.
	 */
	public static void setProperty(Object o, String name, Object v) {
		for (IpmemsScriptEngine e: engs) {
			IpmemsInterpreter i = e.getMainInterpreter();
			if (i.isPropertized(o)) {
				i.setProperty(o, name, v);
				break;
			}
		}
	}
	
	/**
	 * Sets the properties.
	 * @param o Any object.
	 * @param b Property map.
	 */
	public static void setProperties(Object o, Map<String,Object> b) {
		for (IpmemsScriptEngine e: engs) {
			IpmemsInterpreter i = e.getMainInterpreter();
			if (i.isPropertized(o)) {
				i.setProperties(o, b);
				break;
			}
		}
	}
	
	/**
	 * Gets the object property.
	 * @param o Any object.
	 * @param name Property name.
	 * @return Property value.
	 */
	public static Object getProperty(Object o, String name) {
		for (IpmemsScriptEngine e: engs) {
			IpmemsInterpreter i = e.getMainInterpreter();
			if (i.isPropertized(o)) return i.getProperty(o, name);
		}
		return null;
	}
	
	/**
	 * Sets the object's delegate.
	 * @param o Any object.
	 * @param d Delegate.
	 */
	public static void setDelegate(Object o, Object d) {
		for (IpmemsScriptEngine e: engs) {
			IpmemsInterpreter i = e.getMainInterpreter();
			if (i.isDelegatized(o)) {
				i.setDelegate(o, d);
				break;
			}
		}
	}
	
	/**
	 * Evaluates the file.
	 * @param f A script file.
	 * @return Evaluation result.
	 */
	public static Object eval(File f) throws Exception {
		IpmemsScriptEngine e = getEngineByScriptExt(
				IpmemsFile.getFileExtension(f));
		if (e != null) return e.getMainInterpreter().eval(f);
		else throw new NoSuchElementException(f.toString());
	}
	
	/**
	 * Evaluates the URL.
	 * @param url An URL.
	 * @return Evaluation result.
	 * @throws Exception Evaluation exception.
	 */
	public static Object eval(URL url) throws Exception {
		IpmemsScriptEngine e = getEngineByScriptExt(
				IpmemsFile.getFileExtension(url));
		if (e != null) return e.getMainInterpreter().eval(url);
		else throw new NoSuchElementException(url.toString());
	}
		
	/**
	 * Loads all the scripts.
	 * @throws Exception Any exception while scripts loading.
	 */
	public static void loadScripts() throws Exception {
		File scriptsDir = new File((String)Ipmems.get("scriptsDirectory"));
		if (!scriptsDir.isDirectory()) return;
		File sequenceFile = new File(scriptsDir, "sequence");
		if (!sequenceFile.isFile()) return;
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(
					new FileInputStream(sequenceFile), "UTF-8"));
			while (true) {
				String line = r.readLine();
				if (line == null) return;
				line = line.trim();
				if (line.startsWith("#") || line.isEmpty()) continue;
				File script = new File(scriptsDir, line);
				try {
					eval(script);
					IpmemsLoggers.info("sys", "{0}: OK", script);
				} catch (Exception x) {
					IpmemsLoggers.warning("err", "{0}", x, script);
				}
			}
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}
			
	/**
	 * Binds the variable.
	 * @param name Variable name.
	 * @param o Variable value.
	 */
	public static void bind(String name, Object o) {
		for (IpmemsScriptEngine e: engs) e.bind(name, o);
		userMap.put(name, o);
	}
	
	/**
	 * Unbinds the variable.
	 * @param name Variable name.
	 */
	public static void unbind(String name) {
		for (IpmemsScriptEngine e: engs) e.unbind(name);
		userMap.remove(name);
	}

	/**
	 * Checks whether the named variable exists.
	 * @param name Variable name.
	 * @return Variable status.
	 */
	public static boolean has(String name) {
		return userMap.containsKey(name);
	}
	
	/**
	 * Get the variable value.
	 * @param name Variable name.
	 * @return Variable value.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(String name) {
		return (T)userMap.get(name);
	}
	
	/**
	 * Get the default class loader.
	 * @return Default class loader.
	 */
	public static ClassLoader getDefaultClassLoader() {
		return engs[0].getDefaultClassLoader();
	}
	
	/**
	 * Loads the class through the default class loader.
	 * @param <T> Class type.
	 * @param n Class name.
	 * @return Loaded class.
	 * @throws ClassNotFoundException An exception if the class is not found.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> loadClass(String n) 
			throws ClassNotFoundException {
		return (Class<T>)engs[0].loadClass(n, true);
	}
	
	/**
	 * Clears the cache of all the loaded classes.
	 */
	public static void clearCache() {
		for (IpmemsScriptEngine e: engs) e.clearCache();
	}
	
	/**
	 * Bind map.
	 */
	public static final Map<String,Object> userMap =
			new ConcurrentSkipListMap<String,Object>();

	private static final IpmemsScriptEngine[] engs;
	static {
		ArrayList<Object> l = new ArrayList<Object>();
		File d = new File(Ipmems.JAR_DIR, "lib");
		if (d.isDirectory()) for (File f: d.listFiles()) try {
			l.add(f.toURI().toURL());
		} catch (Exception x) {}
		d = new File(Ipmems.JAR_DIR, "plugins");
		if (d.isDirectory()) for (File f: d.listFiles()) try {
			l.add(f.toURI().toURL());
		} catch (Exception x) {}
		Thread.currentThread().setContextClassLoader(
				new URLClassLoader(l.toArray(new URL[l.size()])));
		l.clear();
		ServiceLoader<IpmemsScriptEngine> el = 
				ServiceLoader.load(IpmemsScriptEngine.class);
		Object de = Ipmems.get("defaultEngine", "groovy");
		for (IpmemsScriptEngine e: el) try {
			e.init();
			if (e.getId().equals(de)) l.add(0, e); else l.add(e);
		} catch (Exception x) {
			x.printStackTrace(System.err);
		}
		engs = l.toArray(new IpmemsScriptEngine[l.size()]);
		l.clear();
		el.reload();
	}
}
