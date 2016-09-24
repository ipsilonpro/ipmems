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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON utilities.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsJsonUtil {
	/**
	 * Converts a throwable to a JSON object.
	 * @param t A throwable.
	 * @return JSON object.
	 */
	public static Map<Object,Object> convert(Throwable t) {
		Map<Object,Object> o = new LinkedHashMap<Object,Object>();
		o.put("message", t.getMessage());
		o.put("class", t.getClass().getName());
		o.put("type", "javaException");
		StackTraceElement[] es = t.getStackTrace();
		if (es != null) {
			List<Map<String,Object>> a = new ArrayList<Map<String,Object>>();
			for (StackTraceElement e: es) {
				Map<String,Object> eo = new LinkedHashMap<String,Object>();
				if (e.getClassName() != null) eo.put("class", e.getClassName());
				if (e.getMethodName() != null)
					eo.put("method", e.getMethodName());
				if (e.getFileName() != null) eo.put("file", e.getFileName());
				if (e.getLineNumber() >= 0)	eo.put("line", e.getLineNumber());
				a.add(eo);
			}
			o.put("stack", a);
		}
		if (t.getCause() != null) o.put("cause", convert(t.getCause()));
		return o;
	}
	
	/**
	 * Parses the JSON text.
	 * @param <T> Return type.
	 * @param json JSON text.
	 * @return Java object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parse(String json) throws IOException {
		StringReader sr = new StringReader(json);
		IpmemsJsonReader r = new IpmemsJsonReader(sr);
		try {
			return (T)r.read();
		} finally {
			try {r.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Parses the JSON text from a reader.
	 * @param <T> Return type.
	 * @param r Input reader.
	 * @return Java object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parse(Reader r) throws IOException {
		IpmemsJsonReader rd = new IpmemsJsonReader(r);
		return (T)rd.read();
	}
	
	/**
	 * Parses the JSON text from an URL.
	 * @param <T> Return type.
	 * @param url An URL.
	 * @return Java object.
	 * @throws IOException Read exception.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parse(URL url) throws IOException {
		IpmemsJsonReader r = null;
		try {
			r = new IpmemsJsonReader(
					new InputStreamReader(url.openStream(), "UTF-8"));
			return (T)r.read();
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Parses the JSON text from file.
	 * @param <T> Return type.
	 * @param f A file.
	 * @return Java object.
	 * @throws IOException Parse exception.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parse(File f) throws IOException {
		IpmemsJsonReader r = null;
		try {
			r = new IpmemsJsonReader(new InputStreamReader(
					new FileInputStream(f), "UTF-8"));
			return (T)r.read();
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Makes the JSON string from java object.
	 * @param o Java object.
	 * @return JSON string.
	 */
	public static String make(Object o) throws IOException {
		StringWriter sw = new StringWriter();
		IpmemsJsonWriter wr = new IpmemsJsonWriter(sw);
		try {
			wr.write(o);
			return sw.toString();
		} finally {
			try {wr.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Writes the JSON object to the writer.
	 * @param o Source object.
	 * @param w Writer.
	 * @throws IOException An I/O exception.
	 */
	public static void make(Object o, Writer w) throws IOException {
		IpmemsJsonWriter wr = new IpmemsJsonWriter(w);
		wr.write(o);
	}
	
	/**
	 * Makes the JSON text and writes it to file.
	 * @param o An object.
	 * @param f A file.
	 * @throws IOException Write exception.
	 */
	public static void make(Object o, File f) throws IOException {
		IpmemsJsonWriter w = null;
		try {
			w = new IpmemsJsonWriter(
					new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			w.write(o);
		} finally {
			if (w != null) try {w.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Makes the JSON text and writes it to URL connection.
	 * @param o An object.
	 * @param url An URL.
	 * @throws IOException Write exception.
	 */
	public static void make(Object o, URL url) throws IOException {
		IpmemsJsonWriter w = null;
		URLConnection c;
		try {
			c = url.openConnection();
			c.setDoOutput(true);
			c.connect();
			w = new IpmemsJsonWriter(
					new OutputStreamWriter(c.getOutputStream(), "UTF-8"));
		} finally {
			if (w != null) try {w.close();} catch (Exception x) {}
		}
	}
		
	/**
	 * Converts objects to JSON and vice versa.
	 * @param o An object.
	 * @return JSON string or java object.
	 * @throws IOException An I/O exception.
	 */
	public static Object json(Object o) throws IOException {
		return o instanceof String ? parse((String)o) : make(o);
	}	
}