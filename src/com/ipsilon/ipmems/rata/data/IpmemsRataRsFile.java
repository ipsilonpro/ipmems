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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.data.IpmemsStringData;
import org.ipsilon.ipmems.data.IpmemsThrowableData;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.util.IpmemsLocalFileNavigator;

/**
 * IPMEMS file contents.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataRsFile implements IpmemsRataRs {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRsFile() {
	}
	
	/**
	 * Constructs the RATA file contents.
	 * @param fn File name.
	 */
	public IpmemsRataRsFile(String fn) {
		name = Ipmems.substituted(fn);
		File f = IpmemsLocalFileNavigator.locateScriptFile(name);
		try {
			contents = IpmemsIOLib.getText(f);
		} catch (Exception x) {
			error = x;
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeBoolean(contents != null);
		if (contents != null) {
			IpmemsStringData d = new IpmemsStringData();
			d.setData(contents);
			out.writeObject(d);
		}
		out.writeBoolean(error != null);
		if (error != null) {
			IpmemsThrowableData d = new IpmemsThrowableData();
			d.setData(error);
			out.writeObject(d);
		}
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		name = in.readUTF();
		if (in.readBoolean())
			contents = ((IpmemsStringData)in.readObject()).getData();
		if (in.readBoolean())
			error = ((IpmemsThrowableData)in.readObject()).getData();
	}

	/**
	 * Get the file contents.
	 * @return File contents.
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * Get the I/O error.
	 * @return I/O error.
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * Get the file name.
	 * @return File name.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "File: " + name + (error != null ? ", " + error : "");
	}
	
	private String contents;
	private Throwable error;
	private String name;
}
