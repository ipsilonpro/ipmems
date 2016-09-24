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

package com.ipsilon.ipmems.iprhino;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.mozilla.javascript.*;

/**
 * IPMEMS Rhino interpreter.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsRhinoInterpreter implements IpmemsInterpreter {	
	/**
	 * Constructs the rhino interpreter.
	 * @param eng Parent engine,
	 * @param l Class loader.
	 * @param bnd Script bindings.
	 */
	public IpmemsRhinoInterpreter(IpmemsRhinoEngine eng,
			ClassLoader l, final Map<Object,Object> bnd) {
		engine = eng;
		classLoader = l;
		so = (ScriptableObject)engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				ScriptableObject s = new IpmemsRhinoImporter(cx);
				for (Map.Entry<Object,Object> e: bnd.entrySet())
					s.put(String.valueOf(e.getKey()), s, 
							Context.javaToJS(e.getValue(), s));
				if (!s.has("out", s)) 
					s.put("out", s, Context.javaToJS(System.out, s));
				return s;
			}
		});
		try {
			eval("init", IpmemsRhinoInterpreter.class.getResource("init.js"));
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "Unable to compile init.js", x);
		}
	}

	@Override
	public Function getFunction(String name) {
		Object o = getVariable(name);
		return o instanceof Function ? (Function)o : null;
	}

	@Override
	public Script getScript(String name) {
		Object o = getVariable(name);
		return o instanceof Script ? (Script)o : null;
	}

	@Override
	public Function makeFunction(final String name, final String code) 
			throws Exception {
		return (Function)engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				return cx.compileFunction(so, code, name, 0, null);
			}
		});
	}

	@Override
	public Function makeFunction(final URL url) throws Exception {
		return (Function)engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				try {
					return cx.compileFunction(so, IpmemsIOLib.getText(url),
							url.getFile(), 0, null);
				} catch (Exception x) {
					throw new IllegalStateException(x);
				}
			}
		});
	}

	@Override
	public Function makeFunction(final File file) throws Exception {
		return (Function)engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				try {
					return cx.compileFunction(so, IpmemsIOLib.getText(file),
							file.toString(), 0, null);
				} catch (Exception x) {
					throw new IllegalStateException(x);
				}
			}
		});
	}

	@Override
	public Function makeFunction(Object o, String m, String name, 
			Class<?> ... cs) throws Exception {
		Method method = o instanceof Class ? ((Class<?>)o).getMethod(m, cs) :
				o.getClass().getMethod(m, cs);
		return new NativeJavaMethod(method, name);
	}

	@Override
	public Script makeScript(final String name, final String code) 
			throws Exception {
		return (Script)engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				return cx.compileString(code, name, 0, null);
			}
		});
	}

	@Override
	public Script makeScript(final URL url) throws Exception {
		final Reader r = new InputStreamReader(url.openStream(), "UTF-8");
		try {
			return (Script)engine.ctxFactory.call(new ContextAction() {
				@Override
				public Object run(Context cx) {
					try {
						return cx.compileReader(r, url.getFile(), 0, null);
					} catch (Exception x) {
						throw new IllegalStateException(x);
					}
				}
			});
		} finally {
			try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public Script makeScript(final File f) throws Exception {
		final Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
		try {
			return (Script)engine.ctxFactory.call(new ContextAction() {
				@Override
				public Object run(Context cx) {
					try {
						return cx.compileReader(r, f.toString(), 0, null);
					} catch (Exception x) {
						throw new IllegalStateException(x);
					}
				}
			});
		} finally {
			try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public Object eval(final String name, final String code) throws Exception {
		return engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				return Context.jsToJava(cx.evaluateString(
						so, code, name, 0, null), Object.class);
			}
		});
	}
	
	@Override
	public Object eval(final URL url) throws Exception {
		final Reader r = new InputStreamReader(url.openStream(), "UTF-8");
		try {
			return engine.ctxFactory.call(new ContextAction() {
				@Override
				public Object run(Context cx) {
					try {
						return Context.jsToJava(cx.evaluateReader(
								so, r, url.getFile(), 0, null), Object.class);
					} catch (Exception x) {
						throw new IllegalStateException(x);
					}
				}
			});
		} finally {
			try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public Object eval(final String name, final URL url) throws Exception {
		final Reader r = new InputStreamReader(url.openStream(), "UTF-8");
		try {
			return engine.ctxFactory.call(new ContextAction() {
				@Override
				public Object run(Context cx) {
					try {
						return Context.jsToJava(cx.evaluateReader(
								so, r, name, 0, null), Object.class);
					} catch (Exception x) {
						throw new IllegalStateException(x);
					}
				}
			});
		} finally {
			try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public Object eval(final File f) throws Exception {
		final Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
		try {
			return engine.ctxFactory.call(new ContextAction() {
				@Override
				public Object run(Context cx) {
					try {
						return Context.jsToJava(cx.evaluateReader(
								so, r, f.toString(), 0, null), Object.class);
					} catch (Exception x) {
						throw new IllegalStateException(x);
					}
				}
			});
		} finally {
			try {r.close();} catch (Exception x) {}
		}
	}

	@Override
	public void addAll(final Map bindings, boolean merge) {
		if (merge) engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				for (Object k: bindings.keySet())
					so.put(String.valueOf(k), so, 
							Context.javaToJS(bindings.get(k), so));
				return null;
			}
		}); else addAll(bindings);
	}

	@Override
	public void addAll(final Map bindings) {
		engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				for (Object k: bindings.keySet()) {
					String name = String.valueOf(k);
					if (!so.has(name, so)) 
						so.put(name, so, Context.javaToJS(bindings.get(k), so));
				}
				return null;
			}
		});
	}

	@Override
	public void webProcess(Socket s, File f, PrintStream ps, Map bnd) 
			throws Exception {
		setVariable("out", ps);
		for (Object k: bnd.keySet())
			setVariable(String.valueOf(k), bnd.get(k));
		eval(f);
	}

	@Override
	public void setVariable(final String name, final Object obj) {
		engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				so.put(name, so, Context.javaToJS(obj, so));
				return null;
			}
		});
	}

	@Override
	public Object getVariable(final String name) {
		return engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				return Context.jsToJava(so.get(name, so), Object.class);
			}
		});
	}

	@Override
	public boolean isPropertized(Object o) {
		return o instanceof ScriptableObject;
	}

	@Override
	public boolean isDelegatized(Object o) {
		return false;
	}

	@Override
	public void setProperty(final Object o, final String name, final Object v) {
		if (o instanceof ScriptableObject) engine.ctxFactory.call(
				new ContextAction() {
			@Override
			public Object run(Context cx) {
				ScriptableObject s = (ScriptableObject)o;
				s.put(name, s, Context.javaToJS(v, so));
				return null;
			}
		});
	}

	@Override
	public void setProperties(final Object o, final Map<String,Object> b) {
		if (o instanceof ScriptableObject) engine.ctxFactory.call(
				new ContextAction() {
			@Override
			public Object run(Context cx) {
				for (Map.Entry<String,Object> e: b.entrySet()) {
					ScriptableObject s = (ScriptableObject)o;
					s.put(e.getKey(), s, Context.javaToJS(e.getValue(), so));
				}
				return null;
			}
		});
	}

	@Override
	public Object getProperty(final Object o, final String name) {
		if (o instanceof ScriptableObject) 
			return engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				ScriptableObject s = (ScriptableObject)o;
				return Context.jsToJava(s.get(name, s), Object.class);
			}
		}); else return null;
	}

	@Override
	public Object call(final Object o, final Object... args) throws Exception {
		if (o instanceof Function) return engine.ctxFactory.call(
				new ContextAction() {
			@Override
			public Object run(Context cx) {
				Function f = (Function)o;
				return Context.jsToJava(f.call(cx, so, f, args), Object.class);
			}
		});
		else if (o instanceof Script) 
			return engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				Script s = (Script)o;
				return Context.jsToJava(s.exec(cx, so), Object.class);
			}
		}); else return null;
	}

	@Override
	public Object curry(final Object obj, final Object... v) throws Exception {
		return engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				Function f = (Function)so.get("curry", so);
				Object[] jargs = new Object[v.length + 1];
				for (int i = 0; i < v.length; i++)
					jargs[i + 1] = Context.javaToJS(v[i], so);
				jargs[0] = obj;
				return f.call(cx, so, so, jargs);
			}
		});
	}

	@Override
	public Object compose(Object u, Object v) {
		final Object[] args = {u, v};
		return engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				Function f = (Function)so.get("composed", so);
				return Context.call(cx.getFactory(), f, so, so, args);
			}
		});
	}

	@Override
	public boolean isCallable(Object o) {
		return isFunction(o) || isScript(o);
	}

	@Override
	public boolean isFunction(Object o) {
		return o instanceof Function;
	}

	@Override
	public boolean isScript(Object o) {
		return o instanceof Script;
	}

	@Override
	public void setDelegate(Object o, Object d) {
	}

	@Override
	public void close() {
	}

	@Override
	public void clear() {
		engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				int n = so.size();
				for (int i = 0; i < n; i++) so.delete(0);
				return null;
			}
		});
	}
		
	public void unbind(final String name) {
		engine.ctxFactory.call(new ContextAction() {
			@Override
			public Object run(Context cx) {
				so.delete(name);
				return null;
			}
		});
	}

	@Override
	public IpmemsScriptEngine getEngine() {
		return engine;
	}

	@Override
	public void clearCache() {
	}
		
	private final ClassLoader classLoader;
	private final ScriptableObject so;
	private final IpmemsRhinoEngine engine;
}
