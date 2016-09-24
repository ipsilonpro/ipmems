package org.ipsilon.ipmems.data;

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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.Arrays;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS class data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsClassData extends IpmemsAbstractData<Class<?>> {
	@Override
	public void writeExternal(ObjectOutput o) throws IOException {
		o.writeUTF(getData().getName());
	}

	@Override
	public void readExternal(ObjectInput in) throws 
			IOException, ClassNotFoundException {
		String cn = in.readUTF();
		if (int.class.getName().equals(cn)) setData(int.class);
		else if (short.class.getName().equals(cn)) setData(short.class);
		else if (long.class.getName().equals(cn)) setData(long.class);
		else if (byte.class.getName().equals(cn)) setData(byte.class);
		else if (boolean.class.getName().equals(cn)) setData(boolean.class);
		else if (double.class.getName().equals(cn)) setData(double.class);
		else if (float.class.getName().equals(cn)) setData(float.class);
		else if (char.class.getName().equals(cn)) setData(char.class);
		else if (void.class.getName().equals(cn)) setData(void.class);
		else if (cn != null && cn.startsWith("[")) {
			int n = 0;
			int l = cn.length();
			for (int i = 0; i < l; i++)
				if (cn.charAt(i) == '[') n++; else break;
			int[] dims = new int[n];
			Arrays.fill(dims, 0);
			if (cn.endsWith(";")) {
				String nm = cn.substring(cn.lastIndexOf('[') + 2, l - 1);
				Class<?> c;
				try {
					c = IpmemsScriptEngines.loadClass(nm);
				} catch (Exception x) {
					c = Void.class;
				}
				setData(Array.newInstance(c, dims).getClass());
			} else switch (cn.charAt(l - 1)) {
				case 'I':
					setData(Array.newInstance(int.class, dims).getClass());
					break;
				case 'B':
					setData(Array.newInstance(byte.class, dims).getClass());
					break;
				case 'J':
					setData(Array.newInstance(long.class, dims).getClass());
					break;
				case 'Z':
					setData(Array.newInstance(boolean.class, dims).getClass());
					break;
				case 'S':
					setData(Array.newInstance(short.class, dims).getClass());
					break;
				case 'F':
					setData(Array.newInstance(float.class, dims).getClass());
					break;
				case 'D':
					setData(Array.newInstance(double.class, dims).getClass());
					break;
				case 'C':
					setData(Array.newInstance(char.class, dims).getClass());
					break;
				default:
					setData(Array.newInstance(void.class, dims).getClass());
					break;
				}
		} else try {
			setData(IpmemsScriptEngines.loadClass(cn));
		} catch (Exception x) {
			setData(Void.class);
		}
	}	
}
