package org.ipsilon.ipmems;

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

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.util.IpmemsObservablePropertized;

/**
 * IPMEMS abstract service.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsAbstractService extends	
		IpmemsObservablePropertized implements IpmemsService {
	/**
	 * Default constructor.
	 */
	public IpmemsAbstractService() {
	}
	
	/**
	 * Constructs the parametrized service.
	 * @param props Service properties.
	 */
	public IpmemsAbstractService(Map props) {
		super(props);
	}
	
	@Override
	public String getVar() {
		return getName();
	}

	@Override
	public void init(Object... args) {
		for (int i = 0; i < args.length; i += 2) if (i < args.length - 1)
			put(String.valueOf(args[i]), args[i + 1]);
		String p = getName() + ".";
		int pl = p.length();
		for (Map.Entry<String,Object> e: Ipmems.getMap().entrySet())
			if (e.getKey().startsWith(p)) {
				String key = e.getKey().substring(pl);
				if (!containsKey(key)) put(key, e.getValue());
			}
	}

	@Override
	public String getVersion() {
		return getVersion(this);
	}

	@Override
	public void restart() {
		stop();
		init();
		start();
	}
	
	/**
	 * Get the service version.
	 * @param s Service.
	 * @return Service version.
	 */
	public static String getVersion(IpmemsService s) {
		InputStream i = s.getClass().getResourceAsStream("version.properties");
		if (i != null) try {
			Properties p = new Properties();
			p.load(i);
			int ver = Integer.parseInt(p.getProperty("version", "1000000"));
			StringBuilder sb = new StringBuilder();
			int v0 = ver / 1000000;
			int v1 = (ver - v0 * 1000000) / 1000;
			int v2 = ver - v0 * 1000000 - v1 * 1000;
			sb.append(v0);
			sb.append('.');
			sb.append(v1);
			sb.append('.');
			sb.append(v2);
			return sb.toString();
		} catch (Exception x) {
			IpmemsLoggers.warning(s.getLogName(), "Get version error", x);
		} finally {
			try {i.close();} catch (Exception x) {}
		}
		return "1.0.0";		
	}
}
