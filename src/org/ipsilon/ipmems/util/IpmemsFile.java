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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * File utilities.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsFile {
	/**
	 * Get the file list by filter.
	 * @param dir A directory to search from.
	 * @param filter File name filter.
	 * @return File list.
	 */
	public static List<File> listFilesRecurse(File dir, FilenameFilter filter) {
		return listFileRecurse(new ArrayList<File>(), dir, filter);
	}
	
	/**
	 * Get the file list by filter.
	 * @param dir A directory to search from.
	 * @param filter File filter.
	 * @return File list.
	 */
	public static List<File> listFilesRecurse(File dir, FileFilter filter) {
		return listFileRecurse(new ArrayList<File>(), dir, filter);
	}
	
	/**
	 * Get the file extension.
	 * @param f File name.
	 * @return File extension.
	 */
	public static String getFileExtension(String f) {
		if (f == null) return "";
		int lastIndex = f.lastIndexOf('.');
		return lastIndex > 0 ? f.substring(lastIndex + 1) : "";
	}
	
	/**
	 * Get the file extension.
	 * @param f A file.
	 * @return File extension.
	 */
	public static String getFileExtension(File f) {
		String fileName = f.getName();
		int lastIndex = fileName.lastIndexOf('.');
		return lastIndex > 0 ? fileName.substring(lastIndex + 1) : "";
	}
	
	/**
	 * Get the URL file extension.
	 * @param url An URL.
	 * @return URL file extension.
	 */
	public static String getFileExtension(URL url) {
		String fileName = url.getFile();
		int lastIndex = fileName.lastIndexOf('.');
		return lastIndex > 0 ? fileName.substring(lastIndex + 1) : "";
	}
	
	/**
	 * Deletes the file or entire directory.
	 * @param d File.
	 */
	public static void delete(File d) {
		if (d.isDirectory()) {
			for (File f: d.listFiles()) delete(f);
			d.delete();
		} else d.delete();
	}
	
	private static List<File> listFileRecurse(
			List<File> files, File dir, FilenameFilter f) {
		try {files.addAll(Arrays.asList(dir.listFiles(f)));} finally {}
		try {
			for (File d: dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			})) listFileRecurse(files, d, f);
		} finally {}
		return files;
	}
	
	private static List<File> listFileRecurse(
			List<File> files, File dir, FileFilter f) {
		try {files.addAll(Arrays.asList(dir.listFiles(f)));} finally {}
		try {
			for (File d: dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			})) listFileRecurse(files, d, f);
		} finally {}
		return files;
	}
}
