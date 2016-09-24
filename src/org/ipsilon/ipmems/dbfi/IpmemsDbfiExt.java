package org.ipsilon.ipmems.dbfi;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.ipsilon.ipmems.data.IpmemsDataTypes;
import org.ipsilon.ipmems.data.IpmemsMapData;

/**
 * IPMEMS DBFI externalizable data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDbfiExt extends IpmemsAbstractDbfi {
	@Override
	public Map decodeMap(byte[] message) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(message));
			return ((IpmemsMapData)ois.readObject()).getData();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		} finally {
			if (ois != null) try {ois.close();} catch (Exception x) {}
		}
	}

	@Override
	public byte[] encodeMap(Map map) {
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(IpmemsDataTypes.wrap(map));
			oos.flush();
			return bos.toByteArray();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		} finally {
			if (oos != null) try {oos.close();} catch (Exception x) {}
		}
	}
}
