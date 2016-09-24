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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.ipsilon.ipmems.IpmemsUtil;

/**
 * IPMEMS database key address utilities.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsDbAddress {
	/**
	 * Encodes the address according to address type.
	 * @param type Address type.
	 * @param address Numeric address.
	 * @return Address as string.
	 */
	public static String encode(IpmemsDbAddressType type, long address) {
		switch (type) {
			case X32:
				return "$" + BigInteger.valueOf(address).toString(32);
			case X36:
				return "!" + BigInteger.valueOf(address).toString(36);
			case P4: {
				ByteBuffer bb = ByteBuffer.allocate(8).putLong(address);
				bb.position(0);
				StringBuilder b = new StringBuilder();
				b.append((int)bb.getChar());
				b.append('.');
				b.append((int)bb.getChar());
				b.append('.');
				b.append((int)bb.getChar());
				b.append('.');
				b.append((int)bb.getChar());
				return b.toString();
			}
			case P8: {
				ByteBuffer bb = ByteBuffer.allocate(8).putLong(address);
				bb.position(0);
				StringBuilder b = new StringBuilder();
				b.append((int)0xFF & bb.get());
				b.append(':');
				b.append((int)0xFF & bb.get());
				b.append(':');
				b.append((int)0xFF & bb.get());
				b.append(':');
				b.append((int)0xFF & bb.get());
				b.append(':');
				b.append((int)0xFF & bb.get());
				b.append(':');
				b.append((int)0xFF & bb.get());
				b.append(':');
				b.append((int)0xFF & bb.get());
				b.append(':');
				b.append((int)0xFF & bb.get());
				return b.toString();
			}
			default:
				return Long.toString(address);
		}
	}
	
	/**
	 * Encodes the address according to address type as string.
	 * @param type Address type.
	 * @param address Numeric address.
	 * @return Encoded address.
	 */
	public static String encode(String type, long address) {
		return encode(IpmemsDbAddressType.valueOf(type.toUpperCase()), address);
	}
		
	/**
	 * Decode the address from string.
	 * @param addr String address.
	 * @return Numeric address.
	 */
	public static long decode(String addr) {
		if (addr.contains(".")) {
			ByteBuffer buf = ByteBuffer.allocate(8);
			String[] ps = addr.split("[.]");
			if (ps.length < 4) buf.put(new byte[(4 - ps.length) * 2]);
			for (String s: ps) buf.putShort(Integer.decode(s).shortValue());
			return buf.getLong(0);
		} else if (addr.contains(":")) {
			ByteBuffer buf = ByteBuffer.allocate(8);
			String[] ps = addr.split(":");
			if (ps.length < 8) buf.put(new byte[8 - ps.length]);
			for (String s: ps) buf.put(Short.decode(s).byteValue());
			return buf.getLong(0);
		} else if (addr.startsWith("/"))
			return IpmemsUtil.hash(addr.substring(1));
		else if (addr.startsWith("$"))
			return new BigInteger(addr.substring(1), 32).longValue();
		else if (addr.startsWith("!"))
			return new BigInteger(addr.substring(1), 36).longValue();
		else return Long.decode(addr);
	}	
}
