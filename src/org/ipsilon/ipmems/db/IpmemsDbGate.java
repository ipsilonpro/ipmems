package org.ipsilon.ipmems.db;

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
import java.util.Map;

/**
 * IPMEMS DB gate interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsDbGate<T> extends Closeable {
	/**
	 * Set the gate object.
	 * @param c gate object.
	 */
	public void setGateObject(T c);
	
	/**
	 * Get the gate object.
	 * @return Gate object.
	 */
	public T getGateObject();
	
	/**
	 * Get the gate function.
	 * @param k Function key.
	 * @param a Function argument.
	 * @return Gate function.
	 */
	public Map<String,Object> gf(String k, Map a);
}
