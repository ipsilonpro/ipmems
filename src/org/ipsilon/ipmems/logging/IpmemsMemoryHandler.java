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
import org.ipsilon.ipmems.Ipmems;

/**
 * Memory logging handler.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsMemoryHandler extends IpmemsLogAbstractHandler {
	/**
	 * Default constructor.
	 */
	public IpmemsMemoryHandler() {
		this(Ipmems.get(Integer.class, "logMemMaxSize", 1024));
	}
	
	/**
	 * Constructs the memory handler.
	 * @param max Maximum records limit.
	 */
	public IpmemsMemoryHandler(int max) {
		records = new IpmemsLogRec[max];
	}
		
	/**
	 * Clears the log handler.
	 */
	public synchronized void clear() {
		for (int i = 0; i < size; i++) records[i] = null;
		records = null;
	}
	
	/**
	 * Get the record list size.
	 * @return Record list size.
	 */
	public synchronized int getSize() {
		return size;
	}
		
	@Override
	public synchronized boolean publish(IpmemsLogRec record) {
		if (!super.publish(record)) return false;
		if (records.length == size) {
			for (int i = 0; i < size - 1; i++) records[i] = records[i + 1];
			records[size - 1] = record;
		} else {
			records[size++] = record;
		}
		return true;
	}

	@Override
	public synchronized IpmemsLogRec[] publish(IpmemsLogRec[] records) {
		IpmemsLogRec[] rs = super.publish(records);
		int n = 0;
		IpmemsLogRec[] recs = new IpmemsLogRec[rs.length];
		for (IpmemsLogRec r: rs) if (publish(r)) recs[n++] = r;
		return Arrays.copyOf(recs, n);
	}
	
	/**
	 * Publishes all the log records onto the handler.
	 * @param h Log handler.
	 */
	public synchronized void publish(IpmemsLogHandler h) {
		h.publish(getRecords());
	}

	@Override
	public void close() {
		clear();
	}
	
	/**
	 * Get the record by index.
	 * @param index Element index.
	 * @return Log record.
	 */
	public synchronized IpmemsLogRec get(int index) {
		return records[index];
	}
	
	/**
	 * Get the record index.
	 * @param r A log record.
	 * @return Index of record.
	 */
	public synchronized int indexOf(IpmemsLogRec r) {
		for (int i = 0; i < records.length; i++)
			if (records[i] == r) return i;
		return -1;
	}
	
	/**
	 * Get all the records.
	 * @return All the records as array.
	 */
	public synchronized IpmemsLogRec[] getRecords() {
		return Arrays.copyOf(records, size);
	}
					
	/**
	 * Records store.
	 */
	private IpmemsLogRec[] records;
	
	/**
	 * Current size.
	 */
	private int size;
}
