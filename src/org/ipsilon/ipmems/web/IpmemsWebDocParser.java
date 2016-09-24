package org.ipsilon.ipmems.web;

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

import java.util.*;
import java.util.regex.Pattern;
import org.ipsilon.ipmems.IpmemsClosure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * IPMEMS document parser.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsWebDocParser implements IpmemsClosure {
	/**
	 * Constructs the IPMEMS web document parser.
	 * @param doc Document.
	 */
	public IpmemsWebDocParser(Document doc) {
		document = doc;
	}

	@Override
	public Map<String,Element> call(Object... args) {
		String t = args[0].toString();
		List e = (List)args[1];
		HashMap<String,Element> m = new HashMap<String,Element>();
		NodeList nl = document.getElementsByTagName(t);
		TreeSet<String> strIds = new TreeSet<String>();
		ArrayList<Pattern> pIds = new ArrayList<Pattern>();
		for (Object id: e)
			if (id instanceof Pattern) pIds.add((Pattern)id);
			else strIds.add(String.valueOf(id));
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				Element q = (Element)n;
				if (q.hasAttribute("id")) {
					String id = q.getAttribute("id");
					if (id == null) continue;
					if (strIds.contains(id)) m.put(id, q);
					else if (!pIds.isEmpty()) for (Pattern p: pIds) 
						if (p.matcher(id).matches()) {
							m.put(id, q);
							break;
						}
				}
			}
		}
		strIds.clear();
		pIds.clear();
		return m;
	}
	
	private final Document document;
}
