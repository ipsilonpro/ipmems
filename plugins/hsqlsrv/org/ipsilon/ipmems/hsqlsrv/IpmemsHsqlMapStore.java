package org.ipsilon.ipmems.hsqlsrv;

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

import java.util.Collections;
import java.util.Map;
import org.ipsilon.ipmems.db.IpmemsDbSqlAbstractGate;

/**
 * IPMEMS HSQLDB map store.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsHsqlMapStore extends IpmemsDbSqlAbstractGate {
	@Override
	public Map<String,Object> gf(String k, Map a) {
		if ("store".equals(k)) return store(a);
		else if ("delete".equals(k)) return delete(a);
		else if ("get".equals(k)) return get(a);
		else if ("children".equals(k)) return children(a);
		else return Collections.emptyMap();
	}
	
	private Map<String,Object> store(Map arg) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private Map<String,Object> delete(Map arg) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private Map<String,Object> get(Map arg) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private Map<String,Object> children(Map arg) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
