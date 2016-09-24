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
import java.util.*;
import java.util.zip.CRC32;
import org.ipsilon.ipmems.IpmemsStrings;

/**
 * IPMEMS protocol message parsing/making binding object.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsProtBinding {
	/**
	 * Constructs the binding object.
	 * @param o Current object.
	 * @param c Message configuration.
	 */
	public IpmemsProtBinding(Object o, Map<String,Object> c) {
		obj = o;
		conf = c;
		message = new byte[0];
	}
	
	/**
	 * Constructs the binding object.
	 * @param o Current object.
	 * @param c Message configuration.
	 * @param m Initial message.
	 * @param of Initial offset.
	 */
	public IpmemsProtBinding(Object o, Map<String,Object> c, byte[] m, int of) {
		this(o, c);
		message = m;
		offset = of;
	}
	
	/**
	 * Get the current library.
	 * @return Current library.
	 */
	@Deprecated
	public IpmemsProtBinding getLib() {
		return this;
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
	 * @param u Uppercase flag.
	 * @return Hex string bytes.
	 */
	public static byte[] hex(Number n, int w, boolean u) {
		return String.format("%0" + w + (u ? "X" : "x"), n).getBytes();
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
		} else return ByteBuffer.wrap(i.toByteArray()).array();
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
	 * Get the CRC16 checksum.
	 * @param bo Byte order.
	 * @return Checksum.
	 */
	public byte[] crc16(String bo) {
		return crc16(message, bo);
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
	 * Get the CRC16 checksum.
	 * @return CRC16 checksum.
	 */
	public byte[] crc16() {
		return crc16(message);
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
	 * Get the CRC32 checksum.
	 * @param bo Byte order.
	 * @return Checksum.
	 */
	public byte[] crc32(String bo) {
		return crc32(message, bo);
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
	 * Get the CRC32 checksum.
	 * @return CRC32 checksum.
	 */
	public byte[] crc32() {
		return crc32(message);
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
	
	/**
	 * Get a byte from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A byte.
	 */
	public static byte xbyte(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).get(0);
	}
	
	/**
	 * Get the single byte.
	 * @param bo Byte order.
	 * @return Single byte.
	 */
	public byte xbyte(String bo) {
		return xbyte(arg, bo);
	}

	/**
	 * Get a byte from binary data.
	 * @param data Binary data.
	 * @return Byte order (le, be).
	 */
	public static byte xbyte(byte[] data) {
		return ByteBuffer.wrap(data).get(0);
	}
	
	/**
	 * Extracts a single byte.
	 * @return Single byte.
	 */
	public byte xbyte() {
		return xbyte(arg);
	}

	/**
	 * Get a short from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A short number.
	 */
	public static short xshort(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getShort(0);
	}
	
	/**
	 * Extracts a short number.
	 * @param bo Byte order.
	 * @return Short number.
	 */
	public short xshort(String bo) {
		return xshort(arg, bo);
	}

	/**
	 * Get a short from binary data.
	 * @param data Binary data.
	 * @return A short number.
	 */
	public static short xshort(byte[] data) {
		return ByteBuffer.wrap(data).getShort(0);
	}
	
	/**
	 * Extracts a short number.
	 * @return Short number.
	 */
	public short xshort() {
		return xshort(arg);
	}

	/**
	 * Get an integer from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return An integer.
	 */
	public static int xint(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getInt(0);
	}
	
	/**
	 * Extracts an integer.
	 * @param bo Byte order.
	 * @return Integer.
	 */
	public int xint(String bo) {
		return xint(arg, bo);
	}

	/**
	 * Get an integer from binary data.
	 * @param data Binary data.
	 * @return An integer.
	 */
	public static int xint(byte[] data) {
		return ByteBuffer.wrap(data).getInt(0);
	}
	
	/**
	 * Extracts an integer.
	 * @return Integer.
	 */
	public int xint() {
		return xint(arg);
	}

	/**
	 * Get a long from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A long number.
	 */
	public static long xlong(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getLong(0);
	}
	
	/**
	 * Extracts a long number.
	 * @param bo Byte order.
	 * @return Long number.
	 */
	public long xlong(String bo) {
		return xlong(arg, bo);
	}

	/**
	 * Get a long from binary data.
	 * @param data Binary data.
	 * @return A long number.
	 */
	public static long xlong(byte[] data) {
		return ByteBuffer.wrap(data).getLong(0);
	}
	
	/**
	 * Extracts a long number.
	 * @return Long number.
	 */
	public long xlong() {
		return xlong(arg);
	}

	/**
	 * Get a float from binary data.
	 * @param data Binary data.
	 * @param bo Byte order (le, be).
	 * @return A float number.
	 */
	public static float xfloat(byte[] data, String bo) {
		ByteOrder ord = "le".equals(bo) ? LITTLE_ENDIAN : BIG_ENDIAN;
		return ByteBuffer.wrap(data).order(ord).getFloat(0);
	}
	
	/**
	 * Extracts a float number.
	 * @param bo Byte order.
	 * @return A float number.
	 */
	public float xfloat(String bo) {
		return xfloat(arg, bo);
	}

	/**
	 * Get a float from binary data.
	 * @param data Binary data.
	 * @return A float number.
	 */
	public static float xfloat(byte[] data) {
		return ByteBuffer.wrap(data).getFloat(0);
	}
	
	/**
	 * Extracts a float number.
	 * @return Float number.
	 */
	public float xfloat() {
		return xfloat(arg);
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
	 * Extracts a double number.
	 * @param bo Byte order.
	 * @return Double number.
	 */
	public double xdouble(String bo) {
		return xdouble(arg, bo);
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
	 * Extracts a double number.
	 * @return Double number.
	 */
	public double xdouble() {
		return xdouble(arg);
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
	 * Extracts a boolean.
	 * @param bo Byte order.
	 * @return Boolean.
	 */
	public boolean xbool(String bo) {
		return xbool(arg, bo);
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
	 * Extracts a boolean.
	 * @return Boolean.
	 */
	public boolean xbool() {
		return xbool(arg);
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
	 * Extracts a character.
	 * @param bo Byte order.
	 * @return A character.
	 */
	public char xchar(String bo) {
		return xchar(arg, bo);
	}

	/**
	 * Get a character from binary data.
	 * @param data Binary data.
	 * @return A character.
	 */
	public static char xchar(byte[] data) {
		return ByteBuffer.wrap(data).getChar(0);
	}
	
	/**
	 * Extracts a character.
	 * @return A character.
	 */
	public char xchar() {
		return xchar(arg);
	}

	/**
	 * Checks the CRC16 checksum.
	 * @param data Source data.
	 * @param crc Source checksum.
	 * @param bo Byte order.
	 * @return Check state.
	 */
	public static boolean ccrc16(byte[] data, byte[] crc, String bo) {
		return Arrays.equals(crc16(data, bo), crc);
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
	 * @param bo Byte order.
	 * @return Check state.
	 */
	public boolean ccrc16(String bo) {
		return ccrc16(message, bo);
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
	 * @return CRC16 checksum.
	 */
	public boolean ccrc16() {
		return ccrc16(message);
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
	 * Extracts a big integer.
	 * @param masks Masks.
	 * @return Big integer.
	 */
	public BigInteger num(String ... masks) {
		return num(arg, masks);
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
	 * Extracts a big integer.
	 * @param sign Sign.
	 * @param masks Masks.
	 * @return Big integer.
	 */
	public BigInteger num(int sign, String ... masks) {
		return num(arg, sign, masks);
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
		return new BigInteger(xtest(bytes, sbit) ? -1 : 1, d);
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
	 * Extracts bytes.
	 * @param indices Byte indices.
	 * @return Bytes array.
	 */
	public byte[] xbytes(int ... indices) {
		return xbytes(arg, indices);
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
	 * Extracts bytes.
	 * @param bo Byte order.
	 * @return Bytes array.
	 */
	public byte[] xbytes(String bo) {
		return xbytes(arg, bo);
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
	 * Tests for n-th bit.
	 * @param index Bit index.
	 * @return Test result.
	 */
	public boolean xtest(int index) {
		return xtest(arg, index);
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
	 * Get the hexadecimal representation of data.
	 * @param sep Separator.
	 * @return Hexadecimal string.
	 */
	public String hexmsg(String sep) {
		return hexmsg(arg, sep);
	}
	
	/**
	 * Get the hexadecimal representation of data.
	 * @return Hexadecimal representation.
	 */
	public String hexmsg() {
		return hexmsg(" ");
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
		
	/**
	 * Current object.
	 */
	public final Object obj;
		
	/**
	 * Current transaction data.
	 */
	public final Map<String,Object> data = new LinkedHashMap<String,Object>();
	
	/**
	 * Message configuration.
	 */
	public final Map<String,Object> conf;
	
	/**
	 * Current value.
	 */
	public Object v;
	
	/**
	 * Current argument.
	 */
	public byte[] arg;
	
	/**
	 * Current offset.
	 */
	public int offset;
	
	/**
	 * Current message.
	 */
	public byte[] message;
	
	/**
	 * Current key.
	 */
	public String key;	
}
