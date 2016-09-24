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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.ipsilon.ipmems.db.IpmemsDbSqlAbstractGate;

/**
 * IPMEMS HSQLDB logging class.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsHsqlLogging extends IpmemsDbSqlAbstractGate {
	@Override
	public Map<String,Object> gf(String k, Map a) {
		if ("put".equals(k)) return put(a);
		else if ("get".equals(k)) return get(a);
		else if ("last".equals(k)) return last(a);
		else return Collections.emptyMap();
	}
	
	private Map<String,Object> put(Map map) {
		Date ts = new Date();
		try {
			List l = (List)map.get("rows");
			Object[][] d = new Object[l.size()][];
			for (int i = 0; i < d.length; i++) {
				Map m = (Map)l.get(i);
				Object t = m.containsKey("t") ? m.get("t") : ts;
				Object aid = m.containsKey("aid") ? m.get("aid") : 0;
				Object msg = m.get("msg");
				Object a = m.get("args");
				d[i] = new Object[] {aid, t, msg, a};
			}
			return umap("updateCount", 
					upd("INSERT INTO ipLog VALUES (?,?,?,?)", map, d));
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> get(Map map) {
		try {
			Object t = map.containsKey("t") ? map.get("t") : new Date(0L);
			if (map.containsKey("msg"))	return umap("rows",	rs(
				"SELECT * FROM ipLog WHERE msg = ? AND t > ? ORDER BY t ASC",
					map, map.get("msg"), t));
			else return umap("rows", rs(
				"SELECT * FROM ipLog WHERE aid = ? AND t > ? ORDER BY t ASC",
					map, map.get("aid"), t));
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> last(Map map) {
		String sql = "SELECT * FROM ipLog WHERE %s = ? AND " +
				"t > NOW() - ? %s ORDER BY t DESC";
		try {
			Object t = map.containsKey("dt") ? map.get("dt") : 60;
			Object unit = map.containsKey("unit") ? map.get("unit") : "SECOND";
			if (map.containsKey("msg")) {
				sql = String.format(sql, "msg", unit);
				return umap("rows", rs(sql, map, map.get("msg"), t));
			} else {
				sql = String.format(sql, "aid", unit);
				return umap("rows", rs(sql, map, map.get("aid"), t));
			}
		} catch (Exception x) {
			return umap("error", x);
		}
	}	
}
