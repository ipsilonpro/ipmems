package org.ipsilon.ipmems.io;

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
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * IPMEMS GZIP utilities.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsGzipUtil {
	/**
	 * Encodes the data.
	 * @param msg Source message.
	 * @return Gzipped data.
	 */
	public static byte[] toGzip(byte[] msg) {
		ByteArrayOutputStream bos;
		GZIPOutputStream zos;
		try {
			bos = new ByteArrayOutputStream();
			zos = new GZIPOutputStream(bos) {
				{def.setLevel(Deflater.BEST_COMPRESSION);}
			};
			zos.write(msg, 0, msg.length);
			zos.close();
			return bos.toByteArray();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}
	
	/**
	 * Decodes the data.
	 * @param msg Gzipped message.
	 * @return Ungzipped data.
	 */
	public static byte[] fromGzip(byte[] msg) {
		ByteArrayInputStream bis;
		GZIPInputStream zis;
		ByteArrayOutputStream bos;
		try {
			bis = new ByteArrayInputStream(msg);
			zis = new GZIPInputStream(bis);
			byte[] zbuf = new byte[1024];
			bos = new ByteArrayOutputStream();
			while (true) {
				int l = zis.read(zbuf, 0, zbuf.length);
				if (l < 0) break;
				bos.write(zbuf, 0, l);
			}
			zis.close();
			bos.flush();
			return bos.toByteArray();
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}
}
