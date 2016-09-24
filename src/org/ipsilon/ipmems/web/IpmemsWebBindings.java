package org.ipsilon.ipmems.web;

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
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import org.ipsilon.ipmems.util.IpmemsFile;

/**
 * IPMEMS web bindings.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsWebBindings implements Closeable {
	/**
	 * IPMEMS web server basic bindings.
	 * @param is Input stream.
	 * @param os Output stream.
	 * @param m HTTP method name.
	 * @param u HTTP URI.
	 * @param queryProps Query properties.
	 * @param uriProps URI properties.
	 * @param fl Current file.
	 */
	public IpmemsWebBindings(
			InputStream is,
			OutputStream os,
			String m,
			URI u,
			Properties queryProps,
			Properties uriProps,
			File fl) throws IOException {
		inputStream = is;
		method = m;
		uri = u;
		queryProperties = queryProps;
		uriProperties = uriProps;
		file = fl;
		File pf = file.getParentFile();
		File f = new File(pf, file.getName() + ".properties");
		if (!f.exists()) {
			f = new File(pf, IpmemsFile.getFileExtension(file) + ".properties");
			if (!f.exists()) f = new File(pf, "default.properties");
		}
		InputStreamReader r = null;
		if (f.exists() && f.isFile() && f.canRead()) try {
			r = new InputStreamReader(new FileInputStream(f), "UTF-8");
			queryProperties.load(r);
		} catch (IOException x) {
			throw x;
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
		outputStream = os;
	}
	
	/**
	 * Adds a resource to the resources list.
	 * @param <T> Closeable type.
	 * @param id Resource id.
	 * @param c A closeable object.
	 * @return Passed closeable object.
	 */
	public <T extends Closeable> T addResource(String id, T c) {
		rks.add(id);
		rvs.add(c);
		return c;
	}
	
	/**
	 * Adds a resource to the resources list.
	 * @param id Resource id.
	 * @param f A file object.
	 * @return Passed file object.
	 */
	public File addResource(String id, File f) {
		rks.add(id);
		rvs.add(f);
		return f;
	}
	
	/**
	 * Adds a gzipped stream from this stream.
	 * @param id Stream id.
	 * @return Gzipped stream.
	 * @throws IOException An I/O exception.
	 */
	public GZIPOutputStream addGzipStream(String id) throws IOException {
		GZIPOutputStream gzos = new GZIPOutputStream(outputStream);
		rks.add(id);
		rvs.add(gzos);
		return gzos;
	}
	
	/**
	 * Adds a writer.
	 * @param id Writer id.
	 * @return Writer.
	 * @throws IOException An I/O exception.
	 */
	public Writer addWriter(String id) throws IOException {
		Writer w = new OutputStreamWriter(outputStream, "UTF-8");
		rks.add(id);
		rvs.add(w);
		return w;
	}
	
	/**
	 * Adds a gzipped writer.
	 * @param id Writer base id.
	 * @return Gzipped writer.
	 * @throws IOException An I/O exception.
	 */
	public Writer addGzipWriter(String id) throws IOException {
		GZIPOutputStream gzos = new GZIPOutputStream(outputStream);
		rks.add(id + "Stream");
		rvs.add(gzos);
		Writer w = new OutputStreamWriter(gzos, "UTF-8");
		rks.add(id + "Writer");
		rvs.add(w);
		return w;
	}
	
	@Override
	public void close() {
		for (int i = rvs.size() - 1; i >= 0; i--) {
			if (rvs.get(i) instanceof Closeable) try {
				if (rvs.get(i) instanceof GZIPOutputStream) try {
					((GZIPOutputStream)rvs.get(i)).finish();
				} catch (Exception xx) {}
				((Closeable)rvs.get(i)).close();
			} catch (Exception x) {} else if (rvs.get(i) instanceof File) try {
				((File)rvs.get(i)).delete();
			} catch (Exception x) {}
		}
		rks.clear();
		rvs.clear();
	}

	/**
	 * Get the current input stream.
	 * @return Current input stream.
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Get the current output stream.
	 * @return Current output stream.
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * Get the HTTP method.
	 * @return HTTP method.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Get the HTTP query path.
	 * @return HTTP query path.
	 */
	public String getPath() {
		return file.getPath();
	}
	
	/**
	 * Get the file extension.
	 * @return File extension.
	 */
	public String getExt() {
		return IpmemsFile.getFileExtension(file);
	}

	/**
	 * Get the current file.
	 * @return Current file.
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Get the current directory.
	 * @return Current directory.
	 */
	public File getDirectory() {
		return file.getParentFile();
	}
	
	/**
	 * Get the file name.
	 * @return File name.
	 */
	public String getFileName() {
		return file.getName();
	}
	
	/**
	 * Get the last modified time.
	 * @return Last modified time.
	 */
	public Date getLastModified() {
		return new Date(file.lastModified());
	}

	/**
	 * Get the query properties.
	 * @return Query properties.
	 */
	public Properties getQueryProperties() {
		return queryProperties;
	}

	/**
	 * Get the URI properties.
	 * @return URI properties.
	 */
	public Properties getUriProperties() {
		return uriProperties;
	}

	/**
	 * Get the current URI.
	 * @return Current URI.
	 */
	public URI getUri() {
		return uri;
	}
		
	/**
	 * Get the named resource.
	 * @param id Resource identifier.
	 * @return Resource object.
	 */
	public Object getResource(String id) {
		int idx = rks.indexOf(id);
		if (idx < 0) throw new NoSuchElementException(id);
		else return rvs.get(idx);
	}
	
	/**
	 * Groovy-style get-method.
	 * @param id Resource identifier.
	 * @return Resource object.
	 */
	public Object getAt(String id) {
		return getResource(id);
	}
	
	/**
	 * Prints the object string representation.
	 * @param o An object.
	 * @throws IOException An I/O exception.
	 */
	public void print(Object o) throws IOException {
		for (char c: String.valueOf(o).toCharArray()) outputStream.write(c);
	}
	
	/**
	 * Prints the object string representation with new line character.
	 * @param o An object.
	 * @throws IOException An I/O exception.
	 */
	public void println(Object o) throws IOException {
		print(o);
		outputStream.write('\n');
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("webBindings[");
		sb.append("method=");
		sb.append(method);
		sb.append(',');
		sb.append("uri=");
		sb.append(uri);
		sb.append(',');
		sb.append("uriProps=");
		sb.append(uriProperties);
		sb.append(',');
		sb.append("queryProps=");
		sb.append(queryProperties);
		sb.append(']');
		return sb.toString();
	}
	
	/**
	 * Get the HTTP date.
	 * @param d Date.
	 * @return HTTP date.
	 */
	public final String httpDate(Object d) {
		return DF.format(d);
	}	
	
	/**
	 * Current file.
	 */
	private final File file;
	
	/**
	 * Input stream.
	 */
	private final InputStream inputStream;
	
	/**
	 * Output stream.
	 */
	private final OutputStream outputStream;
	
	/**
	 * Method.
	 */
	private final String method;
	
	/**
	 * URI.
	 */
	private final URI uri;
	
	/**
	 * Query properties.
	 */
	private final Properties queryProperties;
	
	/**
	 * URI properties.
	 */
	private final Properties uriProperties;
		
	/**
	 * Resource keys.
	 */
	private final ArrayList<String> rks = new ArrayList<String>();
	
	/**
	 * Resources.
	 */
	private final ArrayList<Object> rvs = new ArrayList<Object>();
	
	/**
	 * RFC 2822 date format.
	 */
	public static final SimpleDateFormat DF = 
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	
	static {
		DF.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
}
