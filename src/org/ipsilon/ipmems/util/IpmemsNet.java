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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import org.ipsilon.ipmems.io.IpmemsIOLib;

/**
 * IPMEMS network utilities.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsNet {
	/**
	 * Get the URL string.
	 * @param path URL path.
	 * @param props URL parameters.
	 * @return Formatted url.
	 * @throws Exception An Exception.
	 */
	public static String mkUrl(String path, Map props) throws Exception {
		StringBuilder sb = new StringBuilder(path);
		sb.append('?');
		Object[] ks = props.keySet().toArray();
		for (int i = 0; i < ks.length; i++) {
			Object val = props.get(ks[i]);
			sb.append(URLEncoder.encode(String.valueOf(ks[i]), "UTF-8"));
			sb.append('=');
			sb.append(URLEncoder.encode(String.valueOf(val), "UTF-8"));
			if (i < ks.length - 1) sb.append('&');
		}
		return sb.toString();
	}
	
	/**
	 * Parses the URI parameters.
	 * @param u An URI.
	 * @return URI parameters.
	 * @throws Exception An exception.
	 */
	public static Properties uriProps(URI u) throws Exception {
		Properties p = new Properties();
		String query = u.getRawQuery();
		if (query == null) return p;
		int o = 0;
		for (int i = 0; i <= query.length(); i++) 
			if (i == query.length() || query.charAt(i) == '&') {
				String kv = query.substring(o, i);
				int j = kv.indexOf('=');
				p.setProperty(
						URLDecoder.decode(kv.substring(0, j), "UTF-8"),
						URLDecoder.decode(kv.substring(j + 1), "UTF-8"));
				o = i + 1;
			}
		return p;
	}
	
	/**
	 * Get the query props.
	 * @param is Input stream.
	 * @return Query properties.
	 * @throws IOException An I/O exception.
	 */
	public static Properties queryProps(InputStream is) throws IOException {
		Properties p = new Properties();
		while (true) {
			String s = IpmemsIOLib.next(is, '\n');
			if (s == null) break;
			s = s.trim();
			if (s.isEmpty()) break;
			int i = s.indexOf(':');
			if (i < 0) continue;
			p.setProperty(s.substring(0, i).trim(), s.substring(i + 1).trim());
		}
		return p;
	}
}
