package org.ipsilon.ipmems.util;

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

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.util.*;
import org.ipsilon.ipmems.IpmemsIntl;

/**
 * IPMEMS utilities over collections.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsCollections {
	/**
	 * Concatenates the arrays.
	 * @param <T> Array element type.
	 * @param c Array element class.
	 * @param ars Arrays.
	 * @return Concatenated array.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] concat(Class<T> c, T[] ... ars) {
		int n = 0;
		for (T[] a: ars) n += a.length;
		T[] r = (T[])Array.newInstance(c, n);
		n = 0;
		for (T[] a: ars) {
			System.arraycopy(a, 0, r, n, a.length);
			n += a.length;
		}
		return r;
	}
	
	/**
	 * Concatenates the arrays.
	 * @param <T> Array element type.
	 * @param head Array of the arrays head.
	 * @param tail Array of the arrays tail.
	 * @return Concatenated array.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] concat(T[] head, T[] ... tail) {
		int n = head.length;
		for (T[] a: tail) n += a.length;
		T[] r = (T[])Array.newInstance(head.getClass().getComponentType(), n);
		System.arraycopy(head, 0, r, 0, head.length);
		n = head.length;
		for (T[] a: tail) {
			System.arraycopy(a, 0, r, n, a.length);
			n += a.length;
		}
		return r;
	}
		
	/**
	 * Get the command option argument.
	 * @param <T> Argument type.
	 * @param a Command arguments.
	 * @param c Argument type class.
	 * @param s Short option.
	 * @param l Long option.
	 * @param def Default option.
	 * @return Command argument.
	 */
	public static <T> T arg(String[] a, Class<T> c, String s, String l, T def) {
		for (int i = 0; i < a.length; i++)
			if (("--" + l).equals(a[i]) || ("-" + s).equals(a[i])) {
				if (i + 1 >= a.length) return def;
				else if (a[i + 1].startsWith("-")) return def;
				else try {
					return cast(c, a[i + 1]);
				} catch (Exception x) {
					x.printStackTrace(System.err);
					return def;
				}
			}
		return def;
	}
	
	/**
	 * Get the command option argument.
	 * @param a Command arguments.
	 * @param s Short option.
	 * @param l Long option.
	 * @param def Default option.
	 * @return Command argument.
	 */
	public static String arg(String[] a, String s, String l, String def) {
		for (int i = 0; i < a.length; i++)
			if (("--" + l).equals(a[i]) || ("-" + s).equals(a[i])) {
				if (i + 1 >= a.length) return def;
				else if (a[i + 1].startsWith("-")) return def;
				else return a[i + 1];
			}
		return def;
	}

	
	/**
	 * Converts an java array internal object.
	 * @param o Internal java array.
	 * @return Object array.
	 */
	public static Object[] array(Object o) {
		if (o != null) {
			if (o instanceof Collection) return ((Collection)o).toArray();
			else if (o.getClass().isArray()) {
				Object[] a = new Object[Array.getLength(o)];
				for (int i = 0; i < a.length; i++) a[i] = Array.get(o, i);
				return a;
			} else return null;
		} else return null;
	}
	
	/**
	 * Get the list from object.
	 * @param o An object.
	 * @return List.
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> list(Object o) {
		if (o == null) return null;
		else if (o.getClass().isArray()) {
			int n = Array.getLength(o);
			ArrayList<Object> l = new ArrayList<Object>(n);
			for (int i = 0; i < n; i++) l.add(Array.get(o, i));
			return l;
		} else if (o instanceof Collection) {
			return new ArrayList<Object>((Collection<Object>)o);
		} else if (o instanceof Iterable) {
			ArrayList<Object> l = new ArrayList<Object>();
			for (Object v: (Iterable)o) l.add(v);
			return l;
		} else return null;
	}
	
	/**
	 * Get bytes from an iterable object.
	 * @param i Iterable object.
	 * @return Bytes array.
	 */
	public static byte[] getBytes(Iterable i) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (Object o: i) if (o instanceof Number) 
			bos.write(((Number)o).byteValue());
		return bos.toByteArray();
	}
	
	/**
	 * Get the bytes from an array.
	 * @param array An array.
	 * @return Bytes array.
	 */
	public static byte[] getBytes(Object array) {
		if (!array.getClass().isArray()) return new byte[0];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int i = 0; i < Array.getLength(array); i++) {
			Object o = Array.get(array, i);
			if (o instanceof Number) bos.write(((Number)o).byteValue());
		}
		return bos.toByteArray();
	}
	
	/**
	 * Get the map property value casted to a specific type.
	 * @param <T> Type.
	 * @param cl Type class.
	 * @param map Source map.
	 * @param key Property key.
	 * @param def Default value.
	 * @return Map property value.
	 */
	public static <T> T value(Class<T> cl, Map<?,?> map, Object key, T def) {
		try {
			return map.containsKey(key) ? cast(cl, map.get(key)) : def;
		} catch (Exception x) {
			throw new IllegalArgumentException(map + ":" + key, x);
		}
	}
	
	/**
	 * Get the removed property value casted to a specific type.
	 * @param <T> Type.
	 * @param cl Type class.
	 * @param map Source map.
	 * @param key Property key.
	 * @param def Default value.
	 * @return Map property value.
	 */
	public static <T> T rvalue(Class<T> cl, Map<?,?> map, Object key, T def) {
		try {
			return map.containsKey(key) ? cast(cl, map.remove(key)) : def;
		} catch (Exception x) {
			throw new IllegalArgumentException(map + ":" + key, x);
		}
	}
		
	/**
	 * Casts the object to the specified class.
	 * @param <T> Target type.
	 * @param cl Target class.
	 * @param v Source object.
	 * @return Target object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Class<T> cl, Object v) throws Exception {
		if (v == null) return null;
		else if (cl.isInstance(v)) return (T)v;
		else if (String.class.isAssignableFrom(cl)) return (T)v.toString();
		else if (cl.isEnum()) {
			return (T)(Enum.valueOf((Class<Enum>)cl, v.toString()));
		} else if (cl == Date.class) {
			if (v instanceof Number) 
				return (T)new Date(((Number)v).longValue());
			else {
				try {
					return (T)IpmemsIntl.parseIso(v.toString());
				} catch (Exception x) {
					try {
						return (T)Timestamp.valueOf(v.toString());
					} catch (Exception y) {
						throw y;
					}
				}
			}
		} else if (Number.class.isAssignableFrom(cl)) {
			if (cl == Integer.class) {
				return (T)Integer.decode(v.toString());
			} else if (cl == Long.class) {
				return (T)Long.decode(v.toString());
			} else if (cl == Short.class) {
				return (T)Short.decode(v.toString());
			} else if (cl == Byte.class) {
				return (T)Byte.decode(v.toString());
			} else if (cl == Float.class) {
				return (T)Float.valueOf(v.toString());
			} else if (cl == Double.class) {
				return (T)Double.valueOf(v.toString());
			} else {
				Constructor<T> c = cl.getDeclaredConstructor(String.class);
				return c.newInstance(v.toString());
			}
		} else if (Boolean.class.isAssignableFrom(cl)) {
			return (T)Boolean.valueOf(v.toString());
		} else {
			Constructor<T> c = cl.getDeclaredConstructor(String.class);
			return c.newInstance(v.toString());
		}
	}
		
	/**
	 * Get the exception map.
	 * @param t An exception.
	 * @return Exception map.
	 */
	public static Map<String,Object> getThrowableMap(Throwable t) {
		LinkedHashMap<String,Object> m = new LinkedHashMap<String,Object>();
		m.put("message", t.getLocalizedMessage());
		m.put("class", t.getClass().getName());
		StackTraceElement[] es = t.getStackTrace();
		if (es != null) {
			List<Map<String,Object>> l = new LinkedList<Map<String,Object>>();
			for (StackTraceElement e: es) {
				LinkedHashMap<String,Object> em =
						new LinkedHashMap<String,Object>();
				if (e.getClassName() != null) em.put("class", e.getClassName());
				if (e.getMethodName() != null)
					em.put("method", e.getMethodName());
				if (e.getFileName() != null) em.put("file", e.getFileName());
				if (e.getLineNumber() >= 0) em.put("line", e.getLineNumber());
				l.add(em);
			}
			m.put("stack", l);
		}
		if (t.getCause() != null) m.put("cause", getThrowableMap(t.getCause()));
		return m;
	}
}
