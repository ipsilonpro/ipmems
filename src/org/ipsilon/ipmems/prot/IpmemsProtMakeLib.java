package org.ipsilon.ipmems.prot;

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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import java.util.Collection;
import java.util.zip.CRC32;
import org.ipsilon.ipmems.IpmemsStrings;

/**
 * Make outcoming messages library.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsProtMakeLib {
	/**
	 * Formats the arguments.
	 * @param fmt Format string.
	 * @param args Arguments.
	 * @return Formatted string.
	 */
	public static String fmt(String fmt, Object ... args) {
		return String.format(fmt, args);
	}
	
	/**
	 * Formats the number as binary string with given width.
	 * @param n A number.
	 * @param width Number width.
	 * @return Formatted string.
	 */
	public static String binfmt(Number n, int width) {
		String str = new BigInteger(n.toString()).toString(2);
		return str.length() < width ? 
				IpmemsStrings.repeat('0', width - str.length()) + str :
				str;
	}
	
	/**
	 * Makes the byte from a binary string.
	 * @param s A binary string.
	 * @return A byte.
	 */
	public static byte n2b(String s) {
		return Short.valueOf(s, 2).byteValue();
	}
	
	/**
	 * Makes the byte from a numeric string with given base.
	 * @param s Numeric string.
	 * @param base Given base.
	 * @return A byte.
	 */
	public static byte n2b(String s, int base) {
		return Short.valueOf(s, base).byteValue();
	}
	
	/**
	 * Makes the short from a binary string.
	 * @param s A binary string.
	 * @return A short.
	 */
	public static short n2s(String s) {
		return Integer.valueOf(s, 2).shortValue();
	}
	
	/**
	 * Makes the short from a numeric string with given base.
	 * @param s Numeric string.
	 * @param base Given base.
	 * @return A short.
	 */
	public static short n2s(String s, int base) {
		return Integer.valueOf(s, base).shortValue();
	}
	
	/**
	 * Makes the integer from a binary string.
	 * @param s A binary string.
	 * @return An integer.
	 */
	public static int n2i(String s) {
		return Long.valueOf(s, 2).intValue();
	}
	
	/**
	 * Makes the integer from a numeric string with given base.
	 * @param s Numeric string.
	 * @param base Given base.
	 * @return An integer.
	 */
	public static int n2i(String s, int base) {
		return Long.valueOf(s, base).intValue();
	}
	
	/**
	 * Makes the long from a binary string.
	 * @param s Binary string.
	 * @return A long number.
	 */
	public static long n2l(String s) {
		return new BigInteger(s, 2).longValue();
	}
	
	/**
	 * Makes the long from a numeric string with given base.
	 * @param s Numeric string.
	 * @param base Given base.
	 * @return A long number.
	 */
	public static long n2l(String s, int base) {
		return new BigInteger(s, base).longValue();
	}
	
	/**
	 * Makes the character from a binary string.
	 * @param s Binary string.
	 * @return A character.
	 */
	public static char n2c(String s) {
		return (char)Integer.parseInt(s, 2);
	}
	
	/**
	 * Makes the character from a numeric string with given base.
	 * @param s Numeric string.
	 * @param base Given base.
	 * @return A character.
	 */
	public static char n2c(String s, int base) {
		return (char)Integer.parseInt(s, base);
	}
	
	/**
	 * Makes the hex string bytes.
	 * @param n Source number.
	 * @param w Number width.
	 * @param uppercase Uppercase flag.
	 * @return Hex string bytes.
	 */
	public static byte[] hex(Number n, int w, boolean uppercase) {
		return uppercase ? String.format("%0" + w + "X", n).getBytes() :
				String.format("%0" + w + "x", n).getBytes();
	}
	
	/**
	 * Makes an uppercased hex string bytes.
	 * @param n Source number.
	 * @param w Number width.
	 * @return Hex string bytes.
	 */
	public static byte[] hex(Number n, int w) {
		return hex(n, w, true);
	}
	
	/**
	 * Makes an uppercased hex string bytes.
	 * @param n Source number.
	 * @return Hex string bytes.
	 */
	public static byte[] hex(Number n) {
		return String.format("%X", n).getBytes();
	}
	
	/**
	 * Get the bytes representation from a byte.
	 * @param b A byte.
	 * @return Array of bytes.
	 */
	public static byte[] bin(Byte b) {
		return new byte[] {b.byteValue()};
	}

	/**
	 * Get the bytes representation from a short.
	 * @param s A short number.
	 * @param bo Byte order (le, be).
	 * @return Array of bytes.
	 */
	public static byte[] bin(Short s, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.allocate(2).order(ord).putShort(s).array();
	}

	/**
	 * Get the bytes representation from a short (default endianess).
	 * @param s A short number.
	 * @return Array of bytes.
	 */
	public static byte[] bin(Short s) {
		return ByteBuffer.allocate(2).putShort(s).array();
	}
	
	/**
	 * Get the bytes from a float.
	 * @param f Float value.
	 * @return Array of bytes.
	 */
	public static byte[] bin(Float f) {
		return ByteBuffer.allocate(4).putFloat(f).array();
	}
	
	/**
	 * Get the bytes from a double number.
	 * @param n A double number.
	 * @return Array of bytes.
	 */
	public static byte[] bin(Double n) {
		return ByteBuffer.allocate(8).putDouble(n).array();
	}

	/**
	 * Get the bytes representation from an int.
	 * @param i An integer number.
	 * @param bo Byte order (le, be).
	 * @return Array of bytes.
	 */
	public static byte[] bin(Integer i, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.allocate(4).order(ord).putInt(i).array();
	}

	/**
	 * Get the bytes representation from an int.
	 * @param i An integer number.
	 * @return Array of bytes.
	 */
	public static byte[] bin(Integer i) {
		return ByteBuffer.allocate(4).putInt(i).array();
	}

	/**
	 * Get the bytes representation from a character.
	 * @param c A character (unsigned 16-bit integer).
	 * @param bo Byte order (le, be)
	 * @return Array of bytes.
	 */
	public static byte[] bin(Character c, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.allocate(2).order(ord).putChar(c).array();
	}

	/**
	 * Get the bytes representation from a character.
	 * @param c A character.
	 * @return Array of bytes.
	 */
	public static byte[] bin(Character c) {
		return ByteBuffer.allocate(2).putChar(c).array();
	}

	/**
	 * Get the bytes representation from a long.
	 * @param l A long number.
	 * @param bo Byte order (le, be).
	 * @return Array of bytes.
	 */
	public static byte[] bin(Long l, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.allocate(8).order(ord).putLong(l).array();
	}

	/**
	 * Get the bytes representation from a long.
	 * @param l A long number.
	 * @return Array of bytes.
	 */
	public static byte[] bin(Long l) {
		return ByteBuffer.allocate(8).putLong(l).array();
	}
	
	/**
	 * Get the bytes from a big integer.
	 * @param i Big integer.
	 * @param bo Byte order.
	 * @return Array of bytes.
	 */
	public static byte[] bin(BigInteger i, String bo) {
		if ("le".equals(bo)) {
			byte[] buf = i.toByteArray();
			ByteBuffer bb = ByteBuffer.allocate(buf.length);
			for (int k = buf.length - 1; k >= 0; k--) bb.put(buf[k]);
			return bb.array();
		} else {
			return ByteBuffer.wrap(i.toByteArray()).array();
		}
	}

	/**
	 * Get the bytes representation from a boolean.
	 * @param b A boolean value.
	 * @return Array of bytes
	 */
	public static byte[] bin(Boolean b) {
		return new byte[] {b ? (byte)1 : (byte)0};
	}

	/**
	 * Get the CRC16 bytes from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return CRC16 checksum.
	 */
	public static byte[] crc16(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.allocate(2).order(ord).putShort(
				IpmemsCrc16.getCrc16(data)).array();
	}

	/**
	 * Get the CRC16 bytes from binary data.
	 * @param data Binary data.
	 * @return CRC16 checksum.
	 */
	public static byte[] crc16(byte[] data) {
		return ByteBuffer.allocate(2).putShort(
				IpmemsCrc16.getCrc16(data)).array();
	}

	/**
	 * Get the CRC32 bytes from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return CRC32 checksum.
	 */
	public static byte[] crc32(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		CRC32 crc = new CRC32();
		crc.update(data);
		return ByteBuffer.allocate(4).order(ord).putInt(
				(int)crc.getValue()).array();
	}

	/**
	 * Get the RCR32 bytes from binary data.
	 * @param data Binary data.
	 * @return CRC32 checksum.
	 */
	public static byte[] crc32(byte[] data) {
		CRC32 crc = new CRC32();
		crc.update(data);
		return ByteBuffer.allocate(4).putInt((int)crc.getValue()).array();
	}
	
	/**
	 * Get the BCD representations of bytes.
	 * @param nums BCD numbers.
	 * @return BCD bytes.
	 */
	public static byte[] bcd(Collection<Object> nums) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (Object o: nums)
			if (o instanceof Number) bos.write(bcd((Number)o).intValue());
			else bos.write(new BigInteger(String.valueOf(o), 16).intValue());
		return bos.toByteArray();
	}
	
	/**
	 * Get the BCD encoded number.
	 * @param n Source number.
	 * @return BCD number.
	 */
	public static BigInteger bcd(Number n) {
		if (n instanceof BigDecimal) 
			return new BigInteger(
					((BigDecimal)n).toBigInteger().toString(), 16);
		else if (n instanceof BigInteger) 
			return new BigInteger(n.toString(), 16);
		else return new BigInteger(
				new BigInteger(
						Long.toHexString(n.longValue()), 16).toString(), 16);
	}
}
