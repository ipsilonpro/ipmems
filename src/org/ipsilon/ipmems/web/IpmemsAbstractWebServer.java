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
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.IpmemsLib;
import org.ipsilon.ipmems.IpmemsStrings;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.net.IpmemsAbstractTcpServer;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsFile;
import org.ipsilon.ipmems.util.IpmemsNet;
import org.w3c.dom.Document;

/**
 * IPMEMS abstract web server.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsAbstractWebServer extends IpmemsAbstractTcpServer {
	@Override
	public void init(Object... args) {
		super.init(args);
		webDir = new File(substituted("dir", "@{jarDir}/web"));
	}
	
	/**
	 * Prints the HTTP file lines.
	 * @param wb Web bindings.
	 * @param lines HTTP file lines.
	 */
	private void printHttpLines(IpmemsWebBindings wb,
			List<String> lines) throws Exception {
		for (String line: lines) if (!line.isEmpty()) wb.println(line);
	}
		
	@Override
	public void process(Socket s) throws Exception {
		OutputStream os = null;
		InputStream is = null;
		IpmemsWebBindings wb = null;
		try {
			os = s.getOutputStream();
			is = s.getInputStream();			
			String queryLine = IpmemsIOLib.next(is, '\n');
			if (queryLine == null) return;
			String[] queryLineParts = queryLine.trim().split(" ");
			Properties qp = IpmemsNet.queryProps(is);
			URI uri = new URI(queryLineParts[1]);
			wb = new IpmemsWebBindings(is, os, queryLineParts[0].toLowerCase(),
					uri, qp, IpmemsNet.uriProps(uri), getTargetFile(uri, qp));
			if (qp.containsKey("Content-Length")) 
				IpmemsIOLib.copy(is, wb.addResource("putData",
						File.createTempFile("tmp", ".upl", webDir)),
					Long.parseLong(qp.getProperty("Content-Length")));
			process(s, wb);
		} finally {
			if (wb != null) wb.close();
			if (os != null) try {os.close();} catch (Exception x) {}
			if (is != null) try {is.close();} catch (Exception x) {}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected File getTargetFile(URI uri, Properties qp) {
		File dir = webDir;
		if (qp.containsKey("Host") && containsKey("vhosts")) {
			Map<String,String> vhosts = (Map<String,String>)get("vhosts");
			String host = qp.getProperty("Host");
			for (Map.Entry<String,String> e: vhosts.entrySet())
				if (host.contains(e.getKey())) {
					dir = new File(Ipmems.substituted(e.getValue()));
					break;
				}
		}
		File f = new File(dir, uri.getPath());
		if (f.isDirectory()) {
			for (String we: IpmemsScriptEngines.getWebExtensions()) {
				File q = new File(f, "index." + we);
				if (q.isFile()) return q;
			}
			for (String we: get("extensions",
					"html,htmlz,svg,svgz").toString().split(",")) {
				File q = new File(f, "index." + we);
				if (q.isFile()) return q;
			}
		} else if (!f.exists()) for (String e:
				IpmemsScriptEngines.getWebExtensions()) {
			File q = new File(f.getParent(), f.getName() + "." + e);
			if (q.isFile()) return q;
		}
		return f;
	}
	
	protected void process(Socket s, IpmemsWebBindings wb) throws Exception {
		if (!wb.getFile().exists()) {
			printError(wb.getOutputStream(), 404,
					"Not Found", "File {0} doesn't exist", wb.getFile());
			return;
		} else if (wb.getFile().isDirectory()) {
			printError(wb.getOutputStream(), 404,
					"Not Found", "File {0} is a directory", wb.getFile());
			return;
		}
		String ext = wb.getExt();
		IpmemsScriptEngine eng = IpmemsScriptEngines.getEngineByWebExt(ext);
		Object uObject;
		try {
			uObject = processInit(s, wb);
		} catch (Exception xx) {
			processError(wb.getOutputStream(), xx);
			return;
		}
		Date now = new Date();
		List<String> httpLines = getHttpLines(wb.getFile(), ext);
		if (eng != null) {
			if (httpLines == null) {
				wb.println("HTTP/1.1 200 OK");
				wb.println("Server: IPMEMS " + IpmemsLib.getVersion());
				wb.println("Connection: close");
				wb.println("Date: " + wb.httpDate(now));
			} else printHttpLines(wb, httpLines);
			PrintStream p = eng.printStream(wb.getOutputStream(), true);
			wb.addResource("out", p);
			Map<Object,Object> b = new HashMap<Object,Object>();
			b.put("httpMethod",wb.getMethod());
			b.put("queryProps", wb.getQueryProperties());
			b.put("uriProps", wb.getUriProperties());
			b.put("uriPath", wb.getUri().getPath());
			b.put("in", wb.getInputStream());
			b.put("queryData", wb);
			b.put("socket", s);
			b.put("userObject", uObject);
			IpmemsInterpreter i = eng.makeInterpreter(cl(ext, true), b);
			i.webProcess(s, wb.getFile(), p, Collections.EMPTY_MAP);
			i.close();
		} else if (Ipmems.MIMES.containsKey(ext)) {
			boolean gzip = "true".equals(
					wb.getQueryProperties().getProperty("ipmemsGzip"));
			boolean ezip = "true".equals(
					wb.getQueryProperties().getProperty("ipmemsGzipEncoded",
						ext.endsWith("z") ? "true" : "false"));
			String mime = Ipmems.MIMES.getProperty(ext);
			Document doc = processXml(s, wb, uObject);
			if (httpLines == null) {
				wb.println("HTTP/1.1 200 OK");
				wb.println("Server: IPMEMS "+ IpmemsLib.getVersion());
				wb.println("Connection: close");
				if (mime.startsWith("text"))
					wb.println("Content-Type: " + mime + "; charset=UTF-8");
				else wb.println("Content-Type: " + mime);
				if (ezip || gzip) wb.println("Content-Encoding: gzip");
				if (doc == null && !gzip)
					wb.println("Content-Length: " + wb.getFile().length());
				wb.println("Date: " + wb.httpDate(now));
				if (uObject == null && doc == null)
					wb.println("Last-Modified: " + 
							wb.httpDate(wb.getLastModified()));
			} else printHttpLines(wb, httpLines);
			wb.getOutputStream().write('\n');
			wb.getOutputStream().flush();
			if (doc == null) try {
				IpmemsIOLib.copy(wb.getFile(), gzip ?
						wb.addGzipStream("zos") : wb.getOutputStream());
			} catch (Exception xx) {
				IpmemsLoggers.warning(getLogName(),
						"{0} Write {1}", xx, s, wb.getFile());
			} else {
				TransformerFactory fc = TransformerFactory.newInstance();
				Transformer t = fc.newTransformer();
				DOMSource src = new DOMSource(doc);
				t.transform(src, new StreamResult(gzip ?
						wb.addGzipStream("zos") :
						wb.getOutputStream()));
			}
		} else {
			printError(wb.getOutputStream(), 415, "Unsupported media type",
					"Unsupported media type: {0}", ext);
			IpmemsLoggers.warning(getLogName(), 
					"{0} Unknown media type {1}", s, ext);
		}
	}
		
	@Override
	public final int getPort() {
		return get(Integer.class, "port", 8080);
	}

	@Override
	public String getLogName() {
		return "web";
	}
	
	private List<String> getHttpLines(File file, String ext) {
		try {
			File httpFile = new File(
					file.getAbsolutePath().replaceAll(ext + "$", "http"));
			if (httpFile.isFile() && httpFile.canRead())
				return IpmemsIOLib.getLines(httpFile);
			File extFile = new File(webDir, ext + ".lines.http");
			if (extFile.isFile() && extFile.canRead())
				return IpmemsIOLib.getLines(extFile);
			return null;
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(),	"HTTP file error {0}", x, file);
			return null;
		}
	}
	
	/**
	 * Process an error.
	 * @param os Output stream.
	 * @param x An exception.
	 */
	private void processError(OutputStream os, Exception x) throws Exception {
		String msg = IpmemsStrings.exceptionText(x);
		if (x instanceof IllegalAccessException) 
			printError(os, 401, "Unauthorized", msg);
		else if (x instanceof FileNotFoundException) 
			printError(os, 404, "Not Found", msg);
		else printError(os, 500, "Internal Server Error", msg);
	}
		
	public void printError(OutputStream os, int err,
			String desc, String msg, Object ... args) throws Exception {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("HTTP/1.1 " + err + " " + desc);
		pw.println("Server: " + IpmemsLib.getVersion());
		pw.println("Content-Type: text/html; charset=UTF-8");
		pw.println("Connection: close");
		pw.println();
		pw.flush();
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<meta http-equiv=\"Content-Type\" "
				+ "content=\"text/html; charset=UTF-8\"/>");
		pw.print("<title>");
		pw.print(IpmemsIntl.message("HTTP {0} error page", err));
		pw.println("</title>");
		pw.println("<body>");
		pw.println(IpmemsIntl.message(msg, args));
		pw.println("<hr/>");
		pw.println("<small>IPMEMS " + IpmemsLib.getFullVersion() + "</small>");
		pw.println("</body>");
		pw.print("</html>");
		pw.flush();
		os.write(sw.toString().getBytes("UTF-8"));
		pw.close();
		os.flush();
	}
	
	private Object processInit(Socket s, IpmemsWebBindings b) throws Exception {
		File c = null;
		for (String e: IpmemsScriptEngines.getScriptExtensions()) {
			File t = new File(
					b.getDirectory(), b.getFileName() + ".init." + e);
			if (t.isFile() && t.canRead()) {
				c = t;
				break;
			}
			t = new File(b.getDirectory(), b.getExt() + ".init." + e);
			if (t.isFile() && t.canRead()) {
				c = t;
				break;
			}
			t = new File(b.getDirectory(), "default.init." + e);
			if (t.isFile() && t.canRead()) {
				c = t;
				break;
			}
		}
		return c != null ? evalFile(s, b, false, c) : null;
	}
	
	private Document processXml(Socket s, IpmemsWebBindings wb, Object o) {
		String ext = wb.getExt();
		try {
			File c = null;
			for (String e: IpmemsScriptEngines.getScriptExtensions()) {
				File t = new File(
						wb.getDirectory(), wb.getFileName() + ".xml." + e);
				if (t.isFile() && t.canRead()) {
					c = t;
					break;
				}
				t = new File(wb.getDirectory(), ext + ".xml." + e);
				if (t.isFile() && t.canRead()) {
					c = t;
					break;
				}
			}
			if (c == null) return null;
			Document doc = null;
			InputStream is = null;
			try {
				is = ext.endsWith("z") ?
						new GZIPInputStream(new FileInputStream(wb.getFile())) :
						new FileInputStream(wb.getFile());
				DocumentBuilderFactory z = DocumentBuilderFactory.newInstance();
				z.setNamespaceAware(!ext.startsWith("html"));
				z.setIgnoringComments(true);
				DocumentBuilder db = z.newDocumentBuilder();
				doc = db.parse(is);
			} catch (Exception xx) {
				IpmemsLoggers.warning(getLogName(), "{0} XML {1}", xx, s, c);
			} finally {
				if (is != null) try {is.close();} catch (Exception xx) {}
			}
			evalFile(s, wb, false, c,
					"document", doc,
					"userObject", o,
					"elementMap", new IpmemsWebDocParser(doc));
			return doc;
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(),
					"{0} XML trigger error {1}", x, s, wb.getPath());
			return null;
		}
	}
	
	protected Object evalFile(Socket s, IpmemsWebBindings wb, boolean web,
			File c, Object ... bnd) throws Exception {
		String ex = IpmemsFile.getFileExtension(c);
		Map<Object,Object> b = new HashMap<Object,Object>();
		IpmemsScriptEngine e = IpmemsScriptEngines.getEngineByScriptExt(ex);
		PrintStream ps = e.printStream(wb.getOutputStream(), true);
		b.put("out", wb.addResource("out", ps));
		b.put("httpMethod", wb.getMethod());
		b.put("queryProps", wb.getQueryProperties());
		b.put("uriProps", wb.getUriProperties());
		b.put("uriPath", wb.getUri().getPath());
		b.put("queryData", wb);
		b.put("in", wb.getInputStream());
		b.put("socket", s);
		for (int i = 0; i < bnd.length; i += 2) b.put(bnd[i], bnd[i + 1]);
		IpmemsInterpreter i = e.makeInterpreter(cl(ex, web), b);
		try {
			return i.eval(c);
		} finally {
			i.close();
		}
	}
	
	private ClassLoader cl(String ext, boolean web) {
		IpmemsScriptEngine e = web ?
				IpmemsScriptEngines.getEngineByWebExt(ext) :
				IpmemsScriptEngines.getEngineByScriptExt(ext);
		synchronized(cls) {
			if (cls.containsKey(e)) return cls.get(e); else try {
				ArrayList<URL> u = new ArrayList<URL>();
				File f = new File(substituted("classes", "@{jarDir}/webcl"));
				if (f.isDirectory()) u.add(f.toURI().toURL());
				f = new File(substituted("plugins", "@{jarDir}/webpl"));
				if (f.isDirectory())
					for (File pf: f.listFiles()) u.add(pf.toURI().toURL());
				ClassLoader cl = u.isEmpty() ? e.getDefaultClassLoader() :
						e.getClassLoader(u.toArray(new URL[u.size()]));
				cls.put(e, cl);
				return cl;
			} catch (Exception x) {
				IpmemsLoggers.warning(getLogName(), "CL error: {0}", x, ext);
				return e.getDefaultClassLoader();
			}
		}
	}
	
	/**
	 * Web directory.
	 */
	protected volatile File webDir;
	
	private final Map<IpmemsScriptEngine,ClassLoader> cls =
			new IdentityHashMap<IpmemsScriptEngine,ClassLoader>();
}
