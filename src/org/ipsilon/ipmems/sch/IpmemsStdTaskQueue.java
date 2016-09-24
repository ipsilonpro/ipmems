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

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import org.ipsilon.ipmems.IpmemsObserver;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.prot.IpmemsProtUtil;
import org.ipsilon.ipmems.prot.IpmemsProtUtil.Debug;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsFutureKiller;

/**
 * IPMEMS task queue.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsStdTaskQueue extends 
		IpmemsStdObject implements IpmemsTaskQueue, IpmemsObserver {
	/**
	 * Constructs the task queue.
	 * @param p Parent scheduler.
	 * @param n Queue name.
	 * @param props Queue properties.
	 */
	@SuppressWarnings("unchecked")
	public IpmemsStdTaskQueue(IpmemsStdScheduler p, String n, Map props) {
		super(p, n, props);
		IpmemsLoggers.info("sch", "{0} +", toString());
		long keepAliveTime = remove(Long.class, props, "keepAlive", 0L);
		int threads = remove(Integer.class, props, "threads", 1);
		int maxThreads = remove(Integer.class, props, "maxThreads", threads);
		timeUnit = remove(TimeUnit.class, props,
				"timeUnit", TimeUnit.MILLISECONDS);
		int mxSize = remove(Integer.class, props, "maxSize", Integer.MAX_VALUE);
		if (props.containsKey("groups")) try {
			Map<String,Map> gs = (Map)props.remove("groups");
			for (Map.Entry<String,Map> e: gs.entrySet()) try {
				add(new IpmemsStdTaskGroup(this, e.getKey(), e.getValue()));
			} catch (Exception x) {
				IpmemsLoggers.warning("err",
						"{0} + {1}", x, getName(), e.getKey());
			}
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "{0} G", x, getName());
		}
		if (props.containsKey("tasks")) try {
			Map<String,Map> ts = (Map)props.remove("tasks");
			for (Map.Entry<String,Map> e: ts.entrySet()) try {
				if (e.getValue().containsKey("loop"))
					add(new IpmemsStdLoopTask(this, e.getKey(), e.getValue()));
				else add(new IpmemsStdTask(this, e.getKey(), e.getValue()));
			} catch (Exception x) {
				IpmemsLoggers.warning("err",
						"{0} + {1}", x, getName(), e.getKey());
			}
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "{0} T", x, getName());
		}
		executor = new ThreadPoolExecutor(threads, maxThreads, keepAliveTime,
				timeUnit, new LinkedBlockingQueue<Runnable>(mxSize));
		timer = new ScheduledThreadPoolExecutor(1);
		killer = new IpmemsFutureKiller<IpmemsTask>("sch", executor,
				remove(Integer.class, props, "killerThreads", 1));
	}
		
	/**
	 * Get initial number of threads.
	 * @return Initial number of threads.
	 */
	@Override
	public int getThreads() {
		return executor.getCorePoolSize();
	}

	/**
	 * Get maximum number of threads.
	 * @return Maximum number of threads.
	 */
	@Override
	public int getMaxThreads() {
		return executor.getMaximumPoolSize();
	}
		
	/**
	 * Get the associated task groups.
	 * @return Associated task groups.
	 */
	@Override
	public ArrayList<IpmemsStdTaskGroup> getGroups() {
		return groups;
	}

	/**
	 * Get the associated tasks.
	 * @return Associated tasks array.
	 */
	@Override
	public ArrayList<IpmemsStdTask> getTasks() {
		return tasks;
	}
	
	/**
	 * Get the keep alive time.
	 * @return Keep alive time.
	 */
	public long getKeepAliveTime() {
		return executor.getKeepAliveTime(timeUnit);
	}
	
	/**
	 * Get the default time unit.
	 * @return Default time unit.
	 */
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
		
	/**
	 * Get the current queue size.
	 * @return Current queue size.
	 */
	@Override
	public int getSize() {
		return executor.getQueue().size();
	}

	@Override
	public int getMaxSize() {
		BlockingQueue q = executor.getQueue();
		return q.size() + q.remainingCapacity();
	}

	@Override
	public boolean isCase(Object obj) {
		return obj instanceof IpmemsStdTaskGroup ?
				groups.contains((IpmemsStdTaskGroup)obj) :
				obj instanceof IpmemsStdTask ?
				tasks.contains((IpmemsStdTask)obj) : super.isCase(obj);
	}
	
	@Override
	public void event(Object src, Object... args) {
		String o = Integer.toHexString(args[1].hashCode());
		if ("done".equals(args[0]))
			IpmemsLoggers.fine("sch", "{0} {1} ms", o, args[2]);
		else if ("cancelled".equals(args[0]))
			IpmemsLoggers.fine("sch", "{0} ~", o);
		else if ("error".equals(args[0])) {
			try {
				Field f = args[1].getClass().getDeclaredField("sync");
				if (!f.isAccessible()) f.setAccessible(true);
				Object sync = f.get(args[1]);
				f = sync.getClass().getDeclaredField("callable");
				if (!f.isAccessible()) f.setAccessible(true);
				IpmemsTask t = (IpmemsTask)f.get(sync);
				Object tryFunc = t.get("try");
				if (tryFunc != null) try {
					IpmemsScriptEngines.call(tryFunc, t, args[2]);
				} catch (Exception x) {
					IpmemsLoggers.warning("err", "{0}", x, t);
				}
			} catch (Exception x) {
				IpmemsLoggers.warning("err", "{0}", (Throwable)args[2], o);
			}
		} else if ("severe".equals(args[0]))
			IpmemsLoggers.severe("sch", "{0}", (Throwable)args[2], o);
	}
	
	/**
	 * Submit the task.
	 * @param t Task.
	 */
	public void submit(IpmemsStdTask t) {
		long p = t.isKillable() ? TimeUnit.MILLISECONDS.convert(
				t.getTimeout(), t.getTimeUnit()) : 0;
		killer.submit(t, t, p);
	}
	
	/**
	 * Runs the closure.
	 * @param c Function object.
	 * @param args Closure arguments.
	 * @return Future result.
	 */
	public Future<?> run(final Object c, final Object ... args) {
		return executor.submit(IpmemsScriptEngines.isFunction(c) ?
				new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				Object[] a = args == null ? new Object[0] : args;
				return IpmemsScriptEngines.call(c, a);
			}
		} : new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return null;
			}
		});
	}
	
	@Override
	public void start() {
		killer.addObserver(this);
		killer.start();
		for (IpmemsStdTask t: tasks) t.start();
		for (IpmemsStdTaskGroup g: groups) g.start();
	}

	@Override
	public void stop() {
		killer.stop();
		killer.removeObserver(this);
		for (IpmemsStdTask t: tasks) t.stop();
		for (IpmemsStdTaskGroup g: groups) g.stop();
		timer.shutdownNow();
		executor.shutdownNow();
		super.stop();
	}
	
	@Override
	public IpmemsStdObject getObject(String path) {
		IpmemsStdObject obj = super.getObject(path);
		if (obj != null) return obj; else {
			for (IpmemsStdTask e: tasks) {
				String n = e.id();
				if (path.startsWith(n)) {
					if (path.length() == n.length()) return e; else {
						String w = path.substring(n.length());
						if (w.startsWith("/")) {
							IpmemsStdObject o = e.getObject(w.substring(1));
							if (o != null) return o;
						}
					}
				}
			}
			for (IpmemsStdTaskGroup e: groups) {
				String n = e.id();
				if (path.startsWith(n)) {
					if (path.length() == n.length()) return e; else {
						String w = path.substring(n.length());
						if (w.startsWith("/")) {
							IpmemsStdObject o = e.getObject(w.substring(1));
							if (o != null) return o;
						}
					}
				}
			}
			return null;
		}
	}
	
	@Override
	public Map<String,Object> query(
			final Map<String,Object> o,
			final List<Map<String,Object>> i,
			final Map<String,Object> obj,
			final Object ... args) throws Exception {
		return executor.submit(new Callable<Map<String,Object>>() {
			@Override
			public Map<String,Object> call() throws Exception {
				Map<String,Object> m = new HashMap<String,Object>(obj);
				m.put("input", i);
				m.put("output", o);
				IpmemsStdTask t = new IpmemsStdTask(
						IpmemsStdTaskQueue.this,
						UUID.randomUUID().toString(),
						Collections.singletonMap("params", m));
				return IpmemsProtUtil.ioTask(t, new Debug(args));
			}
		}).get();
	}
	
	@Override
	public Map<String,Object> query(
			final List<Map<String,Object>> o,
			final List<List<Map<String,Object>>> i,
			final Map<String,Object> obj,
			final Object ... args) throws Exception {
		return executor.submit(new Callable<Map<String,Object>>() {
			@Override
			public Map<String,Object> call() throws Exception {
				Map<String,Object> m = new HashMap<String,Object>(obj);
				m.put("input", i);
				m.put("output", o);
				IpmemsStdTask t = new IpmemsStdTask(
						IpmemsStdTaskQueue.this,
						UUID.randomUUID().toString(),
						Collections.singletonMap("params", m));
				return IpmemsProtUtil.ioVectorTask(t, new Debug(args));
			}
		}).get();
	}

	@Override
	public Map<String,Object> query(
			final IpmemsTask t,
			final Object ... args) throws Exception {
		return executor.submit(new Callable<Map<String,Object>>() {
			@Override
			public Map<String,Object> call() throws Exception {
				return IpmemsProtUtil.ioTask(t, new Debug(args));
			}
		}).get();
	}

	@Override
	public Map<String,Object> vectorQuery(
			final IpmemsTask t,
			final Object ... args) throws Exception {
		return executor.submit(new Callable<Map<String,Object>>() {
			@Override
			public Map<String,Object> call() throws Exception {
				return IpmemsProtUtil.ioVectorTask(t, new Debug(args));
			}
		}).get();
	}

	/**
	 * Get the queue timer.
	 * @return Queue timer.
	 */
	public ScheduledThreadPoolExecutor getTimer() {
		return timer;
	}

	/**
	 * Get the killer thread group.
	 * @return Killer thread group.
	 */
	public IpmemsFutureKiller<IpmemsTask> getKiller() {
		return killer;
	}
	
	private void add(IpmemsStdTaskGroup g) throws Exception {
		groups.add(g);
		g.call();
	}
	
	private void add(IpmemsStdTask t) throws Exception {
		tasks.add(t);
	}
		
	private final ArrayList<IpmemsStdTaskGroup> groups = 
			new ArrayList<IpmemsStdTaskGroup>();
	private final ArrayList<IpmemsStdTask> tasks = 
			new ArrayList<IpmemsStdTask>();
	private final ThreadPoolExecutor executor;
	private final ScheduledThreadPoolExecutor timer;
	private final TimeUnit timeUnit;
	private final IpmemsFutureKiller<IpmemsTask> killer;
}
