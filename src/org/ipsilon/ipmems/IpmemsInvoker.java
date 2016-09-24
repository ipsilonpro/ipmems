package org.ipsilon.ipmems;

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
import java.util.ArrayList;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS bridge invoker.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsInvoker {
	/**
	 * Invokes the user class.
	 * @param args Command-line arguments.
	 * @throws Exception An exception.
	 */
	public static void main(String[] args) throws Exception {
		String cl = null;
		ArrayList<String> l = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-z") || args[i].equals("--tz")) {
				i++;
				continue;
			}
			if (args[i].equals("-l") || args[i].equals("--locale")) {
				i++;
				continue;
			}
			if (args[i].equals("-q") || args[i].equals("--level")) {
				i++;
				continue;
			}
			if (args[i].equals("-c") || args[i].equals("--class")) {
				cl = args[i + 1];
				i++;
				continue;
			}
			l.add(args[i]);
		}
		if (cl == null) throw new IllegalArgumentException("Class is null");
		Class<?> c = IpmemsScriptEngines.loadClass(cl);
		Method m = c.getDeclaredMethod("main", String[].class);
		m.invoke(null, (Object)l.toArray(new String[l.size()]));
	}
}
