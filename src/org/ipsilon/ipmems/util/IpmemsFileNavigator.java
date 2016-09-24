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

import java.io.IOException;
import org.ipsilon.ipmems.io.IpmemsFileInfo;

/**
 * IPMEMS File Contents downloader/uploader interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsFileNavigator {
	/**
	 * Downloads the file.
	 * @param name File name.
	 * @return File text.
	 * @throws IOException An I/O exception.
	 */
	public String download(String name) throws IOException;
	
	/**
	 * Uploads the file.
	 * @param name File name.
	 * @param text File contents.
	 * @throws IOException An I/O exception.
	 */
	public boolean upload(String name, String text) throws IOException;
	
	/**
	 * Get the default file tree.
	 * @return Default file tree.
	 */
	public IpmemsFileInfo getFileTree();
	
	/**
	 * Get the file tree from path.
	 * @param path Target path.
	 * @return File tree.
	 */
	public IpmemsFileInfo getFileTree(String path);
}
