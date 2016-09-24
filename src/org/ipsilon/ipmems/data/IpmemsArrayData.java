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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import org.ipsilon.ipmems.logging.IpmemsLogRec;

/**
 * IPMEMS array data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsArrayData extends IpmemsAbstractData<Object> {
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		byte t;
		Class<?> ct = ct(getData().getClass());
		if (int.class.equals(ct)) t = 'i';
		else if (byte.class.equals(ct)) t = 'b';
		else if (short.class.equals(ct)) t = 's';
		else if (double.class.equals(ct)) t = 'd';
		else if (float.class.equals(ct)) t = 'f';
		else if (char.class.equals(ct)) t = 'c';
		else if (boolean.class.equals(ct)) t = 'z';
		else if (long.class.equals(ct)) t = 'l';
		else if (String.class.isAssignableFrom(ct)) t = 'N';
		else if (Properties.class.isAssignableFrom(ct)) t = 'P';
		else if (Map.class.isAssignableFrom(ct)) t = 'M';
		else if (Collection.class.isAssignableFrom(ct)) t = 'C';
		else if (Calendar.class.isAssignableFrom(ct)) t = 'Q';
		else if (Timestamp.class.isAssignableFrom(ct)) t = '|';
		else if (java.sql.Date.class.isAssignableFrom(ct)) t = 'W';
		else if (Time.class.isAssignableFrom(ct)) t = 'T';
		else if (Date.class.isAssignableFrom(ct)) t = 'G';
		else if (BigInteger.class.isAssignableFrom(ct)) t = 'R';
		else if (BigDecimal.class.isAssignableFrom(ct)) t = 'X';
		else if (Class.class.isAssignableFrom(ct)) t = '!';
		else if (Externalizable.class.isAssignableFrom(ct)) t = 'E';
		else if (Iterable.class.isAssignableFrom(ct)) t = 'A';
		else if (Locale.class.isAssignableFrom(ct)) t = 'U';
		else if (IpmemsLogRec.class.isAssignableFrom(ct)) t = 'V';
		else if (Throwable.class.isAssignableFrom(ct)) t = '@';
		else if (UUID.class.isAssignableFrom(ct)) t = 'Y';
		else if (Integer.class.isAssignableFrom(ct)) t = 'I';
		else if (Long.class.isAssignableFrom(ct)) t = 'L';
		else if (Boolean.class.isAssignableFrom(ct)) t = 'Z';
		else if (Byte.class.isAssignableFrom(ct)) t = 'B';
		else if (Short.class.isAssignableFrom(ct)) t = 'S';
		else if (Float.class.isAssignableFrom(ct)) t = 'F';
		else if (Double.class.isAssignableFrom(ct)) t = 'D';
		else if (Character.class.isAssignableFrom(ct)) t = 'H';
		else t = 'O';
		out.writeByte(t);
		int dim = dim(getData().getClass(), 0);
		out.writeByte(dim);
		int n = Array.getLength(getData());
		out.writeInt(n);
		for (int i = 0; i < n; i++)
			out.writeObject(IpmemsDataTypes.wrap(Array.get(getData(), i)));
	}
	
	@Override
	public void readExternal(ObjectInput in) throws 
			IOException, ClassNotFoundException {
		Class<?> c;
		byte t = in.readByte();
		switch (t) {
			case 'i':
				c = int.class;
				break;
			case 'b':
				c = byte.class;
				break;
			case 's':
				c = short.class;
				break;
			case 'd':
				c = double.class;
				break;
			case 'f':
				c = float.class;
				break;
			case 'c':
				c = char.class;
				break;
			case 'z':
				c = boolean.class;
				break;
			case 'l':
				c = long.class;
				break;
			case 'N':
				c = String.class;
				break;
			case 'P':
				c = Properties.class;
				break;
			case 'M':
				c = Map.class;
				break;
			case 'C':
				c = Collection.class;
				break;
			case 'Q':
				c = Calendar.class;
				break;
			case '|':
				c = Timestamp.class;
				break;
			case 'W':
				c = java.sql.Date.class;
				break;
			case 'T':
				c = Time.class;
				break;
			case 'G':
				c = Date.class;
				break;
			case 'R':
				c = BigInteger.class;
				break;
			case 'X':
				c = BigDecimal.class;
				break;
			case '!':
				c = Class.class;
				break;
			case 'E':
				c = Externalizable.class;
				break;
			case 'A':
				c = Iterable.class;
				break;
			case 'U':
				c = Locale.class;
				break;
			case 'V':
				c = IpmemsLogRec.class;
				break;
			case '@':
				c = Throwable.class;
				break;
			case 'Y':
				c = UUID.class;
				break;
			case 'I':
				c = Integer.class;
				break;
			case 'L':
				c = Long.class;
				break;
			case 'Z':
				c = Boolean.class;
				break;
			case 'B':
				c = Byte.class;
				break;
			case 'S':
				c = Short.class;
				break;
			case 'F':
				c = Float.class;
				break;
			case 'D':
				c = Double.class;
				break;
			case 'H':
				c = Character.class;
				break;
			default:
				c = Object.class;
				break;
		}
		int[] dims = new int[in.readByte() & 0xFF];
		dims[0] = in.readInt();
		Object a = Array.newInstance(c, dims);
		for (int i = 0; i < dims[0]; i++)
			Array.set(a, i, ((IpmemsData)in.readObject()).getData());
		setData(a);
	}
		
	private static Class<?> ct(Class c) {
		return c.isArray() ? ct(c.getComponentType()) : c;
	}
	
	private static int dim(Class c, int n) {
		return c.isArray() ? dim(c.getComponentType(), n + 1) : n;
	}	
}
