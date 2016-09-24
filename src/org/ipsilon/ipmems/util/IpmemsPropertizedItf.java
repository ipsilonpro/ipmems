package org.ipsilon.ipmems.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
 * IPMEMS object with properties interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsPropertizedItf {
	/**
	 * Get the property by key.
	 * @param key Property key.
	 * @return Property value.
	 */
	public Object get(String key);
	
	/**
	 * Get the property by key.
	 * @param key Property key.
	 * @param def Default value.
	 * @return Property value.
	 */
	public Object get(String key, Object def);
	
	/**
	 * Get the substituted value.
	 * @param key Property key.
	 * @param def Default value.
	 * @return Property value.
	 */
	public String substituted(String key, String def);
	
	/**
	 * Get the property by key.
	 * @param <T> Property type.
	 * @param cl Property type class.
	 * @param key Property key.
	 * @param def Default value.
	 * @return Property value.
	 */
	public <T> T get(Class<T> cl, String key, T def);
	
	/**
	 * Get the property by key.
	 * @param <T> Property type.
	 * @param cl Property type class.
	 * @param key Property key.
	 * @return Property value.
	 */
	public <T> T get(Class<T> cl, String key);
	
	/**
	 * Evaluates the value by key.
	 * @param key Property key.
	 * @return Property value.
	 * @throws Exception An exception.
	 */
	public Object eval(String key) throws Exception;
	
	/**
	 * Evaluates the value by key.
	 * @param key Property key.
	 * @param def Default value.
	 * @return Property value.
	 * @throws Exception An exception.
	 */
	public Object eval(String key, Object def) throws Exception;
	
	/**
	 * Evaluates the value by key.
	 * @param <T> Return type.
	 * @param c Return type class.
	 * @param key Property key.
	 * @param def Default value.
	 * @return Property value.
	 * @throws Exception An exception.
	 */
	public <T> T eval(Class<T> c, String key, T def) throws Exception;
	
	/**
	 * Checks if the property given by key exists.
	 * @param key Propery key.
	 * @return Existence flag.
	 */
	public boolean containsKey(String key);
	
	/**
	 * Set the property value.
	 * @param key Property key.
	 * @param v Property value.
	 * @return Old value.
	 */
	public Object put(String key, Object v);
	
	/**
	 * Get the property names.
	 * @return Property names.
	 */
	public Set<String> getPropertyKeys();
	
	/**
	 * Removes the entry by key.
	 * @param key Entry key.
	 * @return Old value.
	 */
	public Object removeKey(String key);
	
	/**
	 * Removes the key.
	 * @param key Entry key.
	 * @param def Default value.
	 * @return Old value.
	 */
	public Object removeKey(String key, Object def);
	
	/**
	 * Removes the key.
	 * @param <T> Value type.
	 * @param c Value class.
	 * @param key Parameter key.
	 * @param def Default value.
	 * @return Old value.
	 */
	public <T> T removeKey(Class<T> c, String key, T def);
	
	/**
	 * Removes the key.
	 * @param <T> Value type.
	 * @param c Value class.
	 * @param key Parameter key.
	 * @return Old value.
	 */
	public <T> T removeKey(Class<T> c, String key);
	
	/**
	 * Clears all the keys in this object.
	 */
	public void clearKeys();
	
	/**
	 * Get the property values.
	 * @return Property values.
	 */
	public Collection<Object> getPropertyValues();
	
	/**
	 * Get the property map.
	 * @return Property map.
	 */
	public Map<String,Object> getMap();
	
		/**
	 * Groovy-specific get-method.
	 * @param key Parameter key.
	 * @return Parameter value.
	 */
	public Object getAt(String key);
		
	/**
	 * Groovy-specific put-method.
	 * @param key Parameter key.
	 * @param value Parameter value.
	 */
	public Object putAt(String key, Object value);
				
	/**
	 * Checks if the given object contains in this object.
	 * @param obj Any object (map key or child object).
	 * @return Checking result.
	 */
	public boolean isCase(Object obj);
}
