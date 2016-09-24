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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.ipsilon.ipmems.io.IpmemsIO;
import org.ipsilon.ipmems.io.IpmemsServerIO;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS protocol server slot.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsProtSlot extends IpmemsPropertized implements Runnable {
	/**
	 * Constructs the protocol server slot.
	 * @param p Slot properties.
	 * @param k Slot key.
	 * @param s Protocol server.
	 * @param o I/O server object.
	 */
	public IpmemsProtSlot(
			Map p, String k, IpmemsProtServer s, IpmemsServerIO o) {
		super(p);
		key = k;
		server = s;
		io = o;
		executor = new ThreadPoolExecutor(
				removeKey(Integer.class, "corePoolSize", 1),
				removeKey(Integer.class, "maximumPoolSize", 65536),
				removeKey(Long.class, "keepAliveTime", 0L),
				removeKey(TimeUnit.class, "timeUnit", TimeUnit.MILLISECONDS),
				new SynchronousQueue<Runnable>(true));
		IpmemsLoggers.info(log(), "{0} +", toString());
	}

	/**
	 * Get the server I/O object.
	 * @return Server I/O object.
	 */
	public IpmemsServerIO getIo() {
		return io;
	}

	/**
	 * Get the processors executor.
	 * @return Executor.
	 */
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	/**
	 * Get the tasks.
	 */
	public Runnable[] getTasks() {
		return executor.getQueue().toArray(new Runnable[0]);
	}

	/**
	 * Get the slot key.
	 * @return Slot key.
	 */
	public String getKey() {
		return key;
	}

	@Override
	public void run() {
		try {
			Map c = get(Map.class, "processor", Collections.EMPTY_MAP);
			int n = io.isMultiClient() ? Integer.MAX_VALUE : 1;
			for (int i = 0; i < n; i++) {
				IpmemsIO cio = io.call();
				executor.submit(new IpmemsProtProcessor(c, this, cio));
			}
		} catch (Exception x) {
			IpmemsLoggers.warning(log(), "{0} Accept error", x, this);
		}
	}
	
	/**
	 * Checks whether running state is on.
	 * @return Running state.
	 */
	public boolean isRunning() {
		return future != null;
	}
	
	/**
	 * Starts the slot.
	 */
	public void start() {
		try {
			io.init();
		} catch (Exception x) {
			IpmemsLoggers.warning(log(), "{0} IO init", x, this);
		}
		future = server.submit(this);
	}
	
	/**
	 * Stops the slot.
	 */
	public void stop() {
		executor.shutdownNow();
		future.cancel(true);
		try {
			io.close();
		} catch (Exception x) {
			IpmemsLoggers.warning(log(), "{0} IO close", x, this);
		}
		future = null;
	}
	
	/**
	 * Get the associated server.
	 * @return Associated server.
	 */
	public IpmemsProtServer getServer() {
		return server;
	}
	
	/**
	 * Get the log name.
	 * @return Log name.
	 */
	public final String log() {
		return server.getLogName();
	}

	@Override
	public final String toString() {
		return key;
	}

	private final IpmemsServerIO io;
	private final IpmemsProtServer server;
	private final String key;
	private final ThreadPoolExecutor executor;
	private volatile Future<?> future;
}
