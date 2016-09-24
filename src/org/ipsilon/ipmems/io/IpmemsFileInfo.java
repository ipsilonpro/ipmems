package org.ipsilon.ipmems.io;

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

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.ipsilon.ipmems.Ipmems;

/**
 * IPMEMS file info.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsFileInfo implements 
		Externalizable, Comparable<IpmemsFileInfo> {
	/**
	 * Default constructor.
	 */
	public IpmemsFileInfo() {
		children = new ArrayList<IpmemsFileInfo>();
	}
	
	/**
	 * Constructs the IPMEMS file info from existing file.
	 * @param f Existing file.
	 */
	public IpmemsFileInfo(File f) {
		this();
		String jarDir = Ipmems.JAR_DIR.getAbsolutePath();
		String p = f.getAbsolutePath();
		path = p.startsWith(jarDir) ? p.substring(jarDir.length()) : p;
		name = f.getName();
		if (path.isEmpty()) path = ".";
		size = f.length();
		directory = f.isDirectory();
		readable = f.canRead();
		writable = f.canWrite();
		hidden = f.isHidden();
		timestamp = f.lastModified();
		if (directory) {
			ArrayList<IpmemsFileInfo> dirs = new ArrayList<IpmemsFileInfo>();
			for (File i: f.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			})) dirs.add(new IpmemsFileInfo(i));
			ArrayList<IpmemsFileInfo> files = new ArrayList<IpmemsFileInfo>();
			for (File i: f.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return !file.isDirectory();
				}
			})) files.add(new IpmemsFileInfo(i));
			Collections.sort(dirs);
			Collections.sort(files);
			children.addAll(dirs);
			children.addAll(files);
		}
	}
	
	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		path = in.readUTF();
		name = in.readUTF();
		size = in.readLong();
		directory = in.readBoolean();
		readable = in.readBoolean();
		writable = in.readBoolean();
		hidden = in.readBoolean();
		timestamp = in.readLong();
		int n = in.readInt();
		for (int i = 0; i < n; i++)
			children.add((IpmemsFileInfo)in.readObject());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(path);
		out.writeUTF(name);
		out.writeLong(size);
		out.writeBoolean(directory);
		out.writeBoolean(readable);
		out.writeBoolean(writable);
		out.writeBoolean(hidden);
		out.writeLong(timestamp);
		out.writeInt(children.size());
		for (IpmemsFileInfo i: children) out.writeObject(i);
	}

	@Override
	public int compareTo(IpmemsFileInfo o) {
		return name.compareTo(o.name);
	}

	/**
	 * Get the file name.
	 * @return File name.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get file size.
	 * @return File size.
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Get the isDirectory flag.
	 * @return isDirectory flag.
	 */
	public boolean isDirectory() {
		return directory;
	}

	/**
	 * Get the isReadable flag.
	 * @return isReadable flag.
	 */
	public boolean isReadable() {
		return readable;
	}

	/**
	 * Get the isWritable flag.
	 * @return isWritable flag.
	 */
	public boolean isWritable() {
		return writable;
	}

	/**
	 * Get the isHidden flag.
	 * @return isHidden flag.
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Get the file timestamp.
	 * @return File timestamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the file children list.
	 * @return List of children.
	 */
	public List<IpmemsFileInfo> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return name;
	}
	
	private String path;
	private String name;
	private long size;
	private boolean directory;
	private boolean readable;
	private boolean writable;
	private boolean hidden;
	private long timestamp;
	private final ArrayList<IpmemsFileInfo> children;
}
