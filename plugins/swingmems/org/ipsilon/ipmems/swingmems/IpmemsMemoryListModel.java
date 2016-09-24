package org.ipsilon.ipmems.swingmems;

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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import static javax.swing.event.ListDataEvent.*;
import javax.swing.event.ListDataListener;
import org.ipsilon.ipmems.logging.IpmemsLogAbstractHandler;
import org.ipsilon.ipmems.logging.IpmemsLogEventData;
import org.ipsilon.ipmems.logging.IpmemsLogHandler;
import org.ipsilon.ipmems.logging.IpmemsLogRec;

/**
 * IPMEMS memory list model.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsMemoryListModel extends 
		IpmemsLogAbstractHandler implements ListModel {
	/**
	 * Constructs the memory list model.
	 * @param d Log event data.
	 */
	public IpmemsMemoryListModel(IpmemsLogEventData d) {
		data = d;
		data.getLogger().addHandler((IpmemsLogHandler)this);
	}
	
	@Override
	public boolean publish(final IpmemsLogRec record) {
		if (!super.publish(record)) return false;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				int n = records.size();
				records.add(record);
				ListDataListener[] ls = ll.getListeners(ListDataListener.class);
				ListDataEvent e = new ListDataEvent(this, INTERVAL_ADDED, n, n);
				for (int i = ls.length - 1; i >= 0; i--) ls[i].intervalAdded(e);
			}
		});
		return true;
	}

	@Override
	public IpmemsLogRec[] publish(IpmemsLogRec[] r) {
		final IpmemsLogRec[] rs = super.publish(r);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				int n = records.size();
				records.addAll(Arrays.asList(rs));
				ListDataListener[] ls = ll.getListeners(ListDataListener.class);
				int f = n + rs.length;
				ListDataEvent e = new ListDataEvent(this, INTERVAL_ADDED, n, f);
				for (int i = ls.length - 1; i >= 0; i--) ls[i].intervalAdded(e);
			}
		});
		return rs;
	}
	
	/**
	 * Clears the model.
	 */
	public void clear() {
		if (records.isEmpty()) return;
		int idx = records.size() - 1;
		records.clear();
		ListDataListener[] ls = ll.getListeners(ListDataListener.class);
		ListDataEvent e = new ListDataEvent(this, INTERVAL_REMOVED, 0, idx);
		for (int i = ls.length - 1; i >= 0; i--) ls[i].intervalRemoved(e);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		ll.add(ListDataListener.class, l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		ll.remove(ListDataListener.class, l);
	}

	@Override
	public void close() {
		data.getLogger().removeHandler(this);
		clear();
	}
	
	@Override
	public int getSize() {
		if (logLevel == Integer.MIN_VALUE) return records.size();
		else {
			int n = 0;
			for (IpmemsLogRec r: records)
				if (r.getLevel() == logLevel) n++;
			return n;
		}
	}

	@Override
	public IpmemsLogRec getElementAt(int index) {
		if (logLevel == Integer.MIN_VALUE) {
			return records.get(index);
		} else {
			int n = 0;
			for (IpmemsLogRec r: records)
				if (r.getLevel() == logLevel && n++ == index) return r;
			return null;
		}
	}
	
	/**
	 * Sets the log level for filtering.
	 * @param l Log level.
	 */
	public void setFilterLogLevel(int l) {
		int n = getSize() - 1;
		logLevel = l;
		ListDataListener[] ls = ll.getListeners(ListDataListener.class);
		ListDataEvent e = new ListDataEvent(this, CONTENTS_CHANGED, 0, n);
		for (int i = ls.length - 1; i >= 0; i--) ls[i].contentsChanged(e);
	}
	
	/**
	 * Get the current log level for filtering.
	 * @return Log level.
	 */
	public int getFilterLogLevel() {
		return logLevel;
	}

	/**
	 * Get the event data.
	 * @return Log event data.
	 */
	public IpmemsLogEventData getData() {
		return data;
	}
		
	private final IpmemsLogEventData data;
	private final List<IpmemsLogRec> records = new ArrayList<IpmemsLogRec>();
	private final EventListenerList ll = new EventListenerList();
	private int logLevel = Integer.MIN_VALUE;
}
