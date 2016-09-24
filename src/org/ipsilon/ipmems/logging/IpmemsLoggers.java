package org.ipsilon.ipmems.logging;

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
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS loggers utility class.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsLoggers {
	/**
	 * Adds a listener to the listener list.
	 * @param l A listener.
	 */
	public static void addLogListener(IpmemsLoggersListener l) {
		ls.add(l);
		for (IpmemsLogger lg: lm.values()) 
			l.added(new IpmemsLogEventData(IpmemsLoggers.class, lg));
	}
	
	/**
	 * Removes a listener from the listener list.
	 * @param l A listener.
	 */
	public static void removeLogListener(IpmemsLoggersListener l) {
		ls.remove(l);
	}
	
	/**
	 * Add a logger to the system.
	 * @param lg Logger object.
	 * @return Added logger.
	 */
	public static IpmemsLogger addLogger(IpmemsLogger lg) {
		IpmemsLogger l = lm.putIfAbsent(lg.getKey(), lg);
		if (l != null) return l;
		for (IpmemsLoggersListener q: ls) 
			q.added(new IpmemsLogEventData(IpmemsLoggers.class, lg));
		return lg;
	}
	
	/**
	 * Removes the system logger.
	 * @param key Logger's key.
	 * @return Removed logger.
	 */
	public static IpmemsLogger removeLogger(String key) {
		return lm.remove(key);
	}
		
	/**
	 * Initialize the logger.
	 * @param k Logger key.
	 */
	public static void initLogger(String k) {
		IpmemsLogger lg = lm.get(k);
		if (lg != null); else try {
			IpmemsFormatterItf fm = dlf;
			if (Ipmems.has("log." + k + ".fmt")) {
				String cn = Ipmems.sst("log." + k + ".fmt");
				fm = (IpmemsFormatterItf)
						IpmemsScriptEngines.loadClass(cn).newInstance();
			}
			File ld = new File(Ipmems.sst("logDir", "@{dataDirectory}/logs"));
			File logDir = new File(ld, k);
			if (!logDir.exists()) logDir.mkdirs();
			int l;
			if (Ipmems.has("log." + k + ".lev"))
				l = IpmemsLogRec.getLevelIndex(Ipmems.sst("log." + k + ".lev"));
			else l = Ipmems.loggingLevel;
			IpmemsLogger logger = new IpmemsLogger(k, l);
			File logFile = new File(logDir, li + ".log");
			logger.addHandler(new IpmemsFileHandler(logFile, fm));
			if (Ipmems.get(Boolean.class, "logMem", true))
				logger.addHandler(new IpmemsMemoryHandler());
			addLogger(logger);
		} catch (Exception x) {
			x.printStackTrace(System.err);
		}
	}
	
	/**
	 * Reinitialize the log files.
	 * @param key Logger's key.
	 * @param bak BAK creation flag.
	 * @throws IOException An I/O exception.
	 */
	public static void reinitLogs(String key, boolean bak) throws IOException {
		IpmemsLogger l = lm.get(key);
		if (l == null) return;
		for (IpmemsLogHandler h: l.getHandlers())
			if (h instanceof IpmemsFileHandler)
				((IpmemsFileHandler)h).reinit(bak);
	}
			
	/**
	 * Get the logger object.
	 * @param key Logger's key.
	 * @return Logger object.
	 */
	public static IpmemsLogger getLogger(String key) {
		return lm.get(key);
	}
	
	/**
	 * Get logger names.
	 * @return Logger names.
	 */
	public static Collection<String> getLoggerNames() {
		return lm.keySet();
	}
	
	/**
	 * Log the info message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param v Message arguments.
	 */
	public static void info(String key, String m, Throwable t, Object ... v) {
		l(800, key, m, t, v);
	}
		
	/**
	 * Log the info message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void info(String key, String m, Object ... v) {
		l(800, key, m, v);
	}
		
	/**
	 * Log the warning message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param v Message arguments.
	 */
	public static void warning(String key, String m, Throwable t, Object... v) {
		l(900, key, m, t, v);
	}
	
	/**
	 * Log the warning message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void warning(String key, String m, Object ... v) {
		l(900, key, m, v);
	}
		
	/**
	 * Log the severe message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param v Message arguments.
	 */
	public static void severe(String key, String m, Throwable t, Object ... v) {
		l(1000, key, m, t, v);
	}
	
	/**
	 * Log the severe message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void severe(String key, String m, Object ... v) {
		l(1000, key, m, v);
	}
	
	/**
	 * Log the fine message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param v Message arguments.
	 */
	public static void fine(String key, String m, Throwable t, Object ... v) {
		l(500, key, m, t, v);
	}
	
	/**
	 * Log the fine message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void fine(String key, String m, Object ... v) {
		l(500, key, m, v);
	}
	
	/**
	 * Log the finest message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param v Message arguments.
	 */
	public static void finer(String key, String m, Throwable t, Object ... v) {
		l(400, key, m, t, v);
	}
	
	/**
	 * Log the finest message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void finer(String key, String m, Object ... v) {
		l(400, key, m, v);
	}
	
	/**
	 * Log the finest message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void finest(String key, String m, Throwable t, Object ... v) {
		l(300, key, m, t, v);
	}
	
	/**
	 * Log the finest message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void finest(String key, String m, Object ... v) {
		l(300, key, m, v);
	}
	
	/**
	 * Log the config message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param v Message arguments.
	 */
	public static void config(String key, String m, Throwable t, Object ... v) {
		l(700, key, m, t, v);
	}
	
	/**
	 * Log the config message.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void config(String key, String m, Object ... v) {
		l(700, key, m, v);
	}
		
	private static void l(int l, String k, String m, Throwable t, Object... v) {
		IpmemsLogger lg = lm.get(k);
		if (v == null) v = new Object[] {v};
		if (lg != null) lg.log(new IpmemsLogRec(l, m, t, v));
		else if (l >= Ipmems.loggingLevel) synchronized(System.out) {
			try {
				dlf.format(System.out, new IpmemsLogRec(l, m, t, v));
			} catch (Exception x) {}
		}
	}
			
	private static void l(int l, String k, String m, Object ... v) {
		l(l, k, m, null, v);
	}
		
	/**
	 * Log a message.
	 * @param lev Message's log level.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param t Throwable.
	 * @param v Message arguments.
	 */
	public static void log(String lev, String key, String m,
			Throwable t, Object ... v) {
		l(IpmemsLogRec.getLevelIndex(lev.toUpperCase()), key, m, t, v);
	}
	
	/**
	 * Log a message.
	 * @param lev Message's log level.
	 * @param key Logger's key.
	 * @param m Message.
	 * @param v Message arguments.
	 */
	public static void log(String lev, String key, String m, Object ... v) {
		l(IpmemsLogRec.getLevelIndex(lev.toUpperCase()), key, m, v);
	}		
	
	private static final ConcurrentSkipListMap<String,IpmemsLogger> lm =
			new ConcurrentSkipListMap<String,IpmemsLogger>();
	private static final ConcurrentLinkedQueue<IpmemsLoggersListener> ls = 
			new ConcurrentLinkedQueue<IpmemsLoggersListener>();
	private static final IpmemsFormatter dlf = new IpmemsFormatter();
	private static final byte li;
		
	static {
		byte idx = 0;
		try {
			byte maxLogs = Ipmems.get(Byte.class, "logMaxCount", (byte)5);
			File ld = new File(Ipmems.sst("logDir",	"@{dataDirectory}/logs"));
			if (!ld.exists()) ld.mkdirs();
			File logIdFile = new File(ld, "log.idx");
			if (logIdFile.exists() && logIdFile.isFile()) {
				idx = Byte.decode(IpmemsIOLib.getText(logIdFile));
				if (++idx >= maxLogs) idx = 0;
			}
			IpmemsIOLib.setText(logIdFile, Byte.toString(idx));
		} catch (Exception x) {}
		li = idx;
		for (String s: Ipmems.sst("logNames", "sys,err,usr").split(",")) {
			s = s.trim();
			if (!s.isEmpty()) initLogger(s);
		}
	}
}
