package org.ipsilon.ipmems.net;

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

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;
import org.ipsilon.ipmems.IpmemsAbstractService;
import org.ipsilon.ipmems.IpmemsObserver;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.util.IpmemsDynInvoke;
import org.ipsilon.ipmems.util.IpmemsFutureKiller;

/**
 * Abstract TCP server.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsAbstractTcpServer extends 
		IpmemsAbstractService implements Runnable, IpmemsObserver {
	@Override
	public void init(Object... args) {
		super.init(args);
		long kat = get(Long.class, "keepAliveTime", 100L);
		int init = get(Integer.class, "initThreadCount", 1);
		int max = get(Integer.class, "maxThreadCount", 128);
		service = new ThreadPoolExecutor(init, max, kat, TimeUnit.MILLISECONDS,
				new SynchronousQueue<Runnable>());
		killer = new IpmemsFutureKiller<Socket>(
				getLogName(), service, get(Integer.class, "killerThreads", 1));
		maxTime = get(Long.class, "maxTime", 3600000L);
	}
	
	@Override
	public void start() {
		if (isRunning()) return;
		serverThread = new Thread(this, toString());
		serverThread.start();
		killer.addObserver(this);
		killer.start();
		IpmemsLoggers.info(getLogName(), "{0} Start", this);
	}

	@Override
	public void stop() {
		if (serverThread == null) return;
		serverThread.interrupt();
		killer.stop();
		killer.removeObserver(this);
		serverThread = null;
	}

	@Override
	public boolean isRunning() {
		return serverThread != null;
	}

	@Override
	public void event(Object src, Object... a) {
		String o = Integer.toHexString(a[1].hashCode());
		if ("done".equals(a[0]))
			IpmemsLoggers.fine(getLogName(), "{0} {1} ms", o, a[2]);
		else if ("error".equals(a[0])) 
			IpmemsLoggers.warning(getLogName(), "{0}", (Throwable)a[2], o);
		else if ("cancelled".equals(a[0]))
			IpmemsLoggers.warning(getLogName(), "{0} ~", o);
		else if ("severe".equals(a[0]))
			IpmemsLoggers.severe(getLogName(), "{0}", (Throwable)a[2], o);
		fireEvent("closed", o);
	}
	
	@Override
	public String toString() {
		return getName() + ":" + getPort();
	}

	/**
	 * Get the executor service.
	 * @return Executor service.
	 */
	public ThreadPoolExecutor getService() {
		return service;
	}
		
	/**
	 * Get the server port.
	 * @return Server port.
	 */
	public abstract int getPort();
	
	/**
	 * Processing function.
	 * @param s Socket.
	 * @throws Exception An exception.
	 */
	public abstract void process(Socket s) throws Exception;
		
	/**
	 * Process the client.
	 * @param s Client socket.
	 */
	public void processClient(final Socket s) {
		if (containsKey("socketProps"))
			IpmemsDynInvoke.fill(getLogName(), s, (Map)get("socketProps"));
		fireEvent("accept", s);
		killer.submit(s, new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				long start = System.currentTimeMillis();
				process(s);
				return System.currentTimeMillis() - start;
			}
		}, maxTime);
	}

	/**
	 * Get the future killer.
	 * @return Future killer.
	 */
	public IpmemsFutureKiller getKiller() {
		return killer;
	}

	/**
	 * Get the server thread.
	 * @return Server thread.
	 */
	public Thread getServerThread() {
		return serverThread;
	}
	
	private volatile long maxTime;
	private volatile IpmemsFutureKiller<Socket> killer;
	private volatile Thread serverThread;
	private volatile ThreadPoolExecutor service;	
}
