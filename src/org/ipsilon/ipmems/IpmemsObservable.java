package org.ipsilon.ipmems;

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

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * IPMEMS abstract observable.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsObservable implements IpmemsObservableItf {
	@Override
	public void addObserver(IpmemsObserver o) {
		obs.offer(o);
	}

	@Override
	public void removeObserver(IpmemsObserver o) {
		obs.remove(o);
	}

	@Override
	public void fireEvent(Object... args) {
		for (IpmemsObserver o: obs) o.event(this, args);
	}
	
	/**
	 * Observers.
	 */
	protected final ConcurrentLinkedQueue<IpmemsObserver> obs =
			new ConcurrentLinkedQueue<IpmemsObserver>();
}
