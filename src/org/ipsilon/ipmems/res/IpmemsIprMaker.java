package org.ipsilon.ipmems.res;

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
import java.io.FileFilter;
import java.io.FilenameFilter;
import org.ipsilon.ipmems.Ipmems;

/**
 * IPMEMS Binary Resource Maker.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsIprMaker {
	private static void resToBin(File dir) throws Exception {
		for (File f: dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".properties");
			}
		})) IpmemsResBundle.toBin(f);
		for (File d: dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) resToBin(d);
	}
	
	/**
	 * Makes IPMEMS binary resources.
	 * @param args Command-line arguments.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		resToBin(new File(Ipmems.JAR_DIR, "res"));
	}
}
