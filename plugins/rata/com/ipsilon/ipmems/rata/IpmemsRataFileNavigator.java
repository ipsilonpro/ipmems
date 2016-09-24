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

package com.ipsilon.ipmems.rata;

import com.ipsilon.ipmems.rata.data.*;
import java.io.IOException;
import org.ipsilon.ipmems.io.IpmemsFileInfo;
import org.ipsilon.ipmems.util.IpmemsFileNavigator;

/**
 * IPMEMS RATA file interface.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataFileNavigator implements IpmemsFileNavigator {
	/**
	 * IPMEMS RATA file navigator.
	 * @param c Associated client.
	 */
	public IpmemsRataFileNavigator(IpmemsGuiRataClient c) {
		client = c;
	}

	@Override
	public String download(String name) throws IOException {
		client.write(new IpmemsRataRqFile(name));
		IpmemsRataRs rs = client.read();
		if (rs instanceof IpmemsRataRsFile)
			return ((IpmemsRataRsFile)rs).getContents();
		else if (rs instanceof IpmemsRataRsErr)
			throw new IOException(((IpmemsRataRsErr)rs).getThrown());
		else throw new IllegalStateException();
	}

	@Override
	public boolean upload(String name, String text) throws IOException {
		client.write(new IpmemsRataRqWriteFile(name, text));
		IpmemsRataRs rs = client.read();
		if (rs instanceof IpmemsRataRsWriteFile)
			return ((IpmemsRataRsWriteFile)rs).getResult();
		else if (rs instanceof IpmemsRataRsErr)
			throw new IOException(((IpmemsRataRsErr)rs).getThrown());
		else throw new IllegalStateException();
	}

	@Override
	public IpmemsFileInfo getFileTree() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public IpmemsFileInfo getFileTree(String path) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	private final IpmemsGuiRataClient client;
}
