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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * String utilities.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsStrings {		
	/**
	 * Get the map value as string.
	 * @param key Value key.
	 * @param map Map.
	 * @param def Default value.
	 * @return Map value.
	 */
	public static String getMapValue(Object key, Map map, String def) {
		return map.containsKey(key) ? String.valueOf(map.get(key)) : def;
	}
	
	/**
	 * Get the map value as string and delete it.
	 * @param key Value key.
	 * @param map Map.
	 * @param def Default value.
	 * @return Map property value.
	 */
	public static String getMapValueRest(Object key, Map map, String def) {
		return map.containsKey(key) ? String.valueOf(map.remove(key)) : def;
	}
		
	/**
	 * Joins the array or list by separator.
	 * @param obj Iterable object.
	 * @param sep Separator.
	 * @return Joined string.
	 */
	public static String join(Object obj, Object sep) {
		if (obj == null) return "";
		StringBuilder sb = new StringBuilder();
		if (obj.getClass().isArray()) {
			int n = Array.getLength(obj);
			for (int i = 0; i < n; i++) {
				sb.append(Array.get(obj, i));
				if (i < n - 1) sb.append(sep);
			}
		} else if (obj instanceof Iterable) {
			Iterator i = ((Iterable)obj).iterator();
			while (i.hasNext()) {
				sb.append(i.next());
				if (i.hasNext()) sb.append(sep);
			}
		}
		return sb.toString();
	}
		
	/**
	 * Get the repeated string.
	 * @param c A filling character.
	 * @param count Character count.
	 * @return Repeated string.
	 */
	public static String repeat(char c, int count) {
		if (count <= 0) return "";
		char[] buf = new char[count];
		Arrays.fill(buf, c);
		return String.valueOf(buf);
	}
	
	/**
	 * Get the message in hexadecimal format.
	 * @param b Source buffer.
	 * @return Message in hexadecimal format.
	 */
	public static String hex(byte[] b) {
		if (b == null || b.length == 0) return ""; else {
			char[] v = new char[b.length * 3 - 1];
			v[0] = Character.toUpperCase(
					Character.forDigit((b[0] >> 4) & 0xF, 16));
			v[1] = Character.toUpperCase(
					Character.forDigit(b[0] & 0xF, 16));
			for (int i = 1; i < b.length; i++) {
				v[3 * i - 1] = ' ';
				v[3 * i] = Character.toUpperCase(
						Character.forDigit((b[i] >> 4) & 0xF, 16));
				v[3 * i + 1] = Character.toUpperCase(
						Character.forDigit(b[i] & 0xF, 16));
			}
			return String.valueOf(v);
		}
	}
	
	/**
	 * Get the bytes in hexadecimal format.
	 * @param p Prefix.
	 * @param b Bytes.
	 * @return Bytes in hexadecimal format.
	 */
	public static String hex(String p, byte[] b) {
		if (b == null || b.length == 0) return ""; else {
			int n = p.length() + 2;
			char[] v = new char[n * b.length];
			for (int i = 0; i < b.length; i++) {
				int o = i * n;
				for (int k = 0; k < p.length(); k++) v[o + k] = p.charAt(k);
				o += p.length();
				v[o] = Character.toUpperCase(
						Character.forDigit((b[i] >> 4) & 0xF, 16));
				v[o + 1] = Character.toUpperCase(
						Character.forDigit(b[i] & 0xF, 16));
			}
			return String.valueOf(v);
		}
	}
	
	/**
	 * Get the bytes in octal format.
	 * @param p Prefix.
	 * @param b Bytes.
	 * @return Message in octal format.
	 */
	public static String oct(String p, byte[] b) {
		if (b == null || b.length == 0) return ""; else {
			int n = p.length() + 3;
			char[] v = new char[n * b.length];
			for (int i = 0; i < b.length; i++) {
				int o = i * n;
				for (int k = 0; k < p.length(); k++) v[o + k] = p.charAt(k);
				String q = Integer.toOctalString(b[i]);
				o += p.length();
				switch (q.length()) {
					case 1:
						v[o] = '0';
						v[o + 1] = '0';
						v[o + 2] = q.charAt(0);
						break;
					case 2:
						v[o] = '0';
						v[o + 1] = q.charAt(0);
						v[o + 2] = q.charAt(1);
						break;
					default:
						v[o] = q.charAt(0);
						v[o + 1] = q.charAt(1);
						v[o + 2] = q.charAt(2);
						break;
				}
			}
			return String.valueOf(v);
		}
	}
	
	/**
	 * Get content text.
	 * @param s Source string.
	 * @return Content text as string.
	 */
	public static String content(String s) {
		if (!s.contains(";")) return s;
		else return s.split(";")[0].trim();
	}
	
	/**
	 * Get context value by key.
	 * @param s Source string.
	 * @param k Key.
	 * @return Content value.
	 */
	public static String content(String s, String k) {
		String[] ps = s.split(";");
		for (String pair: ps) if (pair.contains("=")) {
			int i = pair.indexOf('=');
			String key = pair.substring(0, i).trim();
			if (k.equalsIgnoreCase(key)) {
				String v = pair.substring(i + 1).trim();
				if ((v.startsWith("\"") && v.endsWith("\"")) ||
					(v.startsWith("'") && v.endsWith("'")))
					return v.substring(1, v.length() - 1);
				else return v;
			}
		}
		return null;
	}
	
	/**
	 * Get the exception text closure.
	 * @param t Throwable.
	 * @return Throwable text representation.
	 */
	public static String exceptionText(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString().replace("\n", "<br/>");
	}
	
	/**
	 * Makes the string UUID to bytes conversion.
	 * @param uid UUID as string.
	 * @return UUID bytes.
	 */
	public static byte[] uuidToBytes(String uid) {
		UUID uuid = UUID.fromString(uid);
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
	
	/**
	 * Makes the UUID to bytes conversion.
	 * @param uid UUID.
	 * @return UUID bytes.
	 */
	public static byte[] uuidToBytes(UUID uid) {
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(uid.getMostSignificantBits());
		bb.putLong(uid.getLeastSignificantBits());
		return bb.array();
	}
	
	/**
	 * Makes the UUID bytes to UUID string conversion.
	 * @param d UUID bytes.
	 * @return UUID string.
	 */
	public static String uuidBytesToString(byte[] d) {
		ByteBuffer bb = ByteBuffer.wrap(d);
		return new UUID(bb.getLong(), bb.getLong()).toString();
	}
	
	/**
	 * Makes the UUID bytes to UUID conversion.
	 * @param d UUID bytes.
	 * @return UUID object.
	 */
	public static UUID uuidBytesToUuid(byte[] d) {
		ByteBuffer bb = ByteBuffer.wrap(d);
		return new UUID(bb.getLong(), bb.getLong());
	}
	
	/**
	 * Get the last part.
	 * @param path Source path.
	 * @param sep Separator.
	 * @return Last path name.
	 */
	public static String lastPart(String path, char sep) {
		if (path == null) return null; else {
			int i = path.lastIndexOf(sep);
			return i < 0 ? path : path.substring(i + 1, path.length());
		}
	}	
}
