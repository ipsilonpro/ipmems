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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import java.util.Arrays;
import java.util.Formatter;
import org.ipsilon.ipmems.IpmemsStrings;

/**
 * Protocol parse library.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsProtParseLib {
	/**
	 * Get the byte from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A byte.
	 */
	public static byte xbyte(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).get(0);
	}

	/**
	 * Get the byte from binary data.
	 * @param data Binary data.
	 * @return Byte order (le, be).
	 */
	public static byte xbyte(byte[] data) {
		return ByteBuffer.wrap(data).get(0);
	}

	/**
	 * Get the short from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A short number.
	 */
	public static short xshort(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getShort(0);
	}

	/**
	 * Get the short from binary data.
	 * @param data Binary data.
	 * @return A short number.
	 */
	public static short xshort(byte[] data) {
		return ByteBuffer.wrap(data).getShort(0);
	}

	/**
	 * Get the integer from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return An integer.
	 */
	public static int xint(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getInt(0);
	}

	/**
	 * Get the integer from binary data.
	 * @param data Binary data.
	 * @return An integer.
	 */
	public static int xint(byte[] data) {
		return ByteBuffer.wrap(data).getInt(0);
	}

	/**
	 * Get the long from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A long number.
	 */
	public static long xlong(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getLong(0);
	}

	/**
	 * Get the long from binary data.
	 * @param data Binary data.
	 * @return A long number.
	 */
	public static long xlong(byte[] data) {
		return ByteBuffer.wrap(data).getLong(0);
	}

	/**
	 * Get the float from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A float number.
	 */
	public static float xfloat(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getFloat(0);
	}

	/**
	 * Get the float from binary data.
	 * @param data Binary data.
	 * @return A float number.
	 */
	public static float xfloat(byte[] data) {
		return ByteBuffer.wrap(data).getFloat(0);
	}

	/**
	 * Get the double from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A double number.
	 */
	public static double xdouble(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getDouble(0);
	}

	/**
	 * Get the double from binary data.
	 * @param data Binary data.
	 * @return A double number.
	 */
	public static double xdouble(byte[] data) {
		return ByteBuffer.wrap(data).getDouble(0);
	}

	/**
	 * Get the boolean from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A boolean value.
	 */
	public static boolean xbool(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).get(0) != 0;
	}

	/**
	 * Get the boolean from binary data.
	 * @param data Binary data.
	 * @return A boolean value.
	 */
	public static boolean xbool(byte[] data) {
		return ByteBuffer.wrap(data).get(0) != 0;
	}

	/**
	 * Get the character from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A character.
	 */
	public static char xchar(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getChar(0);
	}

	/**
	 * Get the character from binary data.
	 * @param data Binary data.
	 * @return A character.
	 */
	public static char xchar(byte[] data) {
		return ByteBuffer.wrap(data).getChar(0);
	}

	/**
	 * Checks the CRC16 checksum.
	 * @param data Source data.
	 * @param crc Source checksum.
	 * @param bo Byte order.
	 * @return Check state.
	 */
	public static boolean ccrc16(byte[] data, byte[] crc, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		ByteBuffer bb = ByteBuffer.allocate(2).order(ord).putShort(
				IpmemsCrc16.getCrc16(data));
		return Arrays.equals(bb.array(), crc);
	}

	/**
	 * Checks the CRC16 checksum.
	 * @param data Source data.
	 * @param bo Byte order.
	 * @return Check state.
	 */
	public static boolean ccrc16(byte[] data, String bo) {
		return ccrc16(Arrays.copyOfRange(data, 0, data.length - 2),
				Arrays.copyOfRange(data, data.length - 2, data.length), bo);
	}

	/**
	 * Checks the CRC16 checksum.
	 * @param data Source data.
	 * @return Check state.
	 */
	public static boolean ccrc16(byte[] data) {
		return ccrc16(Arrays.copyOfRange(data, 0, data.length - 2),
				Arrays.copyOfRange(data, data.length - 2, data.length), "be");
	}

	/**
	 * Checks the CRC16 checksum.
	 * @param data Source data.
	 * @param crc Check state.
	 * @param bo Byte order.
	 * @return Check state.
	 */
	public static boolean crcrc16(byte[] data, byte[] crc, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		ByteBuffer bb = ByteBuffer.allocate(2).order(ord).putShort(
				IpmemsCrc16.getCrc16(
					Arrays.copyOfRange(data, 0, data.length - crc.length)));
		return Arrays.equals(bb.array(), crc);
	}

	/**
	 * Checks the CRC16 checksum.
	 * @param data Source data.
	 * @param crc Check state.
	 * @return Check state.
	 */
	public static boolean crcrc16(byte[] data, byte[] crc) {
		return crcrc16(data, crc, "be");
	}
	
	/**
	 * Converts the numeric string to a number.
	 * @param s Numeric string.
	 * @param base Numeric base.
	 * @return A byte.
	 */
	public static byte n2b(String s, int base) {
		return Short.valueOf(s, base).byteValue();
	}
	
	/**
	 * Converts the numeric string to a number.
	 * @param s Numeric string.
	 * @param base Numeric base.
	 * @return A short.
	 */
	public static short n2s(String s, int base) {
		return Integer.valueOf(s, base).shortValue();
	}
	
	/**
	 * Converts the numeric string to a number.
	 * @param s Numeric string.
	 * @param base Numeric base.
	 * @return A character.
	 */
	public static int n2c(String s, int base) {
		return (char)Integer.parseInt(s, base);
	}
	
	/**
	 * Converts the numeric string to a number.
	 * @param s Numeric string.
	 * @param base Numeric base.
	 * @return A character.
	 */
	public static int n2i(String s, int base) {
		return Long.valueOf(s, base).intValue();
	}
	
	/**
	 * Converts the numeric string to a number.
	 * @param s Numeric string.
	 * @param base Numeric base.
	 * @return A character.
	 */
	public static long n2l(String s, int base) {
		return new BigInteger(s, base).longValue();
	}
	
	/**
	 * Get the big integer number from argument.
	 * @param bytes Binary data.
	 * @param masks Byte masks.
	 * @return Big integer.
	 */
	public static BigInteger num(byte[] bytes, String ... masks) {
		byte[] dest = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			dest[i] = i < masks.length ? 
					(byte)(bytes[i] & Integer.parseInt(masks[i], 2)) : bytes[i];
		return new BigInteger(1, dest);
	}
		
	/**
	 * Get the big integer number from argument.
	 * @param bytes Binary data.
	 * @param sign Number sign.
	 * @param masks Byte masks.
	 * @return Big integer.
	 */
	public static BigInteger num(byte[] bytes, int sign, String ... masks) {
		byte[] dest = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			dest[i] = i < masks.length ? 
					(byte)(bytes[i] & Integer.parseInt(masks[i], 2)) : bytes[i];
		return new BigInteger(sign, dest);
	}
	
	/**
	 * Get the big integer number from argument.
	 * @param sbit Sign bit index (0-th is lowest bit).
	 * @param bytes Binary data.
	 * @param masks Byte masks.
	 * @return Big integer.
	 */
	public static BigInteger num(int sbit, byte[] bytes, String ... masks) {
		byte[] d = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			d[i] = i < masks.length ? 
					(byte)(bytes[i] & Integer.parseInt(masks[i], 2)) : bytes[i];
		return new BigInteger(xtest(d, sbit) ? -1 : 1, d);
	}
	
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
	 * @param w Number width.
	 * @param pw Part width.
	 * @param sep Part separator.
	 * @return Formatted string.
	 */
	public static String binfmt(Number n, int w, int pw, String sep) {
		final String str;
		if (n instanceof Byte || n instanceof Short || n instanceof Integer) 
			str = Integer.toBinaryString(n.intValue());
		else if (n instanceof Long)	str = Long.toBinaryString(n.longValue());
		else if (n instanceof BigInteger) str = ((BigInteger)n).toString(2);
		else if (n instanceof BigDecimal) 
			str = ((BigDecimal)n).toBigInteger().toString(2);
		else if (n instanceof Float)
			str = Integer.toBinaryString(Float.floatToIntBits((Float)n));
		else if (n instanceof Double) 
			str = Long.toBinaryString(Double.doubleToLongBits((Double)n));
		else str = Long.toBinaryString(n.longValue());
		if (pw <= 0 || pw > w) 
			return str.length() < w ? 
					IpmemsStrings.repeat('0', w - str.length()) + str :
					str;
		else {
			StringBuilder sb = new StringBuilder();
			int count = 0;
			for (int i = str.length() - 1; i >= 0; i--) {
				sb.insert(0, str.charAt(i));
				count++;
				if (count == pw) {
					count = 0;
					sb.insert(0, sep);
				}
			}
			return sb.toString();
		}
	}
	
	/**
	 * Formats the number as binary string with given width.
	 * @param n A number.
	 * @param w Number width.
	 * @param pw Part width.
	 * @return Formatted string.
	 */
	public static String binfmt(Number n, int w, int pw) {
		return binfmt(n, w, pw, " ");
	}
	
	/**
	 * Formats the number as binary string with given width.
	 * @param n A number.
	 * @param w Number width.
	 * @return Formatted string.
	 */
	public static String binfmt(Number n, int w) {
		return binfmt(n, w, 0, null);
	}
	
	/**
	 * Get the bytes by indices.
	 * @param source Source data.
	 * @param indices Byte indices.
	 * @return Extracted bytes.
	 */
	public static byte[] xbytes(byte[] source, int ... indices) {
		byte[] result = new byte[indices.length];
		for (int i = 0; i < indices.length; i++) result[i] = source[indices[i]];
		return result;
	}
	
	/**
	 * Get the bytes according to a byte order.
	 * @param src Source bytes.
	 * @param bo Byte order (le, be).
	 * @return Byte array.
	 */
	public static byte[] xbytes(byte[] src, String bo) {
		if ("le".equals(bo)) {
			byte[] buf = new byte[src.length];
			for (int i = 0; i < buf.length; i++) 
				buf[i] = src[src.length - i - 1];
			return buf;
		} else return Arrays.copyOf(src, src.length);
	}
			
	/**
	 * Test for n-th bit.
	 * @param src Binary data.
	 * @param index Bit index.
	 * @return Test result.
	 */
	public static boolean xtest(byte[] src, int index) {
		int idx = src.length - 1 - index / 8;
		return (idx >= 0 && idx < src.length) ? 
				(src[idx] & (1 << index % 8)) != 0 : false;
	}
	
	/**
	 * Get the hexadecimal representation of the binary data.
	 * @param src Source data.
	 * @param sep Separator.
	 * @return Hexadecimal number as string.
	 */
	public static String hexmsg(byte[] src, String sep) {
		if (src == null || src.length == 0) return "";
		else {
			StringBuilder sb = new StringBuilder(
					src.length * 2 + sep.length() * (src.length - 1));
			Formatter fm = new Formatter(sb);
			fm.format("%02X", src[0]);
			for (int i = 1; i < src.length; i++) fm.format(" %02X", src[i]);
			fm.close();
			return sb.toString();
		}
	}
	
	/**
	 * Get the masked byte.
	 * @param v A byte.
	 * @param mask Byte mask (as binary string).
	 * @return Masked byte.
	 */
	public static byte mask(byte v, String mask) {
		return (byte)(v & (Integer.parseInt(mask, 2) & 0xFF));
	}
		
	/**
	 * Get the BCD encoded number.
	 * @param n Source number.
	 * @return BCD number.
	 */
	public static Number xbcd(Number n) {
		if (n instanceof BigInteger) return 
				new BigInteger(((BigInteger)n).toString(16));
		else if (n instanceof BigDecimal) 
			return xbcd(((BigDecimal)n).toBigInteger());
		else return Long.valueOf(Long.toHexString(n.longValue()));
	}
}
