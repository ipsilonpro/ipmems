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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.ipsilon.ipmems.logging.IpmemsLoggers;

/**
 * IPMEMS task group.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsStdTaskGroup extends 
		IpmemsStdObject implements IpmemsTaskGroup {
	/**
	 * Constructs the task group.
	 * @param p Parent object.
	 * @param n Object name.
	 * @param props Object properties.
	 */
	@SuppressWarnings("unchecked")
	public IpmemsStdTaskGroup(IpmemsStdObject p, String n, Map props) {
		super(p, n, props);
		IpmemsLoggers.info("sch", "{0} +", toString());
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
	}

	@Override
	public Object get(String k) {
		return (!super.containsKey(k) && getParent() instanceof IpmemsTaskGroup)
				? getParent().get(k) : super.get(k);
	}

	@Override
	public boolean containsKey(String k) {
		return (!super.containsKey(k) && getParent() instanceof IpmemsTaskGroup)
				? getParent().containsKey(k) : super.containsKey(k);
	}

	@Override
	public Set<String> getKeys() {
		if (getParent() instanceof IpmemsTaskGroup) {
			Set<String> s = new HashSet<String>(getParent().getKeys());
			s.addAll(getPropertyKeys());
			return s;
		} else return super.getKeys();
	}
		
	/**
	 * Get the task array.
	 * @return Task array.
	 */
	@Override
	public ArrayList<IpmemsStdTask> getTasks() {
		return tasks;
	}

	/**
	 * Get the task groups.
	 * @return Task groups.
	 */
	@Override
	public ArrayList<IpmemsStdTaskGroup> getGroups() {
		return groups;
	}
	
	/**
	 * Get the associated task queue.
	 * @return Associated task queue.
	 */
	@Override
	public IpmemsStdTaskQueue getTaskQueue() {
		for (IpmemsStdObject obj = getParent(); 
				obj != null; obj = obj.getParent())
			if (obj instanceof IpmemsStdTaskQueue) 
				return (IpmemsStdTaskQueue)obj;
		return null;
	}

	/**
	 * Runs the object.
	 */
	@Override
	public void start() {
		for (IpmemsStdTask t: tasks) t.start();
		for (IpmemsStdTaskGroup g: groups) g.start();
	}

	@Override
	public void stop() {
		for (IpmemsStdTask t: tasks) t.stop();
		for (IpmemsStdTaskGroup g: groups) g.stop();
		super.stop();
	}

	@Override
	public boolean isCase(Object obj) {
		return obj instanceof IpmemsStdTaskGroup ?
				groups.contains((IpmemsStdTaskGroup)obj) :
					obj instanceof IpmemsStdTask ?
					tasks.contains((IpmemsStdTask)obj) : super.isCase(obj);
	}
	
	private void add(IpmemsStdTaskGroup g) throws Exception {
		groups.add(g);
		g.call();
	}
	
	private void add(IpmemsStdTask t) throws Exception {
		tasks.add(t);
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
	
	private final ArrayList<IpmemsStdTask> tasks = 
			new ArrayList<IpmemsStdTask>();
	private final ArrayList<IpmemsStdTaskGroup> groups = 
			new ArrayList<IpmemsStdTaskGroup>();	
}
