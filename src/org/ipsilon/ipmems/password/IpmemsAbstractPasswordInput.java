package org.ipsilon.ipmems.password;

import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

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
 * IPMEMS abstract password input class.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsAbstractPasswordInput implements
		IpmemsPasswordInput {
	@Override
	public Object getUserData() {
		return null;
	}

	@Override
	public void setUserData(Object data) {
	}
	
	/**
	 * Get the default password input.
	 * @return Default password input object.
	 */
	public static IpmemsPasswordInput getDefault() {
		try {
			Class<?> c = Ipmems.has("securePIC") ?
					IpmemsScriptEngines.loadClass(
						Ipmems.get("securePIC").toString()) :
					IpmemsPropPasswordInput.class;
			return (IpmemsPasswordInput)c.newInstance();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}
}
