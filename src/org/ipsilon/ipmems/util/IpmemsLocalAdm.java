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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS local administration.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLocalAdm implements IpmemsAdm {
	@Override
	public Map<String,Set<String>> getMethodMap() {
		TreeMap<String,Set<String>> mm = new TreeMap<String,Set<String>>();
		for (Entry<String,Object> e: IpmemsScriptEngines.userMap.entrySet()) {
			TreeSet<String> c = new TreeSet<String>();
			for (Method m: e.getValue().getClass().getMethods())
				if (m.getParameterTypes().length == 0) c.add(m.getName());
			for (Method m: e.getValue().getClass().getDeclaredMethods())
				if (m.getParameterTypes().length == 0) c.add(m.getName());
			mm.put(e.getKey(), c);
		}
		return mm;
	}

	@Override
	public Object invoke(String obj, String method) throws Exception {
		Object o = IpmemsScriptEngines.userMap.get(obj);
		if (o == null) throw new IllegalArgumentException(obj);
		try {
			Method m = o.getClass().getDeclaredMethod(method);
			return m.invoke(o);
		} catch (NoSuchMethodException x) {
			Method m = o.getClass().getMethod(method);
			return m.invoke(o);
		}
	}
}
