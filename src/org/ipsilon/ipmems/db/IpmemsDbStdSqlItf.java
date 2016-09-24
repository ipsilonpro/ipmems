package org.ipsilon.ipmems.db;

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
import java.util.List;
import java.util.Map;

/**
 * IPMEMS DB standard DB interface.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDbStdSqlItf extends IpmemsDbSqlAbstractGate {
	@Override
	public Map<String,Object> gf(String k, final Map a) {
		if ("rows".equals(k)) return rows(a);
		else if ("update".equals(k)) return update(a);
		else if ("execute".equals(k)) return execute(a);
		else return Collections.emptyMap();
	}
	
	private Map<String,Object> rows(Map arg) {
		try {
			String sql = arg.get("sql").toString();
			Object ps = arg.get("params");
			Object[] p = ps instanceof List ?
					((List)ps).toArray() : new Object[0];
			return umap("rows", rs(sql, arg, p));
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> update(Map arg) {
		try {
			String sql = String.valueOf(arg.get("sql"));
			Object[][] rows;
			Object ors = arg.get("params");
			if (ors instanceof List) {
				rows = new Object[((List)ors).size()][];
				for (int i = 0; i < rows.length; i++)
					rows[i] = ((List)((List)ors).get(i)).toArray();
			} else rows = new Object[0][0];
			return umap("updateCount", upd(sql, arg, rows));
		} catch (Exception x) {
			return umap("error", x);
		}
	}
	
	private Map<String,Object> execute(Map arg) {
		try {
			String sql = String.valueOf(arg.get("sql"));
			Object[][] prs;
			Object ors = arg.get("params");
			if (ors instanceof List) {
				prs = new Object[((List)ors).size()][];
				for (int i = 0; i < prs.length; i++)
					prs[i] = ((List)((List)ors).get(i)).toArray();
			} else prs = new Object[0][0];
			Object[] v = ex(sql, arg, prs);
			return umap("updateCount", v[0], "rowsList", v[1]);
		} catch (Exception x) {
			return umap("error", x);
		}
	}
}
