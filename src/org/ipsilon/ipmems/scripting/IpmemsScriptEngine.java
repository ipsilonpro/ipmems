package org.ipsilon.ipmems.scripting;

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

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * IPMEMS script engine interface.
 * @author Dmitry Ovchinnikov.
 */
public interface IpmemsScriptEngine {
	/**
	 * Get the engine name.
	 * @return Engine name.
	 */
	public String getName();
	
	/**
	 * Get the engine id.
	 * @return Engine id.
	 */
	public String getId();
	
	/**
	 * Get the engine version.
	 * @return Engine version.
	 */
	public String getVersion();
	
	/**
	 * Get the language name.
	 * @return Language name.
	 */
	public String getLanguageName();
	
	/**
	 * Get the language version.
	 * @return Language version.
	 */
	public String getLanguageVersion();
	
	/**
	 * Get the wrapper name.
	 * @return Wrapper name.
	 */
	public String getWrapperName();
	
	/**
	 * Get the wrapper version.
	 * @return Wrapper version.
	 */
	public String getWrapperVersion();
	
	/**
	 * Get the main class loader.
	 * @return Main class loader.
	 */
	public ClassLoader getDefaultClassLoader();
	
	/**
	 * Get the script extensions.
	 * @return Script extension list.
	 */
	public Collection<String> getScriptExtensions();
	
	/**
	 * Get the web extensions.
	 * @return Web extension list.
	 */
	public Collection<String> getWebExtensions();
	
	/**
	 * Get the extended class loader.
	 * @param urls URLs.
	 * @return Extended class loader.
	 */	
	public ClassLoader getClassLoader(URL ... urls);
	
	/**
	 * Loads the class.
	 * @param name Class name.
	 * @param src Compile the sources flag.
	 * @return Loaded class.
	 */
	public Class<?> loadClass(String name, boolean src)
			throws ClassNotFoundException;
	
	/**
	 * Get the interpreter.
	 * @param c Target class loader.
	 * @param bindings Bindings.
	 * @return Created interpreter.
	 */
	public IpmemsInterpreter makeInterpreter(ClassLoader c, Map bindings);
	
	/**
	 * Get the new interpreter.
	 * @param c Target class loader.
	 * @return Created interpreter.
	 */
	public IpmemsInterpreter makeInterpreter(ClassLoader c);
	
	/**
	 * Get the new interpreter.
	 * @param bindings Variable bindings.
	 * @return New interpreter.
	 */
	public IpmemsInterpreter makeInterpreter(Map bindings);
	
	/**
	 * Get the new interpreter.
	 * @return New interpreter.
	 */
	public IpmemsInterpreter makeInterpreter();
	
	/**
	 * Get the main interpreter.
	 * @return Main interpreter.
	 */
	public IpmemsInterpreter getMainInterpreter();
	
	/**
	 * Get the mime binding.
	 * @return MIME binding.
	 */
	public Map<String,String> getMimeBinding();
	
	/**
	 * Get the default MIME type of scripts.
	 * @return Default MIME type of scripts.
	 */
	public String getDefaultMime();
	
	/**
	 * Create the print stream.
	 * @param o Output (byte) stream.
	 * @param af Auto-flush flag.
	 * @return New print stream.
	 */
	public PrintStream printStream(OutputStream o, boolean af) throws Exception;
	
	/**
	 * Get the line offset.
	 * @return Line offset.
	 */
	public int getLineOffset();
	
	/**
	 * Binds the variable.
	 * @param name Variable name.
	 * @param o Variable value.
	 */
	public void bind(String name, Object o);
	
	/**
	 * Unbinds the variable.
	 * @param name Variable name.
	 */
	public void unbind(String name);
	
	/**
	 * Clears the cache of all the loaded classes.
	 */
	public void clearCache();
	
	/**
	 * Initialize the engine.
	 * @throws Exception Any exception.
	 */
	public void init() throws Exception;
}
