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

/**
 * IPMEMS abstract result.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsRataRsAResult<T> implements IpmemsRataRs {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRsAResult() {	
	}
	
	/**
	 * Constructs the RATA abstract result.
	 * @param dur Duration.
	 * @param c Object class.
	 */
	public IpmemsRataRsAResult(long dur, Class<?> c) {
		duration = dur;
		className = c.getName();
	}

	/**
	 * Get the process duration.
	 * @return Process duration.
	 */
	public long getDuration() {
		return duration;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(duration);
		out.writeUTF(className);
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		duration = in.readLong();
		className = in.readUTF();
	}
	
	/**
	 * Get the associated object.
	 * @return Associated object.
	 */
	public abstract T getObject();

	/**
	 * Get the class name.
	 * @return Class name.
	 */
	public String getClassName() {
		return className;
	}
	
	private long duration;
	private String className;
}
