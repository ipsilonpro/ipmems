package org.ipsilon.ipmems.util;

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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.io.IpmemsFileInfo;
import org.ipsilon.ipmems.io.IpmemsIOLib;

/**
 * IPMEMS local file contents.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLocalFileNavigator implements IpmemsFileNavigator {
	@Override
	public String download(String name) throws IOException {
		File f = locateScriptFile(name);
		return f == null ? null : IpmemsIOLib.getText(f);
	}

	@Override
	public boolean upload(String name, String text) throws IOException {
		File f = locateScriptFile(name);
		if (f != null) {
			IpmemsIOLib.setText(f, text);
			return true;
		} else return false;
	}

	@Override
	public IpmemsFileInfo getFileTree() {
		return new IpmemsFileInfo(Ipmems.JAR_DIR);
	}

	@Override
	public IpmemsFileInfo getFileTree(String path) {
		File f = new File(path);
		if (f.exists()) return new IpmemsFileInfo(f); else {
			f = new File(Ipmems.JAR_DIR, path);
			if (f.exists()) return new IpmemsFileInfo(f);
			else return null;
		}
	}
	
	/**
	 * Locates the script file in local file system.
	 * @param name File name.
	 * @return File object or null.
	 */
	public static File locateScriptFile(final String name) {
		File file = new File(name);
		if (file.exists()) return file;
		file = new File(Ipmems.JAR_DIR, name);
		if (file.exists()) return file;
		FilenameFilter f = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String n) {
				return n.equals(name);
			}
		};
		List<File> scripts = IpmemsFile.listFilesRecurse(new File(
				Ipmems.sst("scriptsDirectory", "@{jarDir}/scripts")), f);
		if (scripts.size() > 0) return scripts.get(0);
		List<File> wscripts = IpmemsFile.listFilesRecurse(new File(
				Ipmems.sst("webDirectory", "@{jarDir}/web")), f);
		if (wscripts.size() > 0) return wscripts.get(0);
		return null;
	}
}
