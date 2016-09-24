package org.ipsilon.ipmems.logging;

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
import org.ipsilon.ipmems.util.IpmemsFileNavigator;

/**
 * IPMEMS remote interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsRemoteItf extends IpmemsLoggersItf {
	/**
	 * Get the main log text.
	 * @return Main log text.
	 * @throws IOException An I/O exception.
	 */
	public String getMainLogText() throws IOException;
	
	/**
	 * Get the associated file downloader/uploader interface.
	 * @return Associated file downloader/uploader interface.
	 */
	public IpmemsFileNavigator getFileInterface();
	
	/**
	 * Sets the log monitoring on/off.
	 * @param state Monitoring state.
	 */
	public void log(boolean state);
}
