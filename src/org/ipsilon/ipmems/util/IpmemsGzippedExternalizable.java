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

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * IPMEMS gzipped externalizable object.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsGzippedExternalizable implements Externalizable {
	/**
	 * Default constructor.
	 */
	public IpmemsGzippedExternalizable() {
	}

	/**
	 * Constructs the externalizable object.
	 * @param o An externalizable.
	 */
	public IpmemsGzippedExternalizable(Externalizable o) {
		externalizable = o;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream gzos = new GZIPOutputStream(bos) {{
			def.setLevel(Deflater.BEST_COMPRESSION);
		}};
		ObjectOutputStream os = new ObjectOutputStream(gzos);
		os.writeObject(externalizable);
		os.flush();
		gzos.finish();
		os.close();
		out.writeInt(bos.size());
		out.write(bos.toByteArray());
	}

	@Override
	public void readExternal(ObjectInput in) throws 
			IOException, ClassNotFoundException {
		byte[] buf = new byte[in.readInt()];
		in.readFully(buf);
		ByteArrayInputStream bis = new ByteArrayInputStream(buf);
		GZIPInputStream gzis = new GZIPInputStream(bis);
		ObjectInputStream is = new ObjectInputStream(gzis);
		externalizable = (Externalizable)is.readObject();
		is.close();
	}

	/**
	 * Get the uncompressed externalizable object.
	 * @return Uncompressed object.
	 */
	public Externalizable getObject() {
		return externalizable;
	}
	
	private Externalizable externalizable;
}
