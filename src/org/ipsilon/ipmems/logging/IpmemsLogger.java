package org.ipsilon.ipmems.logging;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * IPMEMS Logger.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLogger implements IpmemsLoggerItf {
	/**
	 * Constructs the logger.
	 * @param k Logger's key.
	 * @param l Initial level.
	 */
	public IpmemsLogger(String k, int l) {
		key = k;
		level = l;
	}

	/**
	 * Get the logging level.
	 * @return Logging level.
	 */
	public final int getLevel() {
		return level;
	}

	/**
	 * Logging level.
	 * @param l Logging level.
	 */
	public final void setLevel(int l) {
		level = l;
	}

	/**
	 * Get the logging filter.
	 * @return Logging filter.
	 */
	public IpmemsLogFilter getFilter() {
		return filter;
	}

	/**
	 * Set the logging filter.
	 * @param f Logging filter.
	 */
	public void setFilter(IpmemsLogFilter f) {
		filter = f;
	}
	
	/**
	 * Log the record.
	 * @param lr Log record.
	 */
	@Override
	public final void log(IpmemsLogRec lr) {
		final IpmemsLogFilter f = filter;
		if (lr.getLevel() < level || (f != null && !f.isLoggable(lr))) return;
		for (IpmemsLogHandler h: handlers)
			try {h.publish(lr);} catch (Exception x) {}
	}

	@Override
	public final void log(IpmemsLogRec[] rs) {
		IpmemsLogRec[] recs = new IpmemsLogRec[rs.length];
		int n = 0;
		final IpmemsLogFilter f = filter;
		for (IpmemsLogRec r: rs)
			if (r.getLevel() >= level && (f == null || f.isLoggable(r)))
				recs[n++] = r;
		for (IpmemsLogHandler h: handlers)
			try {h.publish(Arrays.copyOf(recs, n));} catch (Exception x) {}
	}

	/**
	 * Adds a handler.
	 * @param h Handler.
	 */
	@Override
	public void addHandler(IpmemsLogHandler h) {
		handlers.add(h);
		IpmemsMemoryHandler mh = getMemoryHandler();
		if (mh != null) mh.publish(h);
	}
	
	/**
	 * Removes the handler.
	 * @param h Handler.
	 */
	@Override
	public void removeHandler(IpmemsLogHandler h) {
		handlers.remove(h);
	}
		
	/**
	 * Get the first memory handler.
	 * @return First memory handler.
	 */
	public IpmemsMemoryHandler getMemoryHandler() {
		for (IpmemsLogHandler h: handlers)
			if (h instanceof IpmemsMemoryHandler) return (IpmemsMemoryHandler)h;
		return null;
	}

	/**
	 * Get logger's name.
	 * @return Logger's name.
	 */
	@Override
	public String getKey() {
		return key;
	}
	
	/**
	 * Get the log handlers.
	 * @return Log handlers array.
	 */
	public Collection<IpmemsLogHandler> getHandlers() {
		return handlers;
	}

	@Override
	public int compareTo(IpmemsLoggerItf o) {
		return getKey().compareTo(o.getKey());
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		for (IpmemsLogHandler h: handlers) try {
			h.close();
		} catch (Exception x) {}
		handlers.clear();
		super.finalize();
	}
	
	private volatile IpmemsLogFilter filter = null;
	private volatile int level = Integer.MIN_VALUE;
	private final String key;
	private final ConcurrentLinkedQueue<IpmemsLogHandler> handlers =
			new ConcurrentLinkedQueue<IpmemsLogHandler>();
}
