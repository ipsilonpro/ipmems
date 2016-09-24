package org.ipsilon.ipmems.data;

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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Properties;

/**
 * IPMEMS properties data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsPropData extends IpmemsAbstractData<Properties> {
	@Override
	public void writeExternal(ObjectOutput o) throws IOException {
		o.writeInt(getData().size());
		for (String p: getData().stringPropertyNames()) {
			o.writeUTF(p);
			o.writeUTF(getData().getProperty(p));
		}
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		int n = in.readInt();
		Properties p = new Properties();
		for (int i = 0; i < n; i++)
			p.setProperty(in.readUTF(), in.readUTF());
		setData(p);
	}
}
