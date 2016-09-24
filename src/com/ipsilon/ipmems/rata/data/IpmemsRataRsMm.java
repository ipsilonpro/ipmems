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

package com.ipsilon.ipmems.rata.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.ipsilon.ipmems.util.IpmemsAdm;

/**
 * IPMEMS RATA method map response.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataRsMm implements IpmemsRataRs {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRsMm() {
	}
	
	/**
	 * Constructs the RATA method map response.
	 * @param adm Administration object.
	 */
	public IpmemsRataRsMm(IpmemsAdm adm) {
		mm = adm.getMethodMap();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(mm.size());
		for (Map.Entry<String,Set<String>> e: mm.entrySet()) {
			out.writeUTF(e.getKey());
			out.writeInt(e.getValue().size());
			for (String m: e.getValue()) out.writeUTF(m);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws
			IOException, ClassNotFoundException {
		int n = in.readInt();
		mm = new TreeMap<String,Set<String>>();
		for (int i = 0; i < n; i++) {
			String k = in.readUTF();
			int m = in.readInt();
			TreeSet<String> s = new TreeSet<String>();
			for (int j = 0; j < m; j++) s.add(in.readUTF());
			mm.put(k, s);
		}
	}

	/**
	 * Get the method map.
	 * @return Method map.
	 */
	public Map<String, Set<String>> getMethodMap() {
		return mm;
	}

	@Override
	public String toString() {
		return "Method map result";
	}
	
	private Map<String,Set<String>> mm;
}
