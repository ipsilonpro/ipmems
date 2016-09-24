package org.ipsilon.ipmems.json;

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

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * IPMEMS JSON reader.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsJsonReader implements Closeable {
	/**
	 * Constructs the JSON reader.
	 * @param r Source reader.
	 */
	public IpmemsJsonReader(Reader r) {
		in = new PushbackReader(r, 1);
	}
	
	/**
	 * Reads an object.
	 * @return An object.
	 * @throws IOException Read exception.
	 */
	public Object read() throws IOException {
		return read(false);
	}

	@Override
	public void close() throws IOException {
		if (in != null) try {
			in.close();
		} finally {
			in = null;
		}
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		try {
			close();
		} catch (Exception x) {
		} finally {
			super.finalize();
		}
	}
	
	private static boolean isStartNumSymbol(char c) {
		return Character.isDigit(c) || c == '+' || c == '-';
	}
	
	private static boolean isNumSymbol(char c) {
		return isStartNumSymbol(c) || c == '.' || c == 'e' || c == 'E';
	}
	
	private char readSpace() throws IOException {
		while (true) {
			int r = in.read();
			if (r < 0) throw new EOFException();
			if (Character.isWhitespace((char)r)) continue;
			else return (char)r;
		}
	}
	
	private boolean readComment() throws IOException {
		int s1 = in.read();
		if (s1 < 0) throw new EOFException();
		int s2 = in.read();
		if (s2 < 0) throw new EOFException();
		boolean lc = true;
		switch (s2) {
			case '*':
				lc = true;
				break;
			case '/':
				lc = false;
				break;
			default:
				throw new IllegalArgumentException();
		}
		if (lc) {
			while (true) {
				int r = in.read();
				if (r < 0) throw new EOFException();
				else if (r == '*') {
					int n = in.read();
					if (n < 0) throw new EOFException();
					else if (n == '/') return false;
					else in.unread(n);
				}
			}
		} else {
			while (true) {
				int r = in.read();
				if (r < 0) return true;
				else if (r == '\n') return false;
			}
		}
	}
	
	private Number readNum(boolean eof) throws IOException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			int r = in.read();
			if (r < 0) {
				if (eof) throw new EOFException();
				else break;
			}
			char c = (char)r;
			if (isNumSymbol(c)) sb.append(c); else {
				in.unread(r);
				break;
			}
		}
		try {
			return new BigInteger(sb.toString(), 10);
		} catch (Exception x) {
			return new BigDecimal(sb.toString());
		}
	}
	
	private String readStr() throws IOException {
		int start = in.read();
		if (start < 0) throw new EOFException();
		if (start != '"') throw new IllegalArgumentException();
		StringBuilder sb = new StringBuilder();
		while (true) {
			int r = in.read();
			if (r < 0) throw new EOFException();
			char c = (char)r;
			if (c == '"') break;
			else if (c == '\\') sb.append(readSpecial());
			else sb.append(c);
		}
		return sb.toString();
	}
	
	private char readSpecial() throws IOException {
		int rf = in.read();
		if (rf < 0) throw new EOFException();
		char c = (char)rf;
		switch (c) {
			case 'n': return '\n';
			case '"':
			case '/':
			case '\\': return c;
			case 'r': return '\r';
			case 't': return '\t';
			case 'b': return '\b';
			case 'f': return '\f';
			case 'u': {
				char[] buf = new char[4];
				for (int i = 0; i < buf.length; i++) {
					int q = in.read();
					if (q < 0) throw new EOFException();
					buf[i] = (char)q;
				}
				return (char)Integer.parseInt(String.valueOf(buf), 16);
			}
			default: 
				throw new IllegalArgumentException(Character.toString(c));
		}			
	}		
	
	private boolean readBool() throws IOException {
		int start = in.read();
		if (start < 0) throw new EOFException();
		if (start == 't') {
			char[] b = new char[3];
			for (int i = 0; i < b.length; i++) {
				int r = in.read();
				if (r < 0) throw new EOFException();
				b[i] = (char)r;
			}
			if (b[0] == 'r' && b[1] == 'u' && b[2] == 'e') return true;
			else throw new IllegalArgumentException("t" + String.valueOf(b));
		} else if (start == 'f') {
			char[] b = new char[4];
			for (int i = 0; i < b.length; i++) {
				int r = in.read();
				if (r < 0) throw new EOFException();
				b[i] = (char)r;
			}
			if (b[0] == 'a' && b[1] == 'l' && b[2] == 's' && b[3] == 'e')
				return false;
			else throw new IllegalArgumentException("f" + String.valueOf(b));
		} else throw new IllegalStateException();
	}
	
	private Object readNull() throws IOException {
		int start = in.read();
		if (start < 0) throw new EOFException();
		if (start == 'n') {
			char[] b = new char[3];
			for (int i = 0; i < b.length; i++) {
				int r = in.read();
				if (r < 0) throw new EOFException();
				b[i] = (char)r;
			}
			if (b[0] == 'u' && b[1] == 'l' && b[2] == 'l') return null;
			else throw new IllegalArgumentException("n" + String.valueOf(b));
		} else throw new IllegalStateException();
	}
	
	private List<Object> readList() throws IOException {
		int start = in.read();
		if (start < 0) throw new EOFException();
		if (start != '[') throw new IllegalArgumentException();
		ArrayList<Object> l = new ArrayList<Object>();
		boolean wait = true;
		while (true) {
			char c = readSpace();
			if (c == ']') break;
			in.unread(c);
			if (c == '/') if (readComment()) throw new EOFException();
			if (wait) l.add(read(true)); else {
				char comma = readSpace();
				if (comma != ',') throw new IllegalArgumentException();
			}
			wait = !wait;
		}
		return l;
	}
	
	private Map<Object,Object> readMap() throws IOException {
		int start = in.read();
		if (start < 0) throw new EOFException();
		if (start != '{') throw new IllegalArgumentException();
		Map<Object,Object> m = new LinkedHashMap<Object,Object>();
		boolean wait = true;
		while (true) {
			char c = readSpace();
			if (c == '}') break;
			in.unread(c);
			if (c == '/') if (readComment()) throw new EOFException();
			if (wait) {
				Object[] a = readMapEntry();
				m.put(a[0], a[1]);
			} else {
				char comma = readSpace();
				if (comma != ',') throw new IllegalArgumentException();
			}
			wait = !wait;
		}
		return m;
	}
	
	private Object[] readMapEntry() throws IOException {
		Object k = read(true);
		char c = readSpace();
		if (c == '/') {
			in.unread(c);
			if (readComment()) throw new EOFException();
			c = readSpace();
		}
		if (c != ':') throw new IllegalArgumentException();
		Object v = read(true);
		return new Object[] {k, v};
	}
	
	private Object read(boolean eof) throws IOException {
		char c = readSpace();
		in.unread(c);
		if (c == 't' || c == 'f') return readBool();
		else if (c == 'n') return readNull();
		else if (isStartNumSymbol(c)) return readNum(eof);
		else if (c == '"') return readStr();
		else if (c == '[') return readList();
		else if (c == '{') return readMap();
		else if (c == '/') {
			if (readComment()) throw new EOFException();
			else return read(eof);
		} else throw new IllegalArgumentException(Character.toString(c));
	}
	
	private PushbackReader in;
}
