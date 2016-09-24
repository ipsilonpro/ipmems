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

package com.ipsilon.ipmems.rata;

import com.ipsilon.ipmems.rata.data.IpmemsRataLogData;
import java.io.Closeable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.ipsilon.ipmems.logging.IpmemsLogHandler;
import org.ipsilon.ipmems.logging.IpmemsLogRec;
import org.ipsilon.ipmems.logging.IpmemsLoggerItf;

/**
 * IPMEMS RATA logger.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsRataLogger implements IpmemsLoggerItf, Closeable {
	/**
	 * Constructs the IPMEMS RATA logger.
	 * @param d Event data.
	 */
	public IpmemsRataLogger(IpmemsRataLogData d) {
		data = d;
	}

	@Override
	public String getKey() {
		return data.getKey();
	}

	@Override
	public int compareTo(IpmemsLoggerItf o) {
		return getKey().compareTo(o.getKey());
	}

	@Override
	public void addHandler(IpmemsLogHandler h) {
		hs.add(h);
	}

	@Override
	public void removeHandler(IpmemsLogHandler h) {
		hs.remove(h);
	}

	@Override
	public void log(IpmemsLogRec r) {
		for (IpmemsLogHandler h: hs) h.publish(r);
	}

	@Override
	public void log(IpmemsLogRec[] rs) {
		for (IpmemsLogHandler h: hs) h.publish(rs);
	}

	@Override
	public void close() {
		hs.clear();
	}
	
	private final IpmemsRataLogData data;
	private final ConcurrentLinkedQueue<IpmemsLogHandler> hs =
			new ConcurrentLinkedQueue<IpmemsLogHandler>();
}
