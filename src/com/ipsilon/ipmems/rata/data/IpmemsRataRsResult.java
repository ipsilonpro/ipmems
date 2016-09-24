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
import org.ipsilon.ipmems.data.IpmemsData;
import org.ipsilon.ipmems.data.IpmemsDataTypes;

/**
 * IPMEMS RATA result data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataRsResult extends IpmemsRataRsAResult<Object> {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRsResult() {
	}
	
	/**
	 * Constructs the RATA result object.
	 * @param dur Duration.
	 * @param obj Associated object.
	 * @param c Object class.
	 */
	public IpmemsRataRsResult(long dur, Object obj, Class<?> c) {
		super(dur, c);
		object = obj;
	}

	@Override
	public Object getObject() {
		return object;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(IpmemsDataTypes.wrap(object));
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		super.readExternal(in);
		IpmemsData d = (IpmemsData)in.readObject();
		object = d.getData();
	}

	@Override
	public String toString() {
		return "Command result: " + (object == null ? null : object.getClass());
	}
	
	private Object object;
}
