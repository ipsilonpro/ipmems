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
import java.util.List;
import java.util.Map;
import org.ipsilon.ipmems.logging.IpmemsLoggers;

/**
 * IPMEMS standard scheduler.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsStdScheduler extends 
		IpmemsStdObject implements IpmemsScheduler {
	/**
	 * Constructs the default scheduler.
	 * @param props Scheduler properties.
	 * @throws Exception An exception.
	 */
	@SuppressWarnings("unchecked")
	public IpmemsStdScheduler(Map props) throws Exception {
		super(null, "", props);
		if (props.containsKey("queues")) try {
			Map<String,Map> qs = (Map)props.remove("queues");
			for (Map.Entry<String,Map> e: qs.entrySet()) try {
				add(new IpmemsStdTaskQueue(this, e.getKey(), e.getValue()));
			} catch (Exception x) {
				IpmemsLoggers.warning("err",
						"{0} + {1}", x, getName(), e.getKey());
			}
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "{0}", x, getName());
		}
	}
		
	@Override
	public List<IpmemsStdTaskQueue> getQueues() {
		return queues;
	}

	@Override
	public boolean isCase(Object obj) {
		return obj instanceof IpmemsStdTaskQueue ?
				queues.contains((IpmemsStdTaskQueue)obj) : super.isCase(obj);
	}
	
	@Override
	public void start() {
		for (IpmemsStdTaskQueue q: queues) q.start();
	}

	@Override
	public void stop() {
		for (IpmemsStdTaskQueue q: queues) q.stop();
		super.stop();
	}
	
	@Override
	public IpmemsStdObject getObject(String p) {
		IpmemsStdObject obj = super.getObject(p);
		if (obj != null) return obj; else {
			for (IpmemsStdTaskQueue q: queues) {
				String n = q.id();
				if (p.startsWith(n)) {
					if (n.length() == p.length()) return q;	else {
						String w = p.substring(n.length());
						if (w.startsWith("/")) {
							IpmemsStdObject o = q.getObject(w.substring(1));
							if (o != null) return o;
						}
					}
				}
			}
			return null;
		}
	}
	
	private void add(IpmemsStdTaskQueue q) throws Exception {
		queues.add(q);
		q.call();
	}
	
	private final ArrayList<IpmemsStdTaskQueue> queues = 
			new ArrayList<IpmemsStdTaskQueue>();
}
