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
 * IPMEMS RATA log records.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataLogRecords implements Externalizable {
	/**
	 * Default constructor.
	 */
	public IpmemsRataLogRecords() {
	}
	
	public IpmemsRataLogRecords(String k, IpmemsLogRec[] recs) {
		key = k;
		records = recs;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(key);
		out.writeInt(records.length);
		for (IpmemsLogRec r: records) {
			IpmemsLogRecData d = new IpmemsLogRecData();
			d.setData(r);
			out.writeObject(d);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws 
			IOException, ClassNotFoundException {
		key = in.readUTF();
		records = new IpmemsLogRec[in.readInt()];
		for (int i = 0; i < records.length; i++) 
			records[i] = ((IpmemsLogRecData)in.readObject()).getData();
	}

	/**
	 * Get the logger records.
	 * @return Logger records.
	 */
	public IpmemsLogRec[] getRecords() {
		return records;
	}

	/**
	 * Get the logger key.
	 * @return Logger key.
	 */
	public String getKey() {
		return key;
	}
	
	private IpmemsLogRec[] records;
	private String key;
}
