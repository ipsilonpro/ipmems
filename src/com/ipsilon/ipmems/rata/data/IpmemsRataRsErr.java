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
 * IPMEMS RATA error.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataRsErr implements IpmemsRataRs {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRsErr() {
	}
	
	/**
	 * Constructs IPMEMS RATA error.
	 * @param x Throwable object.
	 */
	public IpmemsRataRsErr(Throwable x) {
		thrown = x;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		IpmemsThrowableData d = new IpmemsThrowableData();
		d.setData(thrown);
		out.writeObject(d);
	}

	@Override
	public void readExternal(ObjectInput in) throws 
			IOException, ClassNotFoundException {
		thrown = ((IpmemsThrowableData)in.readObject()).getData();
	}

	/**
	 * Get the throwable.
	 * @return Throwable object.
	 */
	public Throwable getThrown() {
		return thrown;
	}

	@Override
	public String toString() {
		return "Error: " + thrown;
	}
	
	private Throwable thrown;
}
