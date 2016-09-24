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

import java.util.Map;
import org.codehaus.groovy.runtime.MethodClosure;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.IpmemsLib;
import org.ipsilon.ipmems.IpmemsStrings;
import org.ipsilon.ipmems.json.IpmemsJsonUtil;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.prot.IpmemsProtUtil;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsNet;

/**
 * IPMEMS groovy import.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsGroovyImports {
	/**
	 * IPMEMS library.
	 */
	public static final Class<?> ipmems = IpmemsLib.class;
	
	/**
	 * User map.
	 */
	public static final Map<String,?> userMap = IpmemsScriptEngines.userMap;
	
	/**
	 * Info log closure.
	 */
	public static final MethodClosure info = 
			new MethodClosure(IpmemsLoggers.class, "info");
	
	/**
	 * Warning log closure.
	 */
	public static final MethodClosure warning =
			new MethodClosure(IpmemsLoggers.class, "warning");
	
	/**
	 * Severe log closure.
	 */
	public static final MethodClosure severe =
			new MethodClosure(IpmemsLoggers.class, "severe");
	
	/**
	 * Config log closure.
	 */
	public static final MethodClosure config =
			new MethodClosure(IpmemsLoggers.class, "config");
	
	/**
	 * Fine log closure.
	 */
	public static final MethodClosure fine =
			new MethodClosure(IpmemsLoggers.class, "fine");
	
	/**
	 * Finer log closure.
	 */
	public static final MethodClosure finer =
			new MethodClosure(IpmemsLoggers.class, "finer");
	
	/**
	 * Finest log closure.
	 */
	public static final MethodClosure finest =
			new MethodClosure(IpmemsLoggers.class, "finest");
	
	/**
	 * System property closure.
	 */
	public static final MethodClosure sysProp =
			new MethodClosure(Ipmems.class, "get");
	
	/**
	 * Exception text closure.
	 */
	public static final MethodClosure exceptionText =
			new MethodClosure(IpmemsStrings.class, "exceptionText");
	
	/**
	 * LocStr closure.
	 */
	public static final MethodClosure locStr =
			new MethodClosure(IpmemsIntl.class, "locString");
	
	/**
	 * LocMsg closure.
	 */
	public static final MethodClosure locMsg =
			new MethodClosure(IpmemsIntl.class, "locMessage");
	
	/**
	 * LocString closure.
	 */
	public static final MethodClosure locString =
			new MethodClosure(IpmemsIntl.class, "string");
	
	/**
	 * LocMessage closure.
	 */
	public static final MethodClosure locMessage =
			new MethodClosure(IpmemsIntl.class, "message");
	
	/**
	 * Bind closure.
	 */
	public static final MethodClosure bind =
			new MethodClosure(IpmemsScriptEngines.class, "bind");
	
	/**
	 * Eval closure.
	 */
	public static final MethodClosure eval =
			new MethodClosure(IpmemsScriptEngines.class, "eval");
	
	/**
	 * Json closure.
	 */
	public static final MethodClosure json =
			new MethodClosure(IpmemsJsonUtil.class, "json");
	
	/**
	 * mkUrl closure.
	 */
	public static final MethodClosure mkUrl =
			new MethodClosure(IpmemsNet.class, "mkUrl");
	
	/**
	 * ioTask closure.
	 */
	public static final MethodClosure ioTask =
			new MethodClosure(IpmemsProtUtil.class, "ioTask");
	
	/**
	 * ioVectorTask closure.
	 */
	public static final MethodClosure ioVectorTask =
			new MethodClosure(IpmemsProtUtil.class, "ioVectorTask");
	
	/**
	 * Substituted closure.
	 */
	public static final MethodClosure substituted =
			new MethodClosure(Ipmems.class, "substituted");
}
