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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsObservable;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS observable propertized object.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsObservablePropertized extends 
		IpmemsObservable implements IpmemsPropertizedItf {
	/**
	 * Default constructor.
	 */
	public IpmemsObservablePropertized() {
	}
	
	/**
	 * Constructs the propertized object by given properties map.
	 * @param props Properties map.
	 */
	public IpmemsObservablePropertized(Map props) {
		for (Object k: props.keySet()) 
			p.put(String.valueOf(k), props.get(k));
	}
	
	@Override
	public Object get(String key, Object def) {
		Object v = get(key);
		return v != null ? v : def;
	}

	@Override
	public Object get(String key) {
		return p.get(key);
	}

	@Override
	public Object getAt(String key) {
		return get(key);
	}

	@Override
	public Object put(String key, Object v) {
		return v == null ? removeKey(key) : p.put(key, v);
	}
	
	@Override
	public Object putAt(String key, Object value) {
		return put(key, value);
	}

	@Override
	public boolean isCase(Object obj) {
		return obj == null ? false : containsKey(obj.toString());
	}

	@Override
	public <T> T get(Class<T> cl, String k, T d) {
		Object v = get(k);
		try {
			return v != null ? IpmemsCollections.cast(cl, v) : d;
		} catch (Exception x) {
			throw new IllegalArgumentException(this + ":" + k, x);
		}
	}

	@Override
	public <T> T get(Class<T> cl, String k) {
		return get(cl, k, null);
	}

	@Override
	public String substituted(String key, String def) {
		Object v = get(key);
		return Ipmems.substituted(v != null ? v.toString() : def);
	}

	@Override
	public boolean containsKey(String key) {
		return p.containsKey(key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return p.keySet();
	}

	@Override
	public Object removeKey(String key) {
		return p.remove(key);
	}

	@Override
	public <T> T removeKey(Class<T> c, String key) {
		Object v = removeKey(key);
		try {
			return IpmemsCollections.cast(c, v);
		} catch (Exception x) {
			throw new IllegalArgumentException(this + "." + key, x);
		}
	}

	@Override
	public Object removeKey(String key, Object def) {
		Object v = removeKey(key);
		return v == null ? def : v;
	}

	@Override
	public <T> T removeKey(Class<T> c, String key, T def) {
		Object v = removeKey(key);
		try {
			return v == null ? def : IpmemsCollections.cast(c, v);
		} catch (Exception x) {
			throw new IllegalArgumentException(this + "." + key, x);
		}
	}

	@Override
	public void clearKeys() {
		p.clear();
	}
	
	@Override
	public Collection<Object> getPropertyValues() {
		return p.values();
	}
	
	/**
	 * Get the property map.
	 * @return Property map.
	 */
	@Override
	public Map<String,Object> getMap() {
		return p;
	}
	
	/**
	 * Get the property value.
	 * @param <T> Property value type.
	 * @param c Property value class.
	 * @param m Source map.
	 * @param k Map key.
	 * @param d Default value.
	 * @return Property value.
	 */
	public static <T> T get(Class<T> c, Map m, Object k, T d) {
		return IpmemsCollections.value(c, m, k, d);
	}
	
	/**
	 * Get the removed property value.
	 * @param <T> Property value type.
	 * @param c Property value class.
	 * @param m Source map.
	 * @param k Map key.
	 * @param d Default value.
	 * @return Property value.
	 */
	public static <T> T remove(Class<T> c, Map m, Object k, T d) {
		return IpmemsCollections.rvalue(c, m, k, d);
	}

	@Override
	public Object eval(String key) throws Exception {
		Object v = get(key);
		return IpmemsScriptEngines.isFunction(v) ?
				IpmemsScriptEngines.call(v, this) : v;
	}

	@Override
	public Object eval(String key, Object def) throws Exception {
		Object v = get(key, def);
		return IpmemsScriptEngines.isFunction(v) ?
				IpmemsScriptEngines.call(v, this) : v;
	}

	@Override
	public <T> T eval(Class<T> c, String key, T def) throws Exception {
		Object v = get(key, def);
		v = IpmemsScriptEngines.isFunction(v) ?
				IpmemsScriptEngines.call(v, this) : v;
		return IpmemsCollections.cast(c, v);
	}

	private final Map<String,Object> p = 
			new ConcurrentSkipListMap<String,Object>();
}
