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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.ipsilon.ipmems.IpmemsIntl;

/**
 * IPMEMS JSON writer.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsJsonWriter implements Closeable, Flushable {
	/**
	 * Constructs the JSON writer.
	 * @param w Target writer.
	 */
	public IpmemsJsonWriter(Writer w) {
		out = w;
	}
		
	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		try {
			out.close();
		} finally {
			out = null;
		}
	}
	
	private boolean isPunct(char c) {
		switch (Character.getType(c)) {
			case Character.DASH_PUNCTUATION:
			case Character.END_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.OTHER_PUNCTUATION:
			case Character.START_PUNCTUATION: return true;
			default: return false;
		}
	}
	
	private void writeString(String str) throws IOException {
		out.write('"');
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isLetterOrDigit(c) || c == ' ') out.write(c);
			else if (c == '\\') out.write("\\\\");
			else if (c == '"') out.write("\\\"");
			else if (c == '\t') out.write("\\t");
			else if (c == '\r') out.write("\\r");
			else if (c == '\n') out.write("\\n");
			else if (c == '\b') out.write("\\b");
			else if (c == '\f') out.write("\\f");
			else if (isPunct(c)) out.write(c);
			else if (Character.isISOControl(c) || Character.isWhitespace(c) ||
					Character.isHighSurrogate(c) || Character.isLowSurrogate(c))
				out.write(String.format("\\u%04X", (int)c));
			else out.write(c);
		}
		out.write('"');
	}
	
	private void writeCollection(Collection i) throws IOException {
		out.write('[');
		boolean ord = false;
		for (Object o: i) {
			if (ord) out.write(',');
			writeObject(o);
			if (!ord) ord = true;
		}
		out.write(']');
	}
	
	private void writeArray(Object array) throws IOException {
		out.write('[');
		int n = Array.getLength(array);
		for (int i = 0; i < n; i++) {
			if (i > 0) out.write(',');
			writeObject(Array.get(array, i));
		}
		out.write(']');
	}
	
	private void writeMap(Map<Object,Object> m) throws IOException {
		out.write('{');
		boolean ord = false;
		for (Map.Entry<Object,Object> e: m.entrySet()) {
			if (ord) out.write(',');
			writeObject(e.getKey());
			out.write(':');
			writeObject(e.getValue());
			if (!ord) ord = true;
		}
		out.write('}');
	}
	
	@SuppressWarnings("unchecked")
	private void writeObject(Object o) throws IOException {
		if (o == null) out.write("null");
		else if (o instanceof Boolean) out.write(((Boolean)o).toString());
		else if (o instanceof Number) out.write(((Number)o).toString());
		else if (o instanceof String) writeString((String)o);
		else if (o instanceof Date) 
			writeString(IpmemsIntl.formatIso((Date)o));
		else if (o instanceof Throwable)
			writeMap(IpmemsJsonUtil.convert((Throwable)o));
		else if (o instanceof Collection) writeCollection((Collection)o);
		else if (o.getClass().isArray()) writeArray(o);
		else if (o instanceof Map) writeMap((Map)o);
		else writeString(o.toString());
	}
	
	/**
	 * Writes an object.
	 * @param o An object.
	 * @throws IOException Write exception.
	 */
	public void write(Object o) throws IOException {
		writeObject(o);
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		if (out != null) try {
			close();
		} catch (Exception x) {
		} finally {
			super.finalize();
		}
	}
	
	private Writer out;
}
