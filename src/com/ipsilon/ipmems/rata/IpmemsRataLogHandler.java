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

import com.ipsilon.ipmems.rata.data.IpmemsRataLogRecord;
import com.ipsilon.ipmems.rata.data.IpmemsRataLogRecords;
import org.ipsilon.ipmems.logging.IpmemsLogAbstractHandler;
import org.ipsilon.ipmems.logging.IpmemsLogEventData;
import org.ipsilon.ipmems.logging.IpmemsLogRec;

/**
 * IPMEMS RATA log handler.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataLogHandler extends IpmemsLogAbstractHandler {
	/**
	 * Constructs the RATA log handler.
	 * @param d Log event data.
	 * @param ctx RATA context.
	 */
	public IpmemsRataLogHandler(IpmemsLogEventData d, IpmemsRataContext ctx) {
		data = d;
		context = ctx;
	}

	@Override
	public boolean publish(IpmemsLogRec record) {
		if (!super.publish(record)) return false;
		context.out(new IpmemsRataLogRecord(data.getLogger().getKey(), record));
		return true;
	}

	@Override
	public IpmemsLogRec[] publish(IpmemsLogRec[] records) {
		IpmemsLogRec[] rs = super.publish(records);
		context.out(new IpmemsRataLogRecords(data.getLogger().getKey(), rs));
		return rs;
	}

	private final IpmemsLogEventData data;
	private final IpmemsRataContext context;
}
