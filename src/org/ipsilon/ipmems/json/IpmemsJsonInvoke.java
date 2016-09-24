package org.ipsilon.ipmems.json;

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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * IPMEMS dynamic invocation library.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsJsonInvoke {
	/**
	 * Casts the arguments.
	 * @param types Argument types.
	 * @param args Source arguments.
	 * @return Arguments.
	 */
	public static Object[] castArguments(Class<?>[] types, Object ... args) {
		if (types.length != args.length) return null;
		Object[] r = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			Object v = castArgument(types[i], args[i]);
			if (v instanceof Exception) return null; else r[i] = v;
		}
		return r;
	}
	
	/**
	 * Cast the argument.
	 * @param t Argument type.
	 * @param a Source argument.
	 * @return Result.
	 */
	public static Object castArgument(Class<?> t, Object a) {
		if (a == null) return a;
		else if (t.isInstance(a)) return a;
		else if (a instanceof Number) {
			if (t == int.class) return ((Number)a).intValue();
			else if (t == byte.class) return ((Number)a).byteValue();
			else if (t == short.class) return ((Number)a).shortValue();
			else if (t == float.class) return ((Number)a).floatValue();
			else if (t == double.class) return ((Number)a).doubleValue();
			else if (t == long.class) return ((Number)a).longValue();
			else if (t == Integer.class) return ((Number)a).intValue();
			else if (t == Byte.class) return ((Number)a).byteValue();
			else if (t == Short.class) return ((Number)a).shortValue();
			else if (t == Float.class) return ((Number)a).floatValue();
			else if (t == Double.class) return ((Number)a).doubleValue();
			else if (t == Long.class) return ((Number)a).longValue();
			else return new IllegalArgumentException();
		} else if ((t == char.class || t == Character.class) 
				&& a instanceof String && ((String)a).length() == 1)
			return ((String)a).charAt(0);
		else if (t == boolean.class && a instanceof Boolean)
			return ((Boolean)a).booleanValue();
		else if (t.isArray() && !t.getComponentType().isPrimitive() && 
				a instanceof List) {
			Object array = Array.newInstance(
					t.getComponentType(), ((List)a).size());
			for (int i = 0; i < ((List)a).size(); i++)
				Array.set(array, i, ((List)a).get(i));
			return array;
		} else return new IllegalArgumentException();
	}
	
	/**
	 * Invokes the method via Java reflection.
	 * @param c Target class.
	 * @param target Target object.
	 * @param method Method name.
	 * @param args Method arguments.
	 * @return Result.
	 * @throws Exception An exception during the invocation.
	 */
	public static Object invoke(Class<?> c, Object target, String method,
			Object ... args) throws Exception {
		for (Method m: c.getMethods()) if (m.getName().equals(method)) {
			Object[] v = castArguments(m.getParameterTypes(), args);
			if (v != null) return m.invoke(target, v);
		}
		String m = String.format("%s.%s(%s:%s)", c, method,
				args.getClass(), Arrays.deepToString(args));
		throw new IllegalStateException(m.toString());
	}
}
