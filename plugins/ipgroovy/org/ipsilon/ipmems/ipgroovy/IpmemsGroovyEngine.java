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

import groovy.io.GroovyPrintStream;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovySystem;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import static java.util.Collections.EMPTY_MAP;
import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.db.IpmemsDbAddress;
import org.ipsilon.ipmems.dbfi.IpmemsDbfi;
import org.ipsilon.ipmems.io.IpmemsIO;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.scripting.IpmemsInterpreter;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngine;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS groovy engine.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsGroovyEngine implements IpmemsScriptEngine {
	@Override
	public void init() throws Exception {
		boolean mx = Ipmems.get(Boolean.class, "groovyMix", false);
		boolean r = Ipmems.get(Boolean.class, "groovyRecompile", true);
		DEFAULT.setSourceEncoding("UTF-8");
		DEFAULT.setRecompileGroovySource(r);
		ImportCustomizer ic = new ImportCustomizer();
		ic.addStarImports(
				Ipmems.class.getPackage().getName(),
				IpmemsLoggers.class.getPackage().getName(),
				IpmemsDbAddress.class.getPackage().getName(),
				IpmemsDbfi.class.getPackage().getName(),
				IpmemsIO.class.getPackage().getName(),
				IpmemsPropertized.class.getPackage().getName()
		);
		if (!mx) ic.addStaticStars(IpmemsGroovyImports.class.getName());
		DEFAULT.addCompilationCustomizers(ic);
		classLoader = new GroovyClassLoader(getClass().getClassLoader());
		if (mx) DefaultGroovyMethods.mixin(Object.class, IpmemsGroovyMix.class);
		File f = new File(Ipmems.sst("scriptsDirectory", ""), "classes");
		if (f.isDirectory()) classLoader.addURL(f.toURI().toURL());
		mainInterpreter = makeInterpreter();
	}
			
	@Override
	public String getName() {
		return "Groovy";
	}

	@Override
	public String getId() {
		return "groovy";
	}

	@Override
	public String getVersion() {
		return GroovySystem.getVersion();
	}

	@Override
	public String getLanguageName() {
		return "Groovy";
	}

	@Override
	public String getLanguageVersion() {
		return GroovySystem.getVersion();
	}

	@Override
	public String getWrapperName() {
		return getClass().getSimpleName();
	}

	@Override
	public String getWrapperVersion() {
		return "1.0.0";
	}

	@Override
	public Collection<String> getWebExtensions() {
		return webExtensions;
	}

	@Override
	public Collection<String> getScriptExtensions() {
		return mimeBindings.keySet();
	}

	@Override
	public ClassLoader getDefaultClassLoader() {
		return classLoader;
	}

	@Override
	public ClassLoader getClassLoader(URL ... urls) {
		GroovyClassLoader l = new GroovyClassLoader(classLoader);
		for (URL url: urls) l.addURL(url);
		return l;
	}

	@Override
	public Class<?> loadClass(String name, boolean src) 
			throws ClassNotFoundException {
		return classLoader.loadClass(name, src, true, true);
	}

	@Override
	public void bind(String name, Object o) {
		mainInterpreter.setVariable(name, o);
	}

	@Override
	public void unbind(String name) {
		mainInterpreter.getContext().getVariables().remove(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IpmemsGroovyInterpreter makeInterpreter(ClassLoader c, Map b) {
		return new IpmemsGroovyInterpreter(this, c, b);
	}

	@Override
	public IpmemsGroovyInterpreter makeInterpreter(ClassLoader c) {
		return new IpmemsGroovyInterpreter(this, c, EMPTY_MAP);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IpmemsGroovyInterpreter makeInterpreter(Map bindings) {
		return new IpmemsGroovyInterpreter(this, classLoader, bindings);
	}

	@Override
	public IpmemsGroovyInterpreter makeInterpreter() {
		return new IpmemsGroovyInterpreter(this, classLoader, EMPTY_MAP);
	}

	@Override
	public IpmemsInterpreter getMainInterpreter() {
		return mainInterpreter;
	}

	@Override
	public Map<String,String> getMimeBinding() {
		return mimeBindings;
	}

	@Override
	public int getLineOffset() {
		return 0;
	}

	@Override
	public String getDefaultMime() {
		return "text/groovy";
	}

	@Override
	public PrintStream printStream(OutputStream o, boolean a) throws Exception {
		return new GroovyPrintStream(o, a, "UTF-8");
	}

	@Override
	public void clearCache() {
		mainInterpreter.clearCache();
	}

	@Override
	public String toString() {
		return getName() + " " + getVersion();
	}
		
	private final List<String> webExtensions = Arrays.asList("groovy", "gsp");
	private final Map<String,String> mimeBindings = 
			Collections.singletonMap("groovy", "text/groovy");
	private GroovyClassLoader classLoader;
	private IpmemsGroovyInterpreter mainInterpreter;	
}
