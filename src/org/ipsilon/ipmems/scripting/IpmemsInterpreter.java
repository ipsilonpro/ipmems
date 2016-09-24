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

import java.io.Closeable;
import java.io.File;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

/**
 * IPMEMS script interpreter.
 * @author Dmitry Ovchinnikov.
 */
public interface IpmemsInterpreter extends IpmemsLocalBindings, Closeable {
	/**
	 * Get the function by name.
	 * @param name Function name.
	 * @return Function object.
	 */
	public Object getFunction(String name);
	
	/**
	 * Get the script by name.
	 * @param name Script name.
	 * @return Script object.
	 */
	public Object getScript(String name);
	
	/**
	 * Makes the function.
	 * @param name Function name.
	 * @param code Function source code.
	 * @return Function object.
	 */
	public Object makeFunction(String name, String code) throws Exception;
	
	/**
	 * Makes the function from URL.
	 * @param url Function source code URL.
	 * @return Function object.
	 */
	public Object makeFunction(URL url) throws Exception;
	
	/**
	 * Makes the function from file.
	 * @param file Source file.
	 * @return Function object.
	 */
	public Object makeFunction(File file) throws Exception;
	
	/**
	 * Make the method function.
	 * @param o Target object.
	 * @param m Method name.
	 * @param cs Argument types.
	 * @return Method function.
	 * @throws Exception An exception.
	 */
	public Object makeFunction(Object o, String m, String name, 
			Class<?> ... cs) throws Exception;
			
	/**
	 * Makes the script.
	 * @param name Script name.
	 * @param code Source code.
	 * @return Script object.
	 */
	public Object makeScript(String name, String code) throws Exception;
	
	/**
	 * Makes the script from URL.
	 * @param url Script source code URL.
	 * @return Script object.
	 */
	public Object makeScript(URL url) throws Exception;
	
	/**
	 * Makes the script from file.
	 * @param file Source file.
	 * @return Script object.
	 */
	public Object makeScript(File file) throws Exception;
	
	/**
	 * Evaluates the code.
	 * @param name Code name.
	 * @param code Code text.
	 * @return Result object.
	 */
	public Object eval(String name, String code) throws Exception;
	
	/**
	 * Evaluates the code from URL.
	 * @param url An URL.
	 * @return Result object.
	 */
	public Object eval(URL url) throws Exception;
	
	/**
	 * Evaluates the code from URL.
	 * @param name Code name.
	 * @param url An url.
	 * @return Result object.
	 * @throws Exception Evaluation error.
	 */
	public Object eval(String name, URL url) throws Exception;
	
	/**
	 * Evaluates the code from file.
	 * @param file Source file.
	 * @return Result object.
	 */
	public Object eval(File file) throws Exception;
	
	/**
	 * Adds all the bindings from argument.
	 * @param bindings Bindings.
	 * @param merge Merge flag.
	 */
	public void addAll(Map bindings, boolean merge);
	
	/**
	 * Adds all the bindings from argument.
	 * @param bindings Bindings.
	 */
	public void addAll(Map bindings);
	
	/**
	 * Process the web content.
	 * @param s Client socket.
	 * @param f Processed file.
	 * @param ps Socket print stream.
	 * @param bnd Bindings.
	 */
	public void webProcess(Socket s, File f, PrintStream ps, Map bnd)
			throws Exception;
	
	/**
	 * Checks whether the specified object is a function.
	 * @param o Any object.
	 * @return Result status.
	 */
	public boolean isFunction(Object o);
	
	/**
	 * Checks whether the specified object is a script.
	 * @param o Any object.
	 * @return Result status.
	 */
	public boolean isScript(Object o);
	
	/**
	 * Is callable.
	 * @param o Any object.
	 * @return Is callable flag.
	 */
	public boolean isCallable(Object o);
	
	/**
	 * Calls the object.
	 * @param obj Any object.
	 * @param args Object arguments.
	 * @return Call result.
	 */
	public Object call(Object obj, Object ... args) throws Exception;
	
	/**
	 * Curries the function.
	 * @param obj Function object.
	 * @param args Function arguments.
	 * @return Curried function or null.
	 */
	public Object curry(Object obj, Object ... args) throws Exception;
	
	/**
	 * Composes the functions.
	 * @param f1 First function.
	 * @param f2 Second function.
	 * @return Composed function or null.
	 */
	public Object compose(Object f1, Object f2);
	
	/**
	 * Checks whether the specified object is delegatized.
	 * @param o Any object.
	 * @return Delegatized status.
	 */
	public boolean isDelegatized(Object o);
	
	/**
	 * Checks whether the specified object is propertized.
	 * @param o Any object.
	 * @return Propertized status.
	 */
	public boolean isPropertized(Object o);
	
	/**
	 * Sets the object's delegate.
	 * @param o Any object.
	 * @param d Delegate.
	 */
	public void setDelegate(Object o, Object d);
	
	/**
	 * Sets the property of object.
	 * @param o Any object.
	 * @param name Property name.
	 * @param v Property value.
	 */
	public void setProperty(Object o, String name, Object v);
	
	/**
	 * Sets the properties.
	 * @param o Any object.
	 * @param b Property map.
	 */
	public void setProperties(Object o, Map<String,Object> b);
	
	/**
	 * Get the object property.
	 * @param o Any object.
	 * @param name Property name.
	 * @return Property value.
	 */
	public Object getProperty(Object o, String name);
	
	/**
	 * Closes the interpreter.
	 */
	@Override
	public void close();
	
	/**
	 * Get the associated engine.
	 * @return Associated engine.
	 */
	public IpmemsScriptEngine getEngine();
	
	/**
	 * Clears the cache of all the loaded classes.
	 */
	public void clearCache();
	
	/**
	 * Clears all the local binding.
	 */
	public void clear();
}
