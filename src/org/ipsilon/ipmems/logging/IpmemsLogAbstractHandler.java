package org.ipsilon.ipmems.logging;

import java.util.Arrays;

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

/**
 * IPMEMS log abstract handler.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsLogAbstractHandler implements IpmemsLogHandler {
	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int lev) {
		level = lev;
	}
	
	@Override
	public IpmemsLogFilter getFilter() {
		return flt;
	}

	@Override
	public void setFilter(IpmemsLogFilter f) {
		flt = f;
	}

	@Override
	public boolean publish(IpmemsLogRec r) {
		return r.getLevel() < level ? false : flt == null || flt.isLoggable(r);
	}

	@Override
	public IpmemsLogRec[] publish(IpmemsLogRec[] records) {
		int n = 0;
		IpmemsLogRec[] recs = new IpmemsLogRec[records.length];
		for (IpmemsLogRec r: records)
			if (r.getLevel() >= level && (flt == null || flt.isLoggable(r)))
				recs[n++] = r;
		return Arrays.copyOf(recs, n);
	}

	@Override
	public void close() {
	}

	private volatile IpmemsLogFilter flt;
	private volatile int level = Integer.MIN_VALUE;
}
