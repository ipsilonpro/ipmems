package org.ipsilon.ipmems.ipgroovy;

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

import groovy.lang.*;
import groovy.text.TemplateEngine;
import groovy.text.XmlTemplateEngine;
import groovy.util.XmlParser;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import org.codehaus.groovy.runtime.ComposedClosure;
import org.codehaus.groovy.runtime.MethodClosure;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;

/**
 * IPMEMS groovy interpreter.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsGroovyInterpreter extends 
		GroovyShell implements IpmemsInterpreter {
	/**
	 * Constructs the Groovy interpreter based on specified
	 * class loader and binding map.
	 * @param eng Parent engine.
	 * @param l Class loader.
	 * @param bnd Binding map.
	 */
	@SuppressWarnings("unchecked")
	public IpmemsGroovyInterpreter(
			IpmemsGroovyEngine eng, ClassLoader l, Map bnd) {
		super(l, new Binding(new IpmemsGroovyMap(bnd)));
		engine = eng;
	}

	@Override
	public Closure getFunction(String name) {
		Object o = getVariable(name);
		return o instanceof Closure ? (Closure)o : null;
	}

	@Override
	public Script getScript(String name) {
		Object o = getVariable(name);
		return o instanceof Script ? (Script)o : null;
	}

	@Override
	public Closure makeFunction(String name, String code) throws Exception {
		return (Closure)eval(name, code);
	}

	@Override
	public Closure makeFunction(URL url) throws Exception {
		return (Closure)eval(url);
	}

	@Override
	public Closure makeFunction(File file) throws Exception {
		return (Closure)eval(file);
	}

	@Override
	public Closure makeFunction(Object o, String m, 
			String name, Class<?>... cs) throws Exception {
		return new MethodClosure(o, m);
	}

	@Override
	public Script makeScript(String name, String code) throws Exception {
		return parse(code, name);
	}

	@Override
	public Script makeScript(URL url) throws Exception {
		InputStreamReader r = null;
		try {
			r = new InputStreamReader(url.openStream(), "UTF-8");
			return parse(r, url.getFile());
		} catch (Exception x) {
			throw x;
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public Script makeScript(File file) throws Exception {
		InputStreamReader r = null;
		try {
			r = new InputStreamReader(new FileInputStream(file), "UTF-8");
			return parse(r, file.toString());
		} catch (Exception x) {
			throw x;
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public Object eval(String name, String code) throws Exception {
		return evaluate(code, name);
	}

	@Override
	public Object eval(String name, URL url) throws Exception {
		InputStreamReader r = null;
		try {
			r = new InputStreamReader(url.openStream(), "UTF-8");
			return evaluate(r, name);
		} catch (Exception x) {
			throw x;
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public Object eval(URL url) throws Exception {
		InputStreamReader r = null;
		try {
			r = new InputStreamReader(url.openStream(), "UTF-8");
			return evaluate(r, url.getFile());
		} catch (Exception x) {
			throw x;
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public Object eval(File file) throws Exception {
		InputStreamReader r = null;
		try {
			r = new InputStreamReader(new FileInputStream(file), "UTF-8");
			return run(r, file.toString(), new String[0]);
		} catch (Exception x) {
			throw x;
		} finally {
			if (r != null) try {r.close();} catch (Exception x) {}
		}
	}
		
	/**
	 * Executes a file.
	 * @param name File name.
	 * @return Result.
	 * @throws Exception An exception.
	 */
	public Object executeFile(String name) throws Exception {
		return eval(new File(Ipmems.substituted(name)));
	}

	@Override
	public void addAll(Map bindings, boolean merge) {
		if (merge) {
			for (Object k: bindings.keySet()) if (k != null)
				setVariable(k.toString(), bindings.get(k));
		} else addAll(bindings);
	}

	@Override
	public void addAll(Map bindings) {
		for (Object k: bindings.keySet()) {
			String key = String.valueOf(k);
			if (k != null && !getContext().hasVariable(key))
				setVariable(key, bindings.get(k));
		}
	}

	@Override
	public void webProcess(Socket s, File f, PrintStream ps, Map bnd) 
			throws Exception {
		if (f.getName().endsWith(".groovy")) {
			setVariable("out", ps);
			if (!bnd.isEmpty()) for (Object k: bnd.keySet())
				setVariable(String.valueOf(k), bnd.get(k));
			eval(f);
		} else if (f.getName().endsWith(".gsp")) {
			ps.println("Content-Type: text/html; charset=UTF-8");
			ps.println();
			for (Object k: bnd.keySet())
				setVariable(String.valueOf(k), bnd.get(k));
			TemplateEngine e = new XmlTemplateEngine(new XmlParser(), this);
			Writable w = e.createTemplate(f).make(getContext().getVariables());
			StringWriter wr = new StringWriter();
			w.writeTo(wr);
			ps.println(wr);
			wr.close();
		}
	}

	@Override
	public boolean isPropertized(Object o) {
		return o instanceof Closure || 
				o instanceof Script || o instanceof GroovyShell;
	}

	@Override
	public boolean isDelegatized(Object o) {
		return o instanceof Closure;
	}

	@Override
	public void setProperty(Object o, String name, Object v) {
		if (o instanceof GroovyObject)
			((GroovyObject)o).setProperty(name, v);
	}

	@Override
	public void setProperties(Object o, Map<String,Object> b) {
		if (o instanceof GroovyObject)
			for (Map.Entry<String,Object> e: b.entrySet())
				((GroovyObject)o).setProperty(e.getKey(), e.getValue());
	}

	@Override
	public Object getProperty(Object o, String name) {
		return o instanceof GroovyObject ?
				((GroovyObject)o).getProperty(name) : null;
	}

	@Override
	public void setDelegate(Object o, Object d) {
		if (o instanceof Closure) ((Closure)o).setDelegate(d);
	}

	@Override
	public boolean isFunction(Object o) {
		return o instanceof Closure;
	}

	@Override
	public boolean isScript(Object o) {
		return o instanceof Script;
	}

	@Override
	public boolean isCallable(Object o) {
		return isFunction(o) || isScript(o);
	}

	@Override
	public Object call(Object obj, Object... args) throws Exception {
		if (obj instanceof Closure) return ((Closure)obj).call(args);
		else if (obj instanceof Script) return ((Script)obj).run();
		else throw new IllegalArgumentException(String.valueOf(obj));
	}

	@Override
	public Closure curry(Object obj, Object... args) throws Exception {
		return obj instanceof Closure ?	((Closure)obj).curry(args) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Closure compose(Object f1, Object f2) {
		return f1 instanceof Closure && f2 instanceof Closure ?
				new ComposedClosure((Closure)f1, (Closure)f2) : null;
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	@Override
	public void close() {
		getContext().getVariables().clear();
		setMetaClass(null);
		getContext().setMetaClass(null);
		clearCache();
	}

	@Override
	public void clearCache() {
		resetLoadedClasses();
	}

	@Override
	public IpmemsScriptEngine getEngine() {
		return engine;
	}

	@Override
	public void clear() {
		getContext().getVariables().clear();
	}
	
	private final IpmemsGroovyEngine engine;
}
