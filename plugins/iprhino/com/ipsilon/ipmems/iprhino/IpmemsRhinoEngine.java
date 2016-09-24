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

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import static java.util.Collections.EMPTY_MAP;
import java.util.List;
import java.util.Map;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;

/**
 * Rhino scripting engine wrapper.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsRhinoEngine implements IpmemsScriptEngine {
	@Override
	public void init() throws Exception {
		ctxFactory = ContextFactory.getGlobal();
		mainInterpreter = makeInterpreter();
		classLoader = Thread.currentThread().getContextClassLoader();
	}
	
	@Override
	public String getName() {
		return "Rhino";
	}

	@Override
	public String getId() {
		return "rhino";
	}

	@Override
	public String getVersion() {
		return ctxFactory.call(new ContextAction() {
			@Override
			public String run(Context cx) {
				return cx.getImplementationVersion();
			}
		}).toString().replaceFirst("[Rr]hino", "").trim();
	}

	@Override
	public String getLanguageName() {
		return "JavaScript";
	}

	@Override
	public String getLanguageVersion() {
		int o = (Integer)ctxFactory.call(new ContextAction() {
			@Override
			public Integer run(Context cx) {
				return cx.getLanguageVersion();
			}
		});
		int mj = o / 100;
		int mn = (o - mj * 100) / 10;
		int mnn = o - mj * 100 - mn * 10;
		return mj + "." + mn + "." + mnn;
	}

	@Override
	public String getWrapperName() {
		return getClass().getSimpleName();
	}

	@Override
	public String getWrapperVersion() {
		return "1.0";
	}

	@Override
	public ClassLoader getDefaultClassLoader() {
		return classLoader;
	}

	@Override
	public Collection<String> getScriptExtensions() {
		return mimeBinding.keySet();
	}

	@Override
	public Collection<String> getWebExtensions() {
		return wexts;
	}

	@Override
	public ClassLoader getClassLoader(URL ... urls) {
		return new URLClassLoader(urls, classLoader);
	}

	@Override
	public Class<?> loadClass(String name, boolean src) 
			throws ClassNotFoundException {
		return classLoader.loadClass(name);
	}

	@Override
	public void bind(String name, Object o) {
		mainInterpreter.setVariable(name, o);
	}

	@Override
	public void unbind(String name) {
		mainInterpreter.unbind(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IpmemsRhinoInterpreter makeInterpreter(ClassLoader c, Map b) {
		return new IpmemsRhinoInterpreter(this, c, b);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IpmemsRhinoInterpreter makeInterpreter(ClassLoader c) {
		return new IpmemsRhinoInterpreter(this, c, EMPTY_MAP);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IpmemsRhinoInterpreter makeInterpreter(Map bindings) {
		return new IpmemsRhinoInterpreter(this, classLoader, bindings);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IpmemsRhinoInterpreter makeInterpreter() {
		return new IpmemsRhinoInterpreter(this, classLoader, EMPTY_MAP);
	}

	@Override
	public IpmemsRhinoInterpreter getMainInterpreter() {
		return mainInterpreter;
	}

	@Override
	public Map<String,String> getMimeBinding() {
		return mimeBinding;
	}

	@Override
	public String getDefaultMime() {
		return "text/javascript";
	}

	@Override
	public PrintStream printStream(OutputStream o, boolean a) throws Exception {
		return new PrintStream(o, a, "UTF-8");
	}

	@Override
	public int getLineOffset() {
		return 1;
	}

	@Override
	public void clearCache() {
	}
	
	@Override
	public String toString() {
		return getName() + " " + getVersion();
	}
		
	private final Map<String,String> mimeBinding =
			Collections.singletonMap("js", "text/javascript");
	private final List<String> wexts = Collections.singletonList("esp");
	private IpmemsRhinoInterpreter mainInterpreter;
	private ClassLoader classLoader;
	
	/**
	 * Global context factory.
	 */
	public ContextFactory ctxFactory;
}
