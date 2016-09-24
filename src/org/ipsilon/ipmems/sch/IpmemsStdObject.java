package org.ipsilon.ipmems.sch;

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

import java.io.Closeable;
import java.util.*;
import static java.util.Collections.EMPTY_MAP;
import org.ipsilon.ipmems.IpmemsStrings;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS scheduler object.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public abstract class IpmemsStdObject extends 
		IpmemsPropertized implements IpmemsObjItf {
	/**
	 * Constructs the object.
	 * @param p Parent object.
	 * @param n Object name.
	 * @param props Object properties.
	 */
	public IpmemsStdObject(IpmemsStdObject p, String n, Map props) {
		super(props.containsKey("params") ? 
				(Map)props.get("params") : EMPTY_MAP);
		parent = p;
		name = p == null ? n : p.name + "/" + n;
		function = initFunc(props.get("func"));
	}
	
	private Object initFunc(Object f) {
		final Object fn;
		if (f == null) fn = null;
		else if (IpmemsScriptEngines.isFunction(f)) fn = f;
		else if (f instanceof String) 
			fn = IpmemsScriptEngines.findFunctionByName((String)f);
		else if (f instanceof Collection) {
			Object r = null;
			ArrayList<Object> l = new ArrayList<Object>();
			for (Object o: (Collection)f)
				if (IpmemsScriptEngines.isFunction(o)) l.add(o);
				else if (o instanceof String) {
					Object func = IpmemsScriptEngines.
							findFunctionByName((String)o);
					if (func != null) l.add(func);
				}
			Object c;
			switch (l.size()) {
				case 0:
					break;
				case 1:
					r = l.get(0);
					break;
				default:
					c = l.get(0);
					for (int i = 1; i < l.size(); i++) try {
						c = IpmemsScriptEngines.composeCurry(c, l.get(i), this);
					} catch (Exception x) {
						IpmemsLoggers.warning("err", "{0} ({1})", x, name, i);
					}
					r = c;
					break;
			}
			fn = r;
		} else fn = null;
		return fn;
	}
			
	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final String id() {
		return IpmemsStrings.lastPart(name, '/');
	}

	@Override
	public final Object getFunction() {
		return function;
	}

	@Override
	public final IpmemsStdObject getParent() {
		return parent;
	}

	@Override
	public Set<String> getKeys() {
		return getPropertyKeys();
	}
	
	@Override
	public final IpmemsStdScheduler getRoot() {
		IpmemsStdObject o = this;
		while (o != null)
			if (o instanceof IpmemsStdScheduler) return (IpmemsStdScheduler)o;
			else o = o.getParent();
		return null;
	}
	
	@Override
	public Long call() throws Exception {
		if (function == null) return 0L; else {
			long s = System.currentTimeMillis();
			IpmemsScriptEngines.call(function, this, EMPTY_MAP);
			return System.currentTimeMillis() - s;
		}
	}

	@Override
	public IpmemsStdObject getObject(String path) {
		if (path == null) return null;
		else if (path.isEmpty() || path.equals(".")) return this;
		else if (path.startsWith("/")) 
			return getRoot().getObject(path.substring(1));
		else if (path.equals("..")) return getParent();
		else if (path.startsWith("../") && getParent() != null)
			return getParent().getObject(path.substring(3));
		else return null;
	}

	@Override
	public void stop() {
		for (Object o: getPropertyValues()) if (o instanceof Closeable) try {
			((Closeable)o).close();
		} catch (Exception x) {}
	}

	@Override
	public Map<String,Object> drain(Map<String,Object> m) {
		HashMap<String,Object> r = new HashMap<String,Object>(getMap());
		r.putAll(m);
		return r;
	}
		
	@Override
	public final String toString() {
		return name;
	}
		
	private final IpmemsStdObject parent;
	private final String name;
	private final Object function;		
}
