package org.ipsilon.ipmems.prot;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import org.ipsilon.ipmems.IpmemsAbstractService;
import org.ipsilon.ipmems.io.IpmemsServerIO;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS protocol server.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsProtServer extends IpmemsAbstractService {
	@Override
	@SuppressWarnings("unchecked")
	public void init(Object... args) {
		super.init(args);
		if (!first) IpmemsScriptEngines.clearCache();
		first = false;
		ConcurrentLinkedQueue<IpmemsProtSlot> ss = slots;
		if (ss != null) ss.clear();
		slots = new ConcurrentLinkedQueue<IpmemsProtSlot>();
		executor = new ThreadPoolExecutor(
				get(Integer.class, "corePoolSize", 1),
				get(Integer.class, "maximumPoolSize", 65536),
				get(Long.class, "keepAliveTime", 0L),
				get(TimeUnit.class, "timeUnit", TimeUnit.MILLISECONDS),
				new SynchronousQueue<Runnable>(true));
		Map<String,Object> conf = Collections.EMPTY_MAP;
		if (!containsKey("conf")) {
			if (containsKey("file")) try {
				conf = (Map)IpmemsScriptEngines.eval(
						new File(substituted("file", null)));
			} catch (Exception x) {
				IpmemsLoggers.warning(getLogName(), "Loading error", x);
			} else if (containsKey("url")) try {
				conf = (Map)IpmemsScriptEngines.eval(
						new URL(substituted("url", null)));
			} catch (Exception x) {
				IpmemsLoggers.warning(getLogName(), "Loading error", x);
			}
		} else conf = (Map)get(Map.class, "conf");
		for (Map.Entry<String,Object> e: conf.entrySet()) try {
			Map ps = (Map)e.getValue();
			Map<String,Object> ioParams = remove(
					Map.class, ps, "ioParams", Collections.EMPTY_MAP);
			IpmemsServerIO io;
			if (ps.containsKey("ioClass")) {
				Object cl = ps.get("ioClass");
				if (cl instanceof String) {
					Class<?> c = IpmemsScriptEngines.loadClass(cl.toString());
					io = (IpmemsServerIO)c.newInstance();
				} else if (cl instanceof Class<?>)
					io = (IpmemsServerIO)((Class<?>)cl).newInstance();
				else continue;
			} else if (ps.containsKey("ioFunc")) {
				io = (IpmemsServerIO)
						IpmemsScriptEngines.call(ps.get("ioFunc"), this);
			} else if (ps.get("io") instanceof IpmemsServerIO) {
				io = (IpmemsServerIO)ps.get("io");
			} else continue;
			io.getMap().putAll(ioParams);
			Map<String,Object> params = remove(
					Map.class, ps, "params", Collections.EMPTY_MAP);
			slots.add(new IpmemsProtSlot(params, e.getKey(), this, io));
		} catch (Exception x) {
			IpmemsLoggers.warning(getLogName(), "{0} +", x, e.getKey());
		}
	}

	@Override
	public String getName() {
		return "protServer";
	}

	@Override
	public String getLogName() {
		return "prot";
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public void start() {
		for (IpmemsProtSlot s: slots) s.start();
	}

	@Override
	public void stop() {
		for (IpmemsProtSlot s: slots) s.stop();
		executor.shutdownNow();
	}

	/**
	 * Get the slots.
	 * @return Slots.
	 */
	public ConcurrentLinkedQueue<IpmemsProtSlot> getSlots() {
		return slots;
	}
	
	/**
	 * Get a slot by key.
	 * @param key A key.
	 * @return Slot by key.
	 */
	public IpmemsProtSlot getSlot(String key) {
		for (IpmemsProtSlot s: slots) if (s.getKey().equals(key)) return s;
		return null;
	}
	
	/**
	 * Get the slot keys.
	 * @return Slot keys.
	 */
	public List<String> getSlotKeys() {
		ArrayList<String> l = new ArrayList<String>();
		for (IpmemsProtSlot s: slots) l.add(s.getKey());
		return l;
	}
	
	/**
	 * Submits the slot.
	 * @param s Slot.
	 * @return Slot future.
	 */
	public Future<?> submit(IpmemsProtSlot s) {
		return executor.submit(s);
	}
	
	private volatile ConcurrentLinkedQueue<IpmemsProtSlot> slots;
	private volatile ThreadPoolExecutor executor;
	private volatile boolean first = true;
}
