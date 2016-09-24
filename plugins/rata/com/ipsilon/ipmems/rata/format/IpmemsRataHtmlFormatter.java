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

package com.ipsilon.ipmems.rata.format;

import java.lang.reflect.Array;
import java.util.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS RATA default formatter.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataHtmlFormatter extends 
		IpmemsPropertized implements IpmemsRataFormatter {
	@Override
	public String format(Object o) {
		if (o instanceof List) return format((List)o);
		else if (o instanceof Collection) return format((Collection)o);
		else if (o instanceof Map) return format((Map)o);
		else if (o != null && o.getClass().isArray()) return arrayToString(o);
		else {
			StringBuilder sb = new StringBuilder(String.valueOf(o));
			int i = 0;
			while (i < sb.length()) switch (sb.charAt(i)) {
				case '<':
					sb.replace(i, i + 1, "&lt;");
					i += 4;
					break;
				case '>':
					sb.replace(i, i + 1, "&gt;");
					i += 4;
					break;
				case '\r':
					sb.replace(i, i + 1, "");
					break;
				case '\n':
					sb.replace(i, i + 1, "<br/>");
					i += 5;
					break;
				case '\t':
					sb.replace(i, i + 1, "&nbsp;");
					i += 6;
					break;
				default:
					i++;
					break;
			}
			sb.insert(0, "<pre>");
			sb.append("</pre>");
			return sb.toString();
		}
	}

	@Override
	public String getName() {
		return IpmemsIntl.string("HTML formatter");
	}

	@Override
	public String getMime() {
		return "text/html";
	}
		
	private String arrayToString(Object o) {
		int n = Array.getLength(o);
		ArrayList<Object> l = new ArrayList<Object>(n);
		for (int i = 0; i < n; i++) l.add(Array.get(o, i));
		return format(l);
	}
	
	private String format(Map m) {
		StringBuilder sb = new StringBuilder("<table border='1'>");
		for (Object es: m.entrySet()) {
			Map.Entry e = (Map.Entry)es;
			sb.append("<tr><td><b>");
			sb.append(format(e.getKey()));
			sb.append("</b></td><td>");
			sb.append(format(e.getValue()));
			sb.append("</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
	
	private String format(List l) {
		if (!l.isEmpty() && 
				(l.size() == 1 && l.get(0) instanceof Map ||
				 (l.get(0) instanceof Map &&
				  l.get(1) instanceof Map && 
				  ((Map)l.get(0)).keySet().equals(((Map)l.get(1)).keySet())))) {
			StringBuilder sb = new StringBuilder("<table border='1'>");
			sb.append("<tr>");
			Set h = ((Map)l.get(0)).keySet();
			for (Object o: h) {
				sb.append("<th>");
				sb.append(o);
				sb.append("</th>");
			}
			sb.append("</tr>");
			for (Object o: l) {
				sb.append("<tr>");
				for (Object k: h) {
					sb.append("<td>");
					sb.append(format(((Map)o).get(k)));
					sb.append("</td>");
				}
				sb.append("</tr>");
			}
			sb.append("</table>");
			return sb.toString();
		} else return format((Collection)l);
	}
	
	private String format(Collection c) {
		StringBuilder sb = new StringBuilder("<table border='1'>");
		for (Object o: c) {
			sb.append("<tr><td>");
			sb.append(format(o));
			sb.append("</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
}
