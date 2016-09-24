package org.ipsilon.ipmems.io;

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
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Input/output utilities.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsIOLib {	
	/**
	 * Copy file to destination.
	 * @param src Source file.
	 * @param dest Destination.
	 * @throws IOException An exception.
	 */
	public static void copy(File src, File dest) throws IOException {
		FileChannel in = null;
		FileChannel out = null;
		try {
			in = new FileInputStream(src).getChannel();
			out = new FileOutputStream(dest).getChannel();
			in.transferTo(0, src.length(), out);
		} catch (IOException x) {
			throw x;
		} finally {
			if (in != null) try {in.close();} catch (Exception x) {}
			if (out != null) try {out.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Copy file to output stream.
	 * @param s Source file.
	 * @param os Destination stream.
	 * @throws IOException An exception.
	 */
	public static void copy(File s, OutputStream os) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(s);
			byte[] buf = new byte[1024];
			while (true) {
				int l = is.read(buf, 0, 1024);
				if (l < 0) break;
				os.write(buf, 0, l);
			}
		} catch (IOException x) {
			throw x;
		} finally {
			if (is != null) try {is.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Copy file to output stream.
	 * @param s Source file.
	 * @param c Writable channel.
	 * @throws IOException An exception.
	 */
	public static void copy(File s, WritableByteChannel c) throws IOException {
		FileChannel in = null;
		try {
			in = new FileInputStream(s).getChannel();
			in.transferTo(0, s.length(), c);
		} catch (IOException x) {
			throw x;
		} finally {
			if (in != null) try {in.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Copy the bytes from input stream to a file.
	 * @param i Input stream.
	 * @param f Destination file.
	 * @param s Data size.
	 * @throws IOException An I/O exception.
	 */
	public static void copy(InputStream i, File f, long s) throws IOException {
		byte[] b = new byte[1024];
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			while (s > 0) {
				int r = i.read(b, 0, s >= b.length ? b.length : (int)s);
				if (r < 0) break;
				fos.write(b, 0, r);
				s -= r;
			}
		} catch (IOException x) {
			throw x;
		} finally {
			if (fos != null) try {fos.close();} catch (Exception x) {}
		}
	}
		
	/**
	 * Get bytes from a stream.
	 * @param is Input stream.
	 * @param max Maximum bytes count.
	 * @return Bytes array.
	 * @throws IOException I/O exception.
	 */
	public static byte[] getBytes(InputStream is, int max) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		while (max > 0) {
			int r = is.read(buf, 0, max > buf.length ? buf.length : max);
			if (r < 0) break;
			bos.write(buf, 0, r);
			max -= r;
		}
		return bos.toByteArray();
	}
	
	/**
	 * Get bytes from a stream.
	 * @param is Input stream.
	 * @return Bytes array.
	 * @throws IOException I/O exception.
	 */
	public static byte[] getBytes(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		for (int r = is.read(b); r >= 0; r = is.read(b)) bos.write(b, 0,r);
		return bos.toByteArray();
	}
	
	/**
	 * Get complete bytes.
	 * @param i Input stream.
	 * @param l Data length.
	 * @return Byte array.
	 * @throws IOException I/O exception.
	 */
	public static byte[] getAllBytes(InputStream i, int l) throws IOException {
		byte[] d = new byte[l];
		int o = 0;
		for (int n; o < l; o += n) if ((n = i.read(d, o, l - o)) < 0) break;
		return o == l ? d : null;
	}
		
	/**
	 * Get the file text (UTF-8).
	 * @param f File.
	 * @return File text.
	 * @throws IOException An exception.
	 */
	public static String getText(File f) throws IOException {
		FileInputStream i = null;
		try {
			int l = (int)f.length(), o = 0;
			i = new FileInputStream(f);
			byte[] d = new byte[l];
			for (int n; o < l; o += n) if ((n = i.read(d, o, l - o)) < 0) break;
			return new String(d, 0, o, "UTF-8");
		} catch (IOException x) {
			throw x;
		} finally {
			if (i != null) try {i.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Get the file from URL text (UTF-8).
	 * @param url URL.
	 * @return File text.
	 * @throws IOException An exception.
	 */
	public static String getText(URL url) throws IOException {
		InputStream i = null;
		try {
			URLConnection c = url.openConnection();
			i = c.getInputStream();
			int l = c.getContentLength();
			if (l >= 0) {
				byte[] d = new byte[l];
				int o = 0;
				for (int n; o < l; o += n) 
					if ((n = i.read(d, o, l - o)) < 0) break;
				return new String(d, 0, o, "UTF-8");
			} else {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] d = new byte[1024];
				for (int n = i.read(d); n >= 0; n = i.read(d))
					bos.write(d, 0, n);
				return bos.toString("UTF-8");
			}
		} catch (IOException x) {
			throw x;
		} finally {
			if (i != null) try {i.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Set the file text.
	 * @param f File.
	 * @param text Text to be set.
	 * @throws IOException An exception.
	 */
	public static void setText(File f, String text) throws IOException {
		OutputStreamWriter w = null;
		try {
			w = new OutputStreamWriter(
					new FileOutputStream(f), "UTF-8");
			w.write(text);
		} catch (IOException x) {
			throw x;
		} finally {
			if (w != null) try {w.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Get the file lines of text.
	 * @param f File.
	 * @return Lines of text.
	 * @throws IOException An exception.
	 */
	public static List<String> getLines(File f) throws IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), "UTF-8"));
			ArrayList<String> a = new ArrayList<String>();
			for (String l = r.readLine(); l != null; l = r.readLine()) a.add(l);
			return a;
		} catch (IOException x) {
			throw x;
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}
	
	/**
	 * Next message.
	 * @param is Input stream.
	 * @param d Delimiter.
	 * @return Message.
	 * @throws IOException I/O exception.
	 */
	public static byte[] next(InputStream is, byte[] d)	throws IOException {
		byte[] buf = new byte[0];
		ML: for (int r = is.read(); r >= 0; r = is.read()) {
			buf = Arrays.copyOf(buf, buf.length + 1);
			buf[buf.length - 1] = (byte)r;
			if (buf.length >= d.length) {
				for (int i = 0; i < d.length; i++)
					if (buf[buf.length - d.length + i] != d[i]) continue ML;
				return Arrays.copyOf(buf, buf.length - d.length);
			}
		}
		return null;
	}
	
	/**
	 * Next message.
	 * @param is Input stream.
	 * @param d Delimiter.
	 * @return Message.
	 * @throws IOException I/O exception.
	 */
	public static byte[] next(InputStream is, byte d) throws IOException {
		byte[] buf = new byte[0];
		for (int r = is.read(); r >= 0; r = is.read())
			if ((byte)r == d) return buf; else {
				buf = Arrays.copyOf(buf, buf.length + 1);
				buf[buf.length - 1] = (byte)r;
			}
		return null;
	}
	
	/**
	 * Next message.
	 * @param is Input stream.
	 * @param d Delimiter.
	 * @return Message.
	 * @throws IOException I/O exception.
	 */
	public static String next(InputStream is, char d) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int r = is.read(); r >= 0; r = is.read())
			if (r == d) return sb.toString(); else sb.append((char)r);
		return null;
	}
	
	/**
	 * Next message.
	 * @param rd Character reader.
	 * @param d Delimiter.
	 * @return Message.
	 * @throws IOException I/O exception.
	 */
	public static char[] next(Reader rd, char[] d) throws IOException {
		char[] buf = new char[0];
		ML: for (int r = rd.read(); r >= 0; r = rd.read()) {
			buf = Arrays.copyOf(buf, buf.length + 1);
			buf[buf.length - 1] = (char)r;
			if (buf.length >= d.length) {
				for (int i = 0; i < d.length; i++)
					if (buf[buf.length - d.length + i] != d[i]) continue ML;
				return Arrays.copyOf(buf, buf.length - d.length);
			}
		}
		return null;
	}
	
	/**
	 * Next message.
	 * @param rd Character reader.
	 * @param d Delimiter.
	 * @return Message.
	 * @throws IOException I/O exception.
	 */
	public static char[] next(Reader rd, char d) throws IOException {
		char[] buf = new char[0];
		ML: for (int r = rd.read(); r >= 0; r = rd.read()) {
			if (r == d) return buf; else {
				buf = Arrays.copyOf(buf, buf.length + 1);
				buf[buf.length - 1] = (char)r;
			}
		}
		return null;
	}
	
	/**
	 * Half-duplex next message.
	 * @param r Input reader.
	 * @param d Delimiter.
	 * @return Message.
	 * @throws IOException I/O exception.
	 */
	public static String next(Reader r, String d) throws IOException {
		StringBuilder w = new StringBuilder();
		for (int c = r.read(); c >= 0; c = r.read()) {
			w.append((char)c);
			if (w.length() >= d.length()) {
				if (d.equals(w.substring(w.length() - d.length(), w.length())))
					return w.substring(0, w.length() - d.length());
			}
		}
		return null;
	}
	
	/**
	 * Transfers the bytes from one stream to another.
	 * @param is Input stream.
	 * @param os Output stream.
	 * @throws IOException An I/O exception.
	 */
	public static void io(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[65536];
		while (true) {
			int r = is.read(buf);
			if (r < 0) break;
			os.write(buf, 0, r);
		}
	}
	
	/**
	 * Closes the objects.
	 * @param cs Closeables.
	 */
	public static void close(Closeable ... cs) {
		for (Closeable c: cs) try {c.close();} catch (Exception x) {}
	}
}
