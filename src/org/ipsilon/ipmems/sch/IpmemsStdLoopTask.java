package org.ipsilon.ipmems.sch;

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

import java.util.Collections;
import java.util.Map;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS standard loop task.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsStdLoopTask extends IpmemsStdTask {
	/**
	 * Constructs the task.
	 * @param p Parent object.
	 * @param n Task name.
	 * @param props Task properties.
	 */
	public IpmemsStdLoopTask(IpmemsStdObject p, String n, Map props) {
		super(p, n, props);
		Object loopInit = null;
		Object func = null;
		Object condFunc = null;
		Object interruptFunc = null;
		Object breakFun = null;
		Object exitFun = null;
		if (props.get("loop") instanceof Map) {
			Map loop = (Map)props.remove("loop");
			loopInit = loop.remove("init");
			condFunc = loop.remove("cond");
			interruptFunc = loop.remove("interrupt");
			breakFun = loop.remove("brk");
			func = loop.remove("func");
			exitFun = loop.remove("exit");
		}
		loopInitFunc = loopInit;
		loopCondFunc = condFunc;
		brkFunc = breakFun;
		intFunc = interruptFunc;
		loopFunc = func;
		exitFunc = exitFun;
	}

	@Override
	public Long call() throws Exception {
		long start = System.currentTimeMillis();
		if (isCallQueue()) getTaskQueue().call();
		for (IpmemsScriptEngines.call(loopInitFunc, this);
			 Boolean.TRUE.equals(IpmemsScriptEngines.call(loopCondFunc, this));
			 IpmemsScriptEngines.call(loopFunc, this)) {
			if (intFunc != null && Boolean.TRUE.equals(
					IpmemsScriptEngines.call(intFunc, this))) {
				if (brkFunc != null) IpmemsScriptEngines.call(brkFunc, this);
				break;
			}
			if (getFunction() == null) break;
			if (IpmemsScriptEngines.call(
					getFunction(), this, Collections.EMPTY_MAP) == null) break;
		}
		if (exitFunc != null) IpmemsScriptEngines.call(exitFunc, this);
		return System.currentTimeMillis() - start;
	}
	
	private final Object loopInitFunc;
	private final Object loopFunc;
	private final Object loopCondFunc;
	private final Object intFunc;
	private final Object brkFunc;
	private final Object exitFunc;
}
