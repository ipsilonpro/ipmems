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
 * IPMEMS set file contents request.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataRqWriteFile implements IpmemsRataRq {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRqWriteFile() {
	}
	
	/**
	 * Constructs the set file contents request.
	 * @param n File name.
	 * @param cnt Contents.
	 */
	public IpmemsRataRqWriteFile(String n, String cnt) {
		name = n;
		contents = cnt;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeInt(contents.length());
		out.writeChars(contents);
	}

	@Override
	public void readExternal(ObjectInput in) throws
			IOException, ClassNotFoundException {
		name = in.readUTF();
		char[] buf = new char[in.readInt()];
		for (int i = 0; i < buf.length; i++) buf[i] = in.readChar();
		contents = String.valueOf(buf);
	}

	/**
	 * Get the file name.
	 * @return File name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the file contents.
	 * @return File contents.
	 */
	public String getContents() {
		return contents;
	}

	@Override
	public String toString() {
		return "Write file: " + name;
	}
	
	private String name;
	private String contents;
}
