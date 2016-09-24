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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Timestamp;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Calendar.*;
import java.util.ResourceBundle.Control;
import org.ipsilon.ipmems.res.IpmemsResBundle;

/**
 * Internationalization utility function.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsIntl {	
	/**
	 * Get the resource bundle by name and according to the default locale.
	 * @param name Name of bundle.
	 * @return Resource bundle.
	 */
	public static ResourceBundle getBundle(String name) {
		return getBundle(name, Locale.getDefault());
	}
	
	/**
	 * Get the resource bundle by name.
	 * @param name Name of bundle.
	 * @param loc Locale.
	 * @return Resource bundle that supports UTF-8 encoding in property files.
	 */
	public static ResourceBundle getBundle(String name, Locale loc) {
		try {
			return ResourceBundle.getBundle(name, loc, new Control() {
				@Override
				public ResourceBundle newBundle(
						String baseName, Locale loc, String format, 
						ClassLoader bcl, boolean reload) throws 
							IllegalAccessException,
							InstantiationException,
							IOException {
					String bName = toBundleName(baseName, loc);
					String pName = toResourceName(bName, "properties");
					String iName = toResourceName(bName, "ipr");
					File resDir = new File(Ipmems.JAR_DIR, "res");
					File pres = new File(resDir, pName);
					File ires = new File(resDir, iName);
					if (ires.exists()) return new IpmemsResBundle(ires);
					else if (pres.exists()) {
						Reader r = null;
						PropertyResourceBundle pr;
						try {
							r = new InputStreamReader(
									new FileInputStream(pres), "UTF-8");
							pr = new PropertyResourceBundle(r);
						} finally {
							if (r != null) r.close();
						}
						return pr;
					} else throw new InstantiationException();
				}

				@Override
				public boolean needsReload(String baseName, Locale locale, 
					String format, ClassLoader loader, 
					ResourceBundle bundle, long loadTime) {
					return false;
				}				
			});
		} catch (Exception x) {
			return new ListResourceBundle() {
				@Override
				protected Object[][] getContents() {
					return new Object[0][0];
				}
			};
		}
	}

	/**
	 * Get the message text.
	 * @param key Message key.
	 * @param args Message parameters.
	 * @return Formatted message text.
	 */
	public static String message(String key, Object ... args) {
		try {
			String fmt = args == null || args.length == 0 ? key :
					MB.containsKey(key) ? MB.getString(key) : key;
			StringBuilder sb = new StringBuilder();
			append(sb, fmt, args);
			return sb.toString();
		} catch (Exception x) {
			return key + Arrays.deepToString(args);
		}
	}
	
	/**
	 * Get the formatted string.
	 * @param key String key.
	 * @param args String parameters.
	 * @return Formatted string.
	 */
	public static String string(String key, Object ... args) {
		try {
			String fmt = args == null || args.length == 0 ? key :
					SB.containsKey(key) ? SB.getString(key) : key;
			return String.format(fmt, args);
		} catch (Exception x) {
			return key + Arrays.deepToString(args);
		}
	}
				
	/**
	 * Gets the locale from its description.
	 * @param locale Locale description.
	 * @return Locale object.
	 */
	public static Locale getLocale(String locale) {
		if (locale == null) return Locale.getDefault(); else {
			locale = locale.trim();
			if (locale.contains(",")) {
				String[] locParts = locale.split(",");
				if (locParts[0].contains(";")) 
					locParts[0] = locParts[0].split(";")[0].trim();
				locale = locParts[0];
			}
			String[] parts = locale.split("[_-]");
			switch (parts.length) {
				case 1:		return new Locale(parts[0]);
				case 2:		return new Locale(parts[0], parts[1]);
				case 3:		return new Locale(parts[0], parts[1], parts[2]);
				default:	return Locale.getDefault();
			}
		}
	}
	
	/**
	 * Get the localized string.
	 * @param l Locale description.
	 * @param str Source string.
	 * @param args Arguments.
	 * @return Localized string.
	 */
	public static String string(Locale l, String str, Object ... args) {
		try {
			ResourceBundle rb = l.equals(Locale.getDefault()) ? 
					SB : getBundle("strings", l);
			String fmt = args == null || args.length == 0 ? str :
					rb.containsKey(str) ? rb.getString(str) : str;
			return String.format(fmt, args);
		} catch (Exception x) {
			return String.format(l, str, args);
		}
	}
	
	/**
	 * Get the localized message.
	 * @param l Locale.
	 * @param str Source string.
	 * @param args Arguments.
	 * @return Localized message.
	 */
	public static String message(Locale l, String str, Object ... args) {
		try {
			ResourceBundle rb = l.equals(Locale.getDefault()) ? 
					MB : getBundle("messages", l);
			String fmt = args == null || args.length == 0 ? str :
					rb.containsKey(str) ? rb.getString(str) : str;
			StringBuilder sb = new StringBuilder();
			append(sb, fmt, args);
			return sb.toString();
		} catch (Exception x) {
			return str + Arrays.deepToString(args);
		}
	}
	
	/**
	 * Get the localized string.
	 * @param loc Locale description.
	 * @param str Source string.
	 * @param args String arguments.
	 * @return Localized string.
	 */
	public static String locString(String loc, String str, Object ... args) {
		return string(getLocale(loc), str, args);
	}
	
	/**
	 * Get the localized message.
	 * @param l Locale descriptor.
	 * @param msg Source message.
	 * @param args Message arguments.
	 * @return Localized message.
	 */
	public static String locMessage(String l, String msg, Object ... args) {
		return message(getLocale(l), msg, args);
	}
	
	/**
	 * Parse an ISO-formatted date.
	 * @param date ISO-formatted date.
	 * @return Java date object.
	 */
	public static Date parseIso(String date) {
		if (date == null) return null; else {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			c.setTimeInMillis(0L);
			int o = 0, i;
			try {
				i = date.indexOf('-');
				if (i < 0) throw new IllegalArgumentException("year");
				c.set(YEAR, Integer.parseInt(date.substring(o, i)));
				o = i + 1;
				i = date.indexOf('-', o);
				if (i < 0) throw new IllegalArgumentException("month");
				c.set(MONTH, Integer.parseInt(date.substring(o, i)) - 1);
				o = i + 1;
				i = date.indexOf('T', o);
				if (i < 0) i = date.indexOf(' ', o);
				if (i < 0) throw new IllegalArgumentException("date");
				c.set(DATE, Integer.parseInt(date.substring(o, i)));
				o = i + 1;
				i = date.indexOf(':', o);
				if (i < 0) throw new IllegalArgumentException("hour");
				c.set(HOUR_OF_DAY, Integer.parseInt(date.substring(o, i)));
				o = i + 1;
				i = date.indexOf(':', o);
				if (i < 0) throw new IllegalArgumentException("minute");
				c.set(MINUTE, Integer.parseInt(date.substring(o, i)));
				o = i + 1;
				if (o + 2 == date.length()) {
					i = date.length();
					c.set(SECOND, Integer.parseInt(date.substring(o, i)));
					c.add(MILLISECOND, -TimeZone.getDefault().getRawOffset());
				} else {
					c.set(SECOND, Integer.parseInt(date.substring(o, o += 2)));
					int s = date.charAt(o++) == '-' ? 1 : -1;
					i = o + 2;
					c.add(HOUR, Integer.parseInt(date.substring(o, i)) * s);
					o += 2;
					i = o + 2;
					c.add(MINUTE, Integer.parseInt(date.substring(o, i)) * s);
				}
				return c.getTime();
			} catch (Exception x) {
				throw new IllegalArgumentException(date, x);
			}
		}
	}
	
	/**
	 * Formats the date.
	 * @param date A date.
	 * @return Formatted date.
	 */
	public static String formatIso(Date date) {
		StringBuilder sb = new StringBuilder(24);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		sb.append(c.get(YEAR));
		sb.append('-');
		int v = c.get(MONTH) + 1;
		if (v < 10) sb.append('0');
		sb.append(v);
		sb.append('-');
		v = c.get(DATE);
		if (v < 10) sb.append('0');
		sb.append(v);
		sb.append('T');
		v = c.get(HOUR_OF_DAY);
		if (v < 10) sb.append('0');
		sb.append(v);
		sb.append(':');
		v = c.get(MINUTE);
		if (v < 10) sb.append('0');
		sb.append(v);
		sb.append(':');
		v = c.get(SECOND);
		if (v < 10) sb.append('0');
		sb.append(v);
		v = TimeZone.getDefault().getRawOffset() / (1000 * 60);
		sb.append(v >= 0 ? '+' : '-');
		if (v < 600) sb.append('0');
		sb.append(v / 60);
		v %= 60;
		if (v < 10) sb.append('0');
		sb.append(v);
		return sb.toString();
	}
	
	/**
	 * Append the formatted message to an appendable object.
	 * @param a Appendable object.
	 * @param fmt Format.
	 * @param args Arguments.
	 * @throws IOException An I/O exception.
	 */
	public static void append(
			Appendable a, String fmt, Object ... args) throws IOException {
		int o = 0;
		for (int i = 0; i < fmt.length(); i++) {
			char c = fmt.charAt(i);
			if (c == '\'') {
				if (o < i) a.append(fmt, o, i);
				if (i + 1 < fmt.length() && fmt.charAt(i + 1) == '\'') {
					a.append(c);
					i++;
					o = i + 1;
				} else {
					int k = fmt.indexOf('\'', i + 1);
					if (k < 0) {
						a.append(fmt, i + 1, fmt.length());
						return;
					} else {
						a.append(fmt, i + 1, k);
						i = k;
						o = i + 1;
					}
				}
			} else if (c == '{') {
				if (o < i) a.append(fmt, o, i);
				int k = fmt.indexOf('}', i + 1);
				if (k < 0) {
					a.append(fmt, i, fmt.length());
					return;
				} else {
					appendPart(a, fmt.substring(i + 1, k), args);
					i = k;
					o = k + 1;
				}
			}
		}
		if (o < fmt.length()) a.append(fmt, o, fmt.length());
	}
	
	private static void appendPart(
			Appendable a, String part, Object ... args) throws IOException {
		String[] parts = part.split(",", 3);
		int i;
		String type = null, format = null;
		Object o;
		try {
			i = Integer.parseInt(parts[0]);
			o = args[i];
			switch (parts.length) {
				case 2:
					type = parts[1];
					break;
				case 3:
					format = parts[2];
					type = parts[1];
					break;
			}
		} catch (Exception x) {
			a.append('{');
			a.append(part);
			a.append('}');
			return;
		}
		NumberFormat nf;
		DateFormat df;
		if (type == null) a.append(String.valueOf(o));		
		else try {
			if ("number".equals(type)) {
				if (format == null)
					nf = NumberFormat.getInstance();
				else if ("integer".equals(format))
					nf = NumberFormat.getIntegerInstance();
				else if ("currency".equals(format))
					nf = NumberFormat.getCurrencyInstance();
				else if ("percent".equals(format))
					nf = NumberFormat.getPercentInstance();
				else
					nf = new DecimalFormat(format);
				a.append(nf.format(o));
			} else if ("date".equals(type)) {
				if (format == null)
					df = DateFormat.getDateInstance();
				else if ("short".equals(format))
					df = DateFormat.getDateInstance(DateFormat.SHORT);
				else if ("medium".equals(format))
					df = DateFormat.getDateInstance(DateFormat.MEDIUM);
				else if ("long".equals(format))
					df = DateFormat.getDateInstance(DateFormat.LONG);
				else if ("full".equals(format))
					df = DateFormat.getDateInstance(DateFormat.FULL);
				else 
					df = new SimpleDateFormat(format);
				a.append(df.format(o));
			} else if ("time".equals(type)) {
				if (format == null)
					df = DateFormat.getTimeInstance();
				else if ("short".equals(format))
					df = DateFormat.getTimeInstance(DateFormat.SHORT);
				else if ("medium".equals(format))
					df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
				else if ("long".equals(format))
					df = DateFormat.getTimeInstance(DateFormat.LONG);
				else if ("full".equals(format))
					df = DateFormat.getTimeInstance(DateFormat.FULL);
				else 
					df = new SimpleDateFormat(format);
				a.append(df.format(o));
			} else if ("timestamp".equals(type)) {
				if (o instanceof Date) {
					a.append(new Timestamp(((Date)o).getTime()).toString());
				} else if (o instanceof Number) {
					a.append(new Timestamp(
							((Number)o).longValue()).toString());
				} else a.append(String.valueOf(o));
			} else if ("choice".equals(type)) {
				a.append(new ChoiceFormat(format).format(o));
			} else throw new IllegalArgumentException();
		} catch (Exception x) {
			a.append('{');
			a.append(part);
			a.append('}');
		}
	}
	
	/**
	 * Message bundle.
	 */
	public static final ResourceBundle MB = getBundle("messages");
	
	/**
	 * String bundle.
	 */
	public static final ResourceBundle SB = getBundle("strings");
}
