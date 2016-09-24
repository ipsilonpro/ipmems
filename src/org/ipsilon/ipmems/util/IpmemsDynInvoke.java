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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import org.ipsilon.ipmems.logging.IpmemsLoggers;

/**
 * IPMEMS dynamic invocation library.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDynInvoke {
	private static Object cast(Class<?> c, Object v) {
		if (v == null) return null;
		else if (c.isInstance(v)) return v;
		else if (c.isArray()) return toArray(c.getComponentType(), v);
		else if (c == int.class || c == Integer.class) return toInt(v);
		else if (c == float.class || c == Float.class) return toFloat(v);
		else if (c == double.class || c == Double.class) return toDouble(v);
		else if (c == short.class || c == Short.class) return toShort(v);
		else if (c == byte.class || c == Byte.class) return toByte(v);
		else if (c == long.class || c == Long.class) return toLong(v);
		else if (c == char.class || c == Character.class) return toChar(v);
		else if (c == boolean.class || c == Boolean.class) return toBool(v);
		else if (c == Date.class) return toDate(v);
		else if (c == java.sql.Date.class) return toSqlDate(v);
		else if (c == Time.class) return toSqlTime(v);
		else if (c == Timestamp.class) return toSqlTimestamp(v);
		else if (c == List.class) return toList(v);
		else if (c == Set.class) return toSet(v);
		else if (c == SortedSet.class) return toSortedSet(v);
		else if (Collection.class.isAssignableFrom(c) && !c.isInterface()) 
			return toCollection(c, v);
		else throw new IllegalArgumentException();
	}
	
	/**
	 * Invokes the static method dynamically.
	 * @param c Target class.
	 * @param m Method name.
	 * @param a Method arguments.
	 * @return Returned value.
	 * @throws Exception An exception.
	 */
	public static Object invokeStatic(Class<?> c, String m, Object ... a)
			throws Exception {
		if (a == null) a = new Object[0];
		for (Method mt: c.getMethods()) if (m.equals(mt.getName())) {
			int n;
			Class<?>[] cs = mt.getParameterTypes();
			Object[] cargs;
			if (mt.isVarArgs()) {
				n = cs.length - 1;
				if (a.length < n) continue;
				cargs = new Object[n + 1];
				try {
					for (int i = 0; i < n; i++) cargs[i] = cast(cs[i], a[i]);
				} catch (IllegalArgumentException x) {
					continue;
				}
				Object[] rargs = Arrays.copyOfRange(a, n, a.length);
				try {
					cargs[n] = cast(cs[n], rargs);
				} catch (IllegalArgumentException x) {
					continue;
				}
			} else {
				n = cs.length;
				if (n != a.length) continue;
				cargs = new Object[n];
				try {
					for (int i = 0; i < n; i++) cargs[i] = cast(cs[i], a[i]);
				} catch (IllegalArgumentException x) {
					continue;
				}
			}
			return mt.invoke(null, cargs);
		}
		throw new NoSuchMethodException(c.toString() + ":" + m +
				Arrays.deepToString(a));
	}
	
	/**
	 * Invokes the method dynamically.
	 * @param t Target object.
	 * @param m Method name.
	 * @param a Method arguments.
	 * @return Returned value.
	 * @throws Exception An exception.
	 */
	public static Object invoke(Object t, String m, Object ... a) 
			throws Exception {
		if (a == null) a = new Object[0];
		for (Method mt: t.getClass().getMethods()) if (m.equals(mt.getName())) {
			int n;
			Class<?>[] cs = mt.getParameterTypes();
			Object[] cargs;
			if (mt.isVarArgs()) {
				n = cs.length - 1;
				if (a.length < n) continue;
				cargs = new Object[n + 1];
				try {
					for (int i = 0; i < n; i++) cargs[i] = cast(cs[i], a[i]);
				} catch (IllegalArgumentException x) {
					continue;
				}
				Object[] rargs = Arrays.copyOfRange(a, n, a.length);
				try {
					cargs[n] = cast(cs[n], rargs);
				} catch (IllegalArgumentException x) {
					continue;
				}
			} else {
				n = cs.length;
				if (n != a.length) continue;
				cargs = new Object[n];
				try {
					for (int i = 0; i < n; i++) cargs[i] = cast(cs[i], a[i]);
				} catch (IllegalArgumentException x) {
					continue;
				}
			}
			return mt.invoke(t, cargs);
		}
		throw new NoSuchMethodException(t.getClass().toString() + ":" + m +
				Arrays.deepToString(a));
	}
	
	/**
	 * Makes a new instance.
	 * @param <T> Instance type.
	 * @param c Instance class.
	 * @param a Constructor arguments.
	 * @return New instance.
	 * @throws Exception An exception.
	 */
	public static <T> T newInstance(Class<T> c, Object ... a) throws Exception {
		if (a == null) a = new Object[0];
		for (Constructor ct: c.getDeclaredConstructors()) {
			int n;
			Class<?>[] cs = ct.getParameterTypes();
			Object[] cargs;
			if (ct.isVarArgs()) {
				n = cs.length - 1;
				if (a.length < n) continue;
				cargs = new Object[n + 1];
				try {
					for (int i = 0; i < n; i++) cargs[i] = cast(cs[i], a[i]);
				} catch (IllegalArgumentException x) {
					continue;
				}
				Object[] rargs = Arrays.copyOfRange(a, n, a.length);
				try {
					cargs[n] = cast(cs[n], rargs);
				} catch (IllegalArgumentException x) {
					continue;
				}
			} else {
				n = cs.length;
				if (n != a.length) continue;
				cargs = new Object[n];
				try {
					for (int i = 0; i < n; i++) cargs[i] = cast(cs[i], a[i]);
				} catch (IllegalArgumentException x) {
					continue;
				}
			}
			return c.cast(ct.newInstance(cargs));
		}
		throw new NoSuchMethodException();
	}
		
	/**
	 * Fills the properties.
	 * @param log Logger name.
	 * @param t Target object.
	 * @param pm Properties map.
	 */
	@SuppressWarnings("unchecked")
	public static void fill(String log, Object t, Map pm) {
		for (Map.Entry<Object,Object> oe: 
				((Map<Object,Object>)pm).entrySet()) try {
			String k = String.valueOf(oe.getKey());
			String m = "set" + k.substring(0, 1).toUpperCase() + k.substring(1);
			invoke(t, m, oe.getValue());
		} catch (Throwable x) {
			IpmemsLoggers.info(log, "{0} Set prop {1}", x, t, oe);
		}
	}
	
	private static Integer toInt(Object v) {
		if (v instanceof Number) return ((Number)v).intValue();
		else throw new IllegalArgumentException();
	}
	
	private static Float toFloat(Object v) {
		if (v instanceof Number) return ((Number)v).floatValue();
		else throw new IllegalArgumentException();
	}
	
	private static Double toDouble(Object v) {
		if (v instanceof Number) return ((Number)v).doubleValue();
		else throw new IllegalArgumentException();
	}
	
	private static Short toShort(Object v) {
		if (v instanceof Number) return ((Number)v).shortValue();
		else throw new IllegalArgumentException();
	}
	
	private static Byte toByte(Object v) {
		if (v instanceof Number) return ((Number)v).byteValue();
		else throw new IllegalArgumentException();
	}
	
	private static Long toLong(Object v) {
		if (v instanceof Number) return ((Number)v).longValue();
		else throw new IllegalArgumentException();
	}
	
	private static Character toChar(Object v) {
		if (v instanceof Character) return (Character)v;
		else if (v instanceof CharSequence && ((CharSequence)v).length() == 1)
			return ((CharSequence)v).charAt(0);
		else throw new IllegalArgumentException();
	}
	
	private static Boolean toBool(Object v) {
		if (v instanceof Boolean) return (Boolean)v;
		else throw new IllegalArgumentException();
	}
	
	private static Object toArray(Class<?> c, Object v) {
		if (v.getClass().isArray()) {
			int n = Array.getLength(v);
			Object arr = Array.newInstance(c, n);
			for (int i = 0; i < n; i++)
				Array.set(arr, i, cast(c, Array.get(v, i)));
			return arr;
		} else if (v instanceof Collection) {
			return toArray(c, ((Collection)v).toArray());
		} else if (v instanceof Iterable) {
			ArrayList<Object> a = new ArrayList<Object>();
			for (Object o: (Iterable)v) a.add(o);
			return toArray(c, a);
		} else throw new IllegalArgumentException();
	}
	
	private static Date toDate(Object v) {
		if (v instanceof Number) return new Date(((Number)v).longValue());
		else if (v instanceof Calendar) return ((Calendar)v).getTime();
		else throw new IllegalArgumentException();
	}
	
	private static java.sql.Date toSqlDate(Object v) {
		if (v instanceof Number)
			return new java.sql.Date(((Number)v).longValue());
		else if (v instanceof Calendar)
			return new java.sql.Date(((Calendar)v).getTimeInMillis());
		else if (v instanceof Date)
			return new java.sql.Date(((Date)v).getTime());
		else throw new IllegalArgumentException();
	}
	
	private static Time toSqlTime(Object v) {
		if (v instanceof Number) return new Time(((Number)v).longValue());
		else if (v instanceof Calendar)
			return new Time(((Calendar)v).getTimeInMillis());
		else if (v instanceof Date)	return new Time(((Date)v).getTime());
		else throw new IllegalArgumentException();
	}
	
	private static Timestamp toSqlTimestamp(Object v) {
		if (v instanceof Number) return new Timestamp(((Number)v).longValue());
		else if (v instanceof Calendar)
			return new Timestamp(((Calendar)v).getTimeInMillis());
		else if (v instanceof Date)	return new Timestamp(((Date)v).getTime());
		else throw new IllegalArgumentException();
	}
	
	@SuppressWarnings("unchecked")
	private static Collection toCollection(Class<?> c, Object v) {
		try {
			Collection l = (Collection)c.newInstance();
			if (v instanceof Iterable) {
				for (Object o: (Iterable)v) l.add(o);
				return l;
			} else if (v.getClass().isArray()) {
				int n = Array.getLength(v);
				for (int i = 0; i < n; i++) l.add(Array.get(v, i));
				return l;
			} else throw new IllegalArgumentException();
		} catch (Exception x) {
			throw new IllegalArgumentException(x);
		}
	}
	
	private static List<?> toList(Object v) {
		if (v instanceof Iterable) {
			ArrayList<Object> l = new ArrayList<Object>();
			for (Object o: (Iterable)v) l.add(o);
			return l;
		} else if (v.getClass().isArray()) {
			ArrayList<Object> l = new ArrayList<Object>();
			int n = Array.getLength(v);
			for (int i = 0; i < n; i++) l.add(Array.get(v, i));
			return l;
		} else throw new IllegalArgumentException();
	}
	
	private static Set<?> toSet(Object v) {
		if (v instanceof Iterable) {
			LinkedHashSet<Object> l = new LinkedHashSet<Object>();
			for (Object o: (Iterable)v) l.add(o);
			return l;
		} else if (v.getClass().isArray()) {
			LinkedHashSet<Object> l = new LinkedHashSet<Object>();
			int n = Array.getLength(v);
			for (int i = 0; i < n; i++) l.add(Array.get(v, i));
			return l;
		} else throw new IllegalArgumentException();
	}
	
	private static SortedSet<?> toSortedSet(Object v) {
		if (v instanceof Iterable) {
			TreeSet<Object> l = new TreeSet<Object>();
			for (Object o: (Iterable)v) l.add(o);
			return l;
		} else if (v.getClass().isArray()) {
			TreeSet<Object> l = new TreeSet<Object>();
			int n = Array.getLength(v);
			for (int i = 0; i < n; i++) l.add(Array.get(v, i));
			return l;
		} else throw new IllegalArgumentException();
	}
}
