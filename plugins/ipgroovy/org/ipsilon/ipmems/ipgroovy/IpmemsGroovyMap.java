package org.ipsilon.ipmems.ipgroovy;

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

import java.util.HashMap;
import java.util.Map;
import static org.ipsilon.ipmems.scripting.IpmemsScriptEngines.userMap;

/**
 * IPMEMS groovy binding.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsGroovyMap extends HashMap<Object,Object> {
	/**
	 * Default constructor.
	 */
	public IpmemsGroovyMap() {
		super(8);
	}
	
	/**
	 * Constructs the groovy binding.
	 * @param ps Properties map.
	 */
	public IpmemsGroovyMap(Map<Object,Object> ps) {
		super(ps);
	}

	@Override
	public Object get(Object key) {
		return super.containsKey(key) ? super.get(key) : userMap.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(key) ? true : userMap.containsKey(key);
	}
}
