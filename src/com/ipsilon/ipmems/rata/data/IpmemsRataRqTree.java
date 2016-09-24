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
 * IPMEMS RATA tree request.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataRqTree implements IpmemsRataRq {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRqTree() {
		name = "";
	}
	
	/**
	 * Constructs the RATA tree request data.
	 * @param n Root node name.
	 */
	public IpmemsRataRqTree(String n) {
		name = n;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(name.length());
		out.writeChars(name);
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		char[] buf = new char[in.readInt()];
		for (int i = 0; i < buf.length; i++) buf[i] = in.readChar();
		name = String.valueOf(buf);
	}

	/**
	 * Get the tree root name.
	 * @return Tree root name.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Tree: " + name;
	}
	
	private String name;
}
