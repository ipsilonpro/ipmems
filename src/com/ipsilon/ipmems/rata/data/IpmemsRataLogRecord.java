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

package com.ipsilon.ipmems.rata.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.ipsilon.ipmems.data.IpmemsLogRecData;
import org.ipsilon.ipmems.logging.IpmemsLogRec;

/**
 * IPMEMS RATA log record.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataLogRecord implements Externalizable {
	/**
	 * Default constructor.
	 */
	public IpmemsRataLogRecord() {
	}
	
	/**
	 * Constructs the RATA log record.
	 * @param k Logger's key.
	 * @param lr Log record.
	 */
	public IpmemsRataLogRecord(String k, IpmemsLogRec lr) {
		key = k;
		record = lr;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(key);
		IpmemsLogRecData d = new IpmemsLogRecData();
		d.setData(record);
		out.writeObject(d);
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		key = in.readUTF();
		record = ((IpmemsLogRecData)in.readObject()).getData();
	}

	/**
	 * Get the logger key.
	 * @return Logger key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get the log record.
	 * @return Log record.
	 */
	public IpmemsLogRec getRecord() {
		return record;
	}
	
	private String key;
	private IpmemsLogRec record;
}
