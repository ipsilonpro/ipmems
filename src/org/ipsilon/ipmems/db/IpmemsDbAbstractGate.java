package org.ipsilon.ipmems.db;

import java.util.HashMap;
import java.util.Map;
import org.ipsilon.ipmems.util.IpmemsCollections;

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

/**
 * IPMEMS database abstract gate.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsDbAbstractGate<T> implements IpmemsDbGate<T> {
	@Override
	public T getGateObject() {
		return gateObject;
	}

	@Override
	public void setGateObject(T c) {
		gateObject = c;
	}
	
	/**
	 * Get the user map.
	 * @param k Key.
	 * @param v Value.
	 * @return User map.
	 */
	protected Map<String,Object> umap(String k, Object v) {
		HashMap<String,Object> m = new HashMap<String,Object>(1);
		m.put(k, v);
		return m;
	}
	
	/**
	 * Get the user map.
	 * @param k First key.
	 * @param v First value.
	 * @param u Second key.
	 * @param z Second value.
	 * @return User map.
	 */
	protected Map<String,Object> umap(String k, Object v, String u, Object z) {
		HashMap<String,Object> m = new HashMap<String,Object>(2);
		m.put(k, v);
		m.put(u, z);
		return m;
	}
	
	/**
	 * Get the value from map.
	 * @param <T> Value type.
	 * @param c Value type class.
	 * @param m Map.
	 * @param k Key.
	 * @param def Default value.
	 * @return Value.
	 */
	protected static <T> T get(Class<T> c, Map m, Object k, T def) {
		return IpmemsCollections.value(c, m, k, def);
	}
	
	@Override
	public void close(){
		gateObject = null;
	}
	
	private volatile T gateObject;
}
