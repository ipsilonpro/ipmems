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

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import org.ipsilon.ipmems.IpmemsIntl;

/**
 * IPMEMS log record.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsLogRec {
	/**
	 * Default constructor.
	 */
	public IpmemsLogRec() {
		this(0, null, null, null);
	}
	
	/**
	 * Constructs the log record.
	 * @param l Log level.
	 * @param msg Log message.
	 */
	public IpmemsLogRec(int l, String msg) {
		this(l, msg, null, null);
	}
	
	/**
	 * Constructs the log record.
	 * @param l Log level.
	 * @param msg Log message.
	 * @param t Throwable.
	 */
	public IpmemsLogRec(int l, String msg, Throwable t) {
		this(l, msg, t, null);
	}
	
	/**
	 * Constructs the log record.
	 * @param l Log level.
	 * @param msg Log message.
	 * @param ps Parameters.
	 */
	public IpmemsLogRec(int l, String msg, Object[] ps) {
		this(l, msg, null, ps);
	}
	
	/**
	 * Constructs the log record.
	 * @param l Log level.
	 * @param msg Log message.
	 * @param t Exception or error.
	 * @param ps Parameters.
	 */
	public IpmemsLogRec(int l, String msg, Throwable t, Object[] ps) {
		this(System.currentTimeMillis(), l, msg, t, ps);
	}
	
	/**
	 * Constructs the IPMEMS log record.
	 * @param ts Timestmap.
	 * @param l Log level.
	 * @param msg Log message.
	 * @param t Thrown.
	 * @param ps Parameters.
	 */
	public IpmemsLogRec(long ts, int l, String msg, Throwable t, Object[] ps) {
		timestamp = ts;
		message = msg;
		thrown = t;
		params = immarray(ps);
		level = l;
	}

	/**
	 * Get the message's timestamp.
	 * @return Message's timestamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the message arguments.
	 * @return Message arguments.
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * Get the associated thrown.
	 * @return Associated thrown.
	 */
	public Throwable getThrown() {
		return thrown;
	}
	
	/**
	 * Get the record message.
	 * @return Record message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get the log level.
	 * @return Log level.
	 */
	public int getLevel() {
		return level;
	}
	
	/**
	 * Get the level name.
	 * @return Level name.
	 */
	public String getLevelName() {
		return getLevelName(level);
	}

	@Override
	public String toString() {
		return String.format("%1$tF %1$tT%1$tz %2$s%3$s", timestamp,
				IpmemsIntl.message(message, params),
				thrown != null ? " : " + thrown : "");
	}
	
	/**
	 * Get the log level name by index.
	 * @param n Level index.
	 * @return Level name.
	 */
	public static String getLevelName(int n) {
		switch (n) {
			case 800:					return "INFO";
			case 900:					return "WARNING";
			case 1000:					return "SEVERE";
			case 500:					return "FINE";
			case 400:					return "FINER";
			case 300:					return "FINEST";
			case 700:					return "CONFIG";
			case Integer.MAX_VALUE:		return "OFF";
			case Integer.MIN_VALUE:		return "ALL";
			default:					return Integer.toString(n);
		}
	}
	
	/**
	 * Get the level index by name.
	 * @param name Log level name.
	 * @return Level index.
	 */
	public static int getLevelIndex(String name) {
		if ("INFO".equals(name))			return 800;
		else if ("WARNING".equals(name))	return 900;
		else if ("SEVERE".equals(name))		return 1000;
		else if ("FINE".equals(name))		return 500;
		else if ("FINER".equals(name))		return 400;
		else if ("FINEST".equals(name))		return 300;
		else if ("CONFIG".equals(name))		return 700;
		else if ("OFF".equals(name))		return Integer.MAX_VALUE;
		else if ("ALL".equals(name))		return Integer.MIN_VALUE;
		else return Integer.decode(name);
	}
	
	private static Object[] immarray(Object[] a) {
		if (a == null) return a; else {
			for (int i = 0; i < a.length; i++) {
				Object e = a[i];
				if (e == null || e instanceof Number || e instanceof String ||
					e instanceof Boolean || e instanceof Character ||
					e instanceof Date || e instanceof Class || 
					e instanceof UUID || e instanceof Locale) continue;
				else if (e instanceof Object[])
					a[i] = Arrays.deepToString((Object[])e);
				else if (e instanceof byte[])
					a[i] = Arrays.toString((byte[])e);
				else if (e instanceof char[])
					a[i] = Arrays.toString((char[])e);
				else if (e instanceof double[])
					a[i] = Arrays.toString((double[])e);
				else if (e instanceof float[])
					a[i] = Arrays.toString((float[])e);
				else if (e instanceof short[])
					a[i] = Arrays.toString((short[])e);
				else if (e instanceof int[])
					a[i] = Arrays.toString((int[])e);
				else if (e instanceof long[])
					a[i] = Arrays.toString((long[])e);
				else if (e instanceof boolean[])
					a[i] = Arrays.toString((boolean[])e);
				else a[i] = e.toString();
			}
			return a;
		}
	}
	
	private final long timestamp;
	private final String message;
	private final Object[] params;
	private final Throwable thrown;
	private final int level;
}
