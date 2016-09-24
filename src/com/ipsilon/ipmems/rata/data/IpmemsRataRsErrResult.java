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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.ipsilon.ipmems.data.IpmemsThrowableData;

/**
 * IPMEMS error result.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataRsErrResult extends IpmemsRataRsAResult<Throwable> {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRsErrResult() {
	}
	
	/**
	 * Constructs the RATA error result.
	 * @param dur Duration.
	 * @param t Any throwable.
	 */
	public IpmemsRataRsErrResult(long dur, Throwable t) {
		super(dur, t.getClass());
		object = t;
	}

	@Override
	public Throwable getObject() {
		return object;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		IpmemsThrowableData d = new IpmemsThrowableData();
		d.setData(object);
		out.writeObject(d);
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		super.readExternal(in);
		IpmemsThrowableData d = (IpmemsThrowableData)in.readObject();
		object = d.getData();
	}

	@Override
	public String toString() {
		return "Error result: " + object;
	}
	
	private Throwable object;
}
