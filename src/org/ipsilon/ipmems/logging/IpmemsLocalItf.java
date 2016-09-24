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

import java.io.File;
import java.io.IOException;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.util.IpmemsLocalFileNavigator;

/**
 * IPMEMS local log extractor.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLocalItf implements IpmemsRemoteItf {
	/**
	 * Constructs the local log extractor.
	 */
	public IpmemsLocalItf() {
		fileContents = new IpmemsLocalFileNavigator();
	}

	@Override
	public String getMainLogText() throws IOException {
		return IpmemsIOLib.getText(new File(Ipmems.JAR_DIR, "ipmems.log"));
	}

	@Override
	public IpmemsLocalFileNavigator getFileInterface() {
		return fileContents;
	}

	@Override
	public void addListener(IpmemsLoggersListener l) {
		IpmemsLoggers.addLogListener(l);
	}

	@Override
	public void removeListener(IpmemsLoggersListener l) {
		IpmemsLoggers.removeLogListener(l);
	}

	@Override
	public void log(boolean state) {
	}

	@Override
	public String toString() {
		return "local";
	}
	
	private final IpmemsLocalFileNavigator fileContents;
}
