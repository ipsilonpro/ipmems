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

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import org.ipsilon.ipmems.IpmemsAbstractService;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS standard scheduler service.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsStdSchedulerService extends IpmemsAbstractService {
	@Override
	public void init(Object... args) {
		super.init(args);
		sch = null;
		if (!first) IpmemsScriptEngines.clearCache();
		first = false;
		Map ps;
		if (containsKey("file")) try {
			ps = (Map)IpmemsScriptEngines.eval(
					new File(substituted("file", "")));
		} catch (Exception x) {
			throw new IllegalStateException(x);
		} else if (containsKey("url")) try {
			ps = (Map)IpmemsScriptEngines.eval(
					new URL(substituted("url", null)));
		} catch (Exception x) {
			throw new IllegalStateException(x);
		} else ps = Collections.EMPTY_MAP;
		try {
			sch = new IpmemsStdScheduler(ps);
			sch.call();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		} finally {
			ps.clear();
		}
	}

	@Override
	public String getName() {
		return "scheduler";
	}

	@Override
	public String getLogName() {
		return "sch";
	}

	@Override
	public boolean isRunning() {
		return started;
	}

	@Override
	public void start() {
		sch.start();
		started = true;
	}

	@Override
	public void stop() {
		sch.stop();
		started = false;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Get the scheduler object.
	 * @return Scheduler object.
	 */
	public IpmemsStdScheduler getSch() {
		return sch;
	}
	
	private volatile IpmemsStdScheduler sch;
	private volatile boolean started;
	private volatile boolean first = true;
}
