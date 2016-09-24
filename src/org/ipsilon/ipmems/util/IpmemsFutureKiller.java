package org.ipsilon.ipmems.util;

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

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.ipsilon.ipmems.IpmemsObservable;
import org.ipsilon.ipmems.logging.IpmemsLoggers;

/**
 * IPMEMS one-thread future killer.
 * @param <T> Future type.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsFutureKiller<T> extends IpmemsObservable implements Runnable{
	/**
	 * Constructs the IPMEMS future killer.
	 * @param log Logger.
	 * @param e Executor.
	 * @param threads Scheduler threads count.
	 */
	public IpmemsFutureKiller(String log, ExecutorService e, int threads) {
		threadCount = threads;
		executor = e;
		logName = log;
	}
		
	/**
	 * Submits the task.
	 * @param obj Source obj.
	 * @param task A task.
	 * @param mt Max time.
	 * @return Task future.
	 */
	public Future<Long> submit(T obj, Callable<Long> task, long mt) {
		final Future<Long> f = service.submit(task);
		IpmemsLoggers.fine(logName, "{0} --> {1}", 
				obj, Integer.toHexString(f.hashCode()));
		if (mt > 0) scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				if (!f.isDone()) {
					f.cancel(true);
				}
			}
		}, mt, TimeUnit.MILLISECONDS);
		return f;
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public void run() {
		while (true) {
			Future<Long> f = null;
			try {
				f = service.take();
				fireEvent("done", f, f.get());
			} catch (InterruptedException x) {
				break;
			} catch (ExecutionException x) {
				fireEvent("error", f, x.getCause());
			} catch (CancellationException x) {
				fireEvent("cancelled", f);
			} catch (Exception x) {
				fireEvent("severe", f, x);
			}
		}
	}
		
	/**
	 * Starts the auto killer.
	 */
	public synchronized void start() {
		if (autoKiller == null) {
			service = new ExecutorCompletionService<Long>(executor);
			scheduler = new ScheduledThreadPoolExecutor(threadCount);
			autoKiller = new Thread(this);
			autoKiller.start();
		}
	}
	
	/**
	 * Stops the auto killer.
	 */
	public synchronized void stop() {
		if (autoKiller != null) {
			autoKiller.interrupt();
			scheduler.shutdownNow();
			service = null;
			scheduler = null;
			autoKiller = null;
		}
	}
	
	private final ExecutorService executor;
	private Thread autoKiller;
	private final int threadCount;
	private final String logName;
	private ScheduledThreadPoolExecutor scheduler;
	private ExecutorCompletionService<Long> service;
}
