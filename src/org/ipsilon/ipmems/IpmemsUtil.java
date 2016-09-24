package org.ipsilon.ipmems;

import java.nio.charset.Charset;

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

/**
 * Auxiliary functions.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsUtil {	
	/**
	 * Calculates the 64-bit hash code.
	 * @param d Source data.
	 * @return Hash code.
	 */
	public static long hash(byte[] d) {
		final long m = 0xc6a4a7935bd1e995L;
		final int r = 47;
		long h = (0xe17a1465 & 0xffffffffL) ^ (d.length * m);
		int l = d.length / 8;
		for (int i = 0; i < l; i++) {
			final int j = i * 8;
			long k = ( (long)d[j  ]&0xff)      +(((long)d[j+1]&0xff)<<8 ) +
					 (((long)d[j+2]&0xff)<<16) +(((long)d[j+3]&0xff)<<24) +
					 (((long)d[j+4]&0xff)<<32) +(((long)d[j+5]&0xff)<<40) +
					 (((long)d[j+6]&0xff)<<48) +(((long)d[j+7]&0xff)<<56);	
			k *= m;
			k ^= k >>> r;
			k *= m;
			h ^= k;
			h *= m; 
		}
		switch (d.length % 8) {
			case 7: h ^= (long)(d[(d.length&~7)+6]&0xff) << 48;
			case 6: h ^= (long)(d[(d.length&~7)+5]&0xff) << 40;
			case 5: h ^= (long)(d[(d.length&~7)+4]&0xff) << 32;
			case 4: h ^= (long)(d[(d.length&~7)+3]&0xff) << 24;
			case 3: h ^= (long)(d[(d.length&~7)+2]&0xff) << 16;
			case 2: h ^= (long)(d[(d.length&~7)+1]&0xff) << 8;
			case 1: h ^= (long)(d[ d.length&~7   ]&0xff);
					h *= m;
		}
		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;
		return h;
	}
	
	/**
	 * Calculates the 64-bit hash code.
	 * @param cs Source string data.
	 * @return Hash code.
	 */
	public static long hash(String cs) {
		return hash(cs.getBytes(Charset.forName("UTF-8")));
	}
}
