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

package com.ipsilon.ipmems.iprhino;

import static org.ipsilon.ipmems.scripting.IpmemsScriptEngines.userMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

/**
 * IPMEMS Rhino importer.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRhinoImporter extends ImporterTopLevel {
	/**
	 * Constructs the Rhino importer.
	 * @param cx Context.
	 */
	public IpmemsRhinoImporter(Context cx) {
		super(cx);
	}

	@Override
	public Object get(String n, Scriptable s) {
		return super.has(n, s) ? super.get(n, s) :
				Context.javaToJS(userMap.get(n), s);
	}

	@Override
	public boolean has(String n, Scriptable s) {
		return super.has(n, s) ? true : userMap.containsKey(n);
	}
}
