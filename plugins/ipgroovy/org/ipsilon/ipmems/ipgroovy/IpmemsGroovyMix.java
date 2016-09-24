package org.ipsilon.ipmems.ipgroovy;

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

import java.io.File;
import java.net.URL;
import java.util.Map;
import org.codehaus.groovy.runtime.MethodClosure;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.IpmemsLib;
import org.ipsilon.ipmems.IpmemsStrings;
import org.ipsilon.ipmems.json.IpmemsJsonUtil;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.prot.IpmemsProtUtil;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsNet;

/**
 * Mixing class.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsGroovyMix {
	/**
	 * Get the IPMEMS library.
	 * @return IPMEMS library.
	 */
	public static Class<IpmemsLib> getIpmems() {
		return IpmemsLib.class;
	}
	
	/**
	 * Get the user map.
	 * @return User map.
	 */
	public static Map<String,Object> getUserMap() {
		return IpmemsScriptEngines.userMap;
	}
	
	/**
	 * Get the exception text.
	 * @param t Target.
	 * @param th Exception.
	 * @return Exception text.
	 */
	public static String exceptionText(Object t, Throwable th) {
		return IpmemsStrings.exceptionText(th);
	}
	
	/**
	 * Makes an URL.
	 * @param t Target.
	 * @param path Path.
	 * @param p URL parameters.
	 * @return URL as string.
	 * @throws Exception An exception.
	 */
	public static String mkUrl(Object t, String path, Map p) throws Exception {
		return IpmemsNet.mkUrl(path, p);
	}
	
	/**
	 * Get the localized string.
	 * @param t Target.
	 * @param l Locale.
	 * @param k String key.
	 * @param args Arguments.
	 * @return Localized string.
	 */
	public static String locStr(Object t, String l, String k, Object... args) {
		return IpmemsIntl.locString(l, k, args);
	}
	
	/**
	 * Get the localized message.
	 * @param t Target.
	 * @param l Locale.
	 * @param k Message key.
	 * @param args Arguments.
	 * @return Localized message.
	 */
	public static String locMsg(Object t, String l, String k, Object... args) {
		return IpmemsIntl.locMessage(l, k, args);
	}
	
	/**
	 * Get the I/O task.
	 * @return I/O task.
	 */
	public static MethodClosure getIoTask() {
		return ioTask;
	}
	
	/**
	 * Get the I/O vector task.
	 * @return I/O vector task.
	 */
	public static MethodClosure getIoVectorTask() {
		return ioVectorTask;
	}
	
	/**
	 * Makes the JSON-transform.
	 * @param t Target.
	 * @param v Object.
	 * @return JSON-text or parsed object.
	 * @throws Exception An exception.
	 */
	public static Object json(Object t, Object v) throws Exception {
		return IpmemsJsonUtil.json(v);
	}
	
	/**
	 * Get the localized string.
	 * @param t Target.
	 * @param k String key.
	 * @param args Arguments.
	 * @return Localized string.
	 */
	public static String locString(Object t, String k, Object ... args) {
		return IpmemsIntl.string(k, args);
	}
	
	/**
	 * Get the localized message.
	 * @param t Target.
	 * @param k Message key.
	 * @param args Arguments.
	 * @return Localized message.
	 */
	public static String locMessage(Object t, String k, Object ... args) {
		return IpmemsIntl.message(k, args);
	}
	
	/**
	 * Get the system property.
	 * @param t Target.
	 * @param k Property key.
	 * @return Property value.
	 */
	public static Object sysProp(Object t, String k) {
		return Ipmems.get(k);
	}
	
	/**
	 * Get the system substituted property.
	 * @param t Target.
	 * @param k Property key.
	 * @param def Default value.
	 * @return Property value.
	 */
	public static String sysProp(Object t, String k, String def) {
		return Ipmems.sst(k, def);
	}
	
	/**
	 * Get the system property.
	 * @param t Target.
	 * @param k Property key.
	 * @param def Default value.
	 * @return Property value.
	 */
	public static Object sysProp(Object t, String k, Object def) {
		return Ipmems.get(k, def);
	}
	
	/**
	 * Evaluates a file.
	 * @param t Target.
	 * @param f File.
	 * @return Evaluation result.
	 * @throws Exception An exception.
	 */
	public static Object eval(Object t, File f) throws Exception {
		return IpmemsScriptEngines.eval(f);
	}
	
	/**
	 * Evaluates a resource given by URL.
	 * @param t Target.
	 * @param url URL of the resource.
	 * @return Evaluation result.
	 * @throws Exception An exception.
	 */
	public static Object eval(Object t, URL url) throws Exception {
		return IpmemsScriptEngines.eval(url);
	}
	
	/**
	 * Get the substituted string.
	 * @param t Target.
	 * @param str Source string.
	 * @return Substituted string.
	 */
	public static String substituted(Object t, String str) {
		return Ipmems.substituted(str);
	}
	
	/**
	 * Log the info record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param args Arguments.
	 */
	public static void info(Object g, String k, String m, 
			Throwable t, Object ... args) {
		IpmemsLoggers.info(k, m, t, args);
	}
	
	/**
	 * Log the severe record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param args Arguments.
	 */
	public static void severe(Object g, String k, String m, 
			Throwable t, Object ... args) {
		IpmemsLoggers.severe(k, m, t, args);
	}
	
	/**
	 * Log the config record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param args Arguments.
	 */
	public static void config(Object g, String k, String m, 
			Throwable t, Object ... args) {
		IpmemsLoggers.config(k, m, t, args);
	}
	
	/**
	 * Log the warning record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param args Arguments.
	 */
	public static void warning(Object g, String k, String m, 
			Throwable t, Object ... args) {
		IpmemsLoggers.warning(k, m, t, args);
	}
	
	/**
	 * Log the fine record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param args Arguments.
	 */
	public static void fine(Object g, String k, String m, 
			Throwable t, Object ... args) {
		IpmemsLoggers.fine(k, m, t, args);
	}
	
	/**
	 * Log the finer record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param args Arguments.
	 */
	public static void finer(Object g, String k, String m, 
			Throwable t, Object ... args) {
		IpmemsLoggers.finer(k, m, t, args);
	}
	
	/**
	 * Log the finest record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param args Arguments.
	 */
	public static void finest(Object g, String k, String m, 
			Throwable t, Object ... args) {
		IpmemsLoggers.finest(k, m, t, args);
	}
	
	/**
	 * Log the info record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param args Arguments.
	 */
	public static void info(Object g, String k, String m, Object ... args) {
		IpmemsLoggers.info(k, m, args);
	}
	
	/**
	 * Log the severe record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param args Arguments.
	 */
	public static void severe(Object g, String k, String m, Object ... args) {
		IpmemsLoggers.severe(k, m, args);
	}
	
	/**
	 * Log the config record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param args Arguments.
	 */
	public static void config(Object g, String k, String m, Object ... args) {
		IpmemsLoggers.config(k, m, args);
	}
	
	/**
	 * Log the warning record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param args Arguments.
	 */
	public static void warning(Object g, String k, String m, Object ... args) {
		IpmemsLoggers.warning(k, m, args);
	}
	
	/**
	 * Log the fine record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param args Arguments.
	 */
	public static void fine(Object g, String k, String m, Object ... args) {
		IpmemsLoggers.fine(k, m, args);
	}
	
	/**
	 * Log the finer record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param args Arguments.
	 */
	public static void finest(Object g, String k, String m, Object ... args) {
		IpmemsLoggers.finest(k, m, args);
	}
	
	/**
	 * Log the finer record.
	 * @param g Target.
	 * @param k Log key.
	 * @param m Message.
	 * @param args Arguments.
	 */
	public static void finer(Object g, String k, String m, Object ... args) {
		IpmemsLoggers.finer(k, m, args);
	}
	
	private static final MethodClosure ioTask =
			new MethodClosure(IpmemsProtUtil.class, "ioTask");
	private static final MethodClosure ioVectorTask =
			new MethodClosure(IpmemsProtUtil.class, "ioVectorTask");
}
