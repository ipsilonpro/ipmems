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

import java.sql.Timestamp;
import java.util.*;
import org.ipsilon.ipmems.db.IpmemsDbSqlAbstractGate;
import org.ipsilon.ipmems.util.IpmemsCollections;

/**
 * IPMEMS HSQLDB data store.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsHsqlDataStore extends IpmemsDbSqlAbstractGate {
	@Override
	public Map<String,Object> gf(String k, Map a) {
		if ("store".equals(k)) return store(a);
		else if ("ids".equals(k)) return ids(a);
		else if ("values".equals(k)) return values(a);
		else if ("after".equals(k)) return after(a);
		else if ("before".equals(k)) return before(a);
		else if ("between".equals(k)) return between(a);
		else if ("count".equals(k)) return count(a);
		else if ("delete".equals(k)) return delete(a);
		else if ("update".equals(k)) return update(a);
		else if ("first".equals(k)) return first(a);
		else if ("last".equals(k)) return last(a);
		else return Collections.emptyMap();
	}
	
	private Map<String,Object> store(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", true);
		boolean store = get(Boolean.class, arg, "store", true);
		boolean stc = get(Boolean.class, arg, "check", false);
		String ds = "MERGE INTO %1$s USING " +
				"(VALUES(CAST(? AS BIGINT),CAST(? AS TIMESTAMP)," +
				"CAST(? AS %2$s))) AS v(id,t,val) " +
				"ON %1$s.id = v.id AND %1$s.t < v.t " +
				"WHEN MATCHED THEN UPDATE SET t = v.t, val = v.val " +
				"WHEN NOT MATCHED THEN INSERT VALUES v.id, v.t, v.val";
		String ss = "MERGE INTO %1$s USING " +
				"(VALUES(CAST(? AS BIGINT),CAST(? AS TIMESTAMP)," +
				"CAST(? AS %2$s))) AS v(id,t,val) " +
				"ON %1$s.id = v.id AND %1$s.t = v.t " +
				"WHEN NOT MATCHED THEN INSERT VALUES v.id, v.t, v.val";
		String is = "INSERT INTO %s VALUES (?,?,?)";
		if (dyn) ds = String.format(ds, st.tableName(sp, dyn), st.getType());
		if (store) {
			if (!stc) is = String.format(is, st.tableName(sp, false));
			else ss = String.format(ss, st.tableName(sp, false), st.getType());
		}
		List l = (List)arg.get("rows");
		Object[][] d = new Object[l.size()][];
		int u = 0;
		try {
			for (int i = 0; i < d.length; i++) {
				Object o = l.get(i);
				if (o instanceof List) {
					d[i] = ((List)o).toArray();
				} else if (o instanceof Map) {
					Map v = (Map)o;
					d[i] = new Object[] {v.get("id"), v.get("t"), v.get("val")};
				} else continue;
			}
			if (dyn) u += upi(ds, arg, d);
			if (store) u += upd(stc ? ss : is, arg, d);
			return umap("updateCount", u);
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> ids(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", true);
		try {
			ArrayList<Long> l = new ArrayList<Long>();
			for (Map<String,Object> z: rs("SELECT DISTINCT id FROM "
					+ st.tableName(sp, dyn), arg))
				l.add((Long)z.get("id"));
			return umap("ids", l);
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> values(Map a) {
		StoreT st = get(StoreT.class, a, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, a, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, a, "dyn", false);
		String q = "SELECT * FROM %s WHERE id %s%s ORDER BY t %s";
		try {
			if (a.containsKey("ids")) {
				if (a.containsKey("t")) {
					q = String.format(q, st.tableName(sp, dyn),
							"IN (UNNEST(?))", " AND t = ?",
							a.containsKey("ord") ? a.get("ord") : "ASC");
					return umap("rows", rs(q, a, a.get("ids"), a.get("t")));
				} else {
					q = String.format(q, st.tableName(sp, dyn),
							"IN (UNNEST(?))", "",
							a.containsKey("ord") ? a.get("ord") : "ASC");
					return umap("rows", rs(q, a, a.get("ids")));
				}
			} else if (a.containsKey("id")) {
				if (a.containsKey("t")) {
					q = String.format(q,
							st.tableName(sp, dyn), "= ?", "AND t = ?",
							a.containsKey("ord") ? a.get("ord") : "ASC");
					return umap("rows",	rs(q, a, a.get("id"), a.get("t")));
				} else {
					q = String.format(q,
							st.tableName(sp, dyn), "= ?", "",
							a.containsKey("ord") ? a.get("ord") : "ASC");
					return umap("rows", rs(q, a, a.get("id")));
				}
			} else {
				q = "SELECT * FROM %s ORDER BY t %s";
				q = String.format(q, st.tableName(sp, dyn),
						a.containsKey("ord") ? a.get("ord") : "ASC");
				return umap("rows", rs(q, a));
			}
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> last(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", false);
		String q = "SELECT id, MAX(t) AS t FROM %s WHERE id %s GROUP BY id";
		try {
			Map<Long,Timestamp> res = new LinkedHashMap<Long,Timestamp>();
			if (arg.containsKey("ids")) {
				q = String.format(q, st.tableName(sp, dyn), "IN (UNNEST(?))");
				for (Map<String,Object> z: rs(q, arg, arg.get("ids")))
					res.put((Long)z.get("id"), (Timestamp)z.get("t"));
			} else if (arg.containsKey("id")) {
				q = String.format(q, st.tableName(sp, dyn), "= ?");
				for (Map<String,Object> z: rs(q, arg, arg.get("id")))
					res.put((Long)z.get("id"), (Timestamp)z.get("t"));
			} else {
				q = "SELECT id, t FROM %s";
				q = String.format(q, st.tableName(sp, true));
				for (Map<String,Object> z: rs(q, arg))
					res.put((Long)z.get("id"), (Timestamp)z.get("t"));
			}
			return umap("result", res);
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String,Object> first(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", false);
		String q = "SELECT id, MIN(t) AS t FROM %s WHERE id %s GROUP BY id";
		try {
			Map<Long,Timestamp> res = new LinkedHashMap<Long,Timestamp>();
			if (arg.containsKey("ids")) {
				q = String.format(
						q, st.tableName(sp, dyn), "IN (UNNEST(?))");
				for (Map<String,Object> z: rs(q, arg, arg.get("ids")))
					res.put((Long)z.get("id"), (Timestamp)z.get("t"));
			} else if (arg.containsKey("id")) {
				q = String.format(q, st.tableName(sp, dyn), "= ?");
				for (Map<String,Object> z: rs(q, arg, arg.get("id")))
					res.put((Long)z.get("id"), (Timestamp)z.get("t"));
			} else {
				Map n = new LinkedHashMap(arg);
				n.put("ids", ids(arg).get("ids"));
				n.put("dyn", true);
				return first(n);
			}
			return umap("result", res);
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> count(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", false);
		try {
			String q = "SELECT COUNT(*) AS c FROM %s WHERE id%s%s";
			if (arg.containsKey("ids")) {
				if (arg.containsKey("t")) {
					q = String.format(q, st.tableName(sp, dyn),
							" IN (UNNEST(?))", " AND t = ?");
					return umap("count", rs(q, arg,
							arg.get("ids"), arg.get("t")).get(0).get("c"));
				} else {
					q = String.format(q,
							st.tableName(sp, dyn), " IN (UNNEST(?))", "");
					return umap("count", rs(q, arg,
							arg.get("ids")).get(0).get("c"));
				}
			} else if (arg.containsKey("id")) {
				if (arg.containsKey("t")) {
					q = String.format(q, st.tableName(sp, dyn),
							" = ?", " AND t = ?");
					return umap("count", rs(q, arg,
							arg.get("id"), arg.get("t")).get(0).get("c"));
				} else {
					q = String.format(q, st.tableName(sp, dyn), " = ?", "");
					return umap("count", rs(q, arg,
							arg.get("id")).get(0).get("c"));
				}
			} else {
				q = "SELECT COUNT(*) AS c FROM %s";
				q = String.format(q, st.tableName(sp, dyn));
				return umap("count", rs(q, arg).get(0).get("c"));
			}
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> after(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", false);
		boolean inc = get(Boolean.class, arg, "inc", false);
		try {
			String q = "SELECT * FROM %s WHERE id %s AND t %s ? ORDER BY t %s";
			q = String.format(q,
					st.tableName(sp, dyn),
					arg.containsKey("ids") ? "IN (UNNEST(?))" : "= ?",
					inc ? ">=" : ">",
					arg.containsKey("ord") ? arg.get("ord") : "ASC");
			Object arg1 = arg.containsKey("ids") ?
					arg.get("ids") : arg.get("id");
			return umap("rows", rs(q, arg, arg1, arg.get("t")));
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> before(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", false);
		boolean inc = get(Boolean.class, arg, "inc", false);
		try {
			String q = "SELECT * FROM %s WHERE id %s AND t %s ? ORDER BY t %s";
			q = String.format(q,
					st.tableName(sp, dyn),
					arg.containsKey("ids") ? "IN (UNNEST(?))" : "= ?",
					inc ? "<=" : "<",
					arg.containsKey("ord") ? arg.get("ord") : "ASC");
			Object arg1 = arg.containsKey("ids") ?
					arg.get("ids") : arg.get("id");
			return umap("rows", rs(q, arg, arg1, arg.get("t")));
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> between(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", false);
		boolean linc = get(Boolean.class, arg, "linc", true);
		boolean rinc = get(Boolean.class, arg, "rinc", true);
		try {
			String q = "SELECT * FROM %s WHERE id %s AND "
					+ "t %s ? AND t %s ? ORDER BY t %s";
			q = String.format(q,
					st.tableName(sp, dyn),
					arg.containsKey("ids") ? "IN (UNNEST(?))" : "= ?",
					linc ? ">=" : ">",
					rinc ? "<=" : "<",
					arg.containsKey("ord") ? arg.get("ord") : "ASC");
			Object a = arg.containsKey("ids") ?
					arg.get("ids") : arg.get("id");
			return umap("rows", rs(q, arg, a, arg.get("t1"), arg.get("t2")));
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> update(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", true);
		String sql = "UPDATE %s SET val = ? WHERE id = ? AND t = ?";
		String sqf = "UPDATE %s SET (t, val) = (?, ?) WHERE id = ?";
		String sqd = "UPDATE %s SET (t, val) = (?, ?) WHERE id = ? AND t < ?";
		try {
			int u = 0;
			for (Object o: IpmemsCollections.value(
					List.class, arg, "rows", Collections.EMPTY_LIST))
				if (o instanceof Map) {
					Map vm = (Map)o;
					if (vm.containsKey("id") && vm.containsKey("t")) {
						String s = String.format(sql, st.tableName(sp, false));
						u += upd(s, arg, new Object[] {
							vm.get("val"), vm.get("id"), vm.get("t")});
						if (dyn) {
							s = String.format(sql, st.tableName(sp, dyn));
							u += upd(s, arg, new Object[] {
								vm.get("val"), vm.get("id"), vm.get("t")});
						}
					} else if (vm.containsKey("id") && vm.containsKey("nt")) {
						String s = String.format(sqf, st.tableName(sp, false));
						u += upd(s, arg, new Object[] {
							vm.get("nt"), vm.get("val"), vm.get("id")});
						if (dyn) {
							s = String.format(sqd, st.tableName(sp, dyn));
							u += upd(s, arg, new Object[] {vm.get("nt"),
								vm.get("val"), vm.get("id"), vm.get("nt")});
						}
					}
				} else if (o instanceof List) {
					List l = (List)o;
					if (l.size() != 3) continue;
					String s = String.format(sql, st.tableName(sp, false));
					u += upd(s,arg, new Object[]{l.get(2), l.get(0), l.get(1)});
					if (dyn) {
						s = String.format(sql, st.tableName(sp, dyn));
						u += upd(s, arg, new Object[] {
							l.get(2), l.get(0), l.get(1)});
					}
				}
			return umap("updateCount", u);
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> delete(Map arg) {
		StoreT st = get(StoreT.class, arg, "table", StoreT.NUM);
		StoreP sp = get(StoreP.class, arg, "prefix", StoreP.CURRENT);
		boolean dyn = get(Boolean.class, arg, "dyn", true);
		String d1 = "DELETE FROM %s WHERE id = ?";
		String d2 = "DELETE FROM %s WHERE id = ? AND t = ?";
		try {
			int u = 0;
			for (Object o: IpmemsCollections.value(
					List.class, arg, "rows", Collections.EMPTY_LIST))
				if (o instanceof Map) {
					Map vm = (Map)o;
					if (vm.containsKey("id") && vm.containsKey("t")) {
						String s = String.format(d2, st.tableName(sp, false));
						u += upd(s, arg, new Object[] {
							vm.get("id"), vm.get("t")});
						if (dyn) {
							s = String.format(d2, st.tableName(sp, dyn));
							u += upd(s, arg, new Object[] {
								vm.get("id"), vm.get("t")});
						}
					} else if (vm.containsKey("id")) {
						String s = String.format(d1, st.tableName(sp, false));
						u += upd(s, arg, new Object[] {vm.get("id")});
						if (dyn) {
							s = String.format(d1, st.tableName(sp, dyn));
							u += upd(s, arg, new Object[] {vm.get("id")});
						}
					}
				} else if (o instanceof List) {
					List l = (List)o;
					String s;
					switch (l.size()) {
						case 1:
							s = String.format(d1, st.tableName(sp, false));
							u += upd(s, arg, new Object[] {l.get(0)});
							if (dyn) {
								s = String.format(d1, st.tableName(sp, dyn));
								u += upd(s, arg, new Object[] {l.get(0)});
							}
							break;
						case 2:
							s = String.format(d2, st.tableName(sp, false));
							u += upd(s, arg, new Object[] {l.get(0), l.get(1)});
							if (dyn) {
								s = String.format(d2, st.tableName(sp, dyn));
								u += upd(s,arg,new Object[]{l.get(0),l.get(1)});
							}
							break;
					}
				}
			return umap("updateCount", u);
		} catch (Exception x) {
			return umap("error", x);
		}
	}
			
	private static enum StoreT {
		NUM,
		STRING("String", "LONGVARCHAR"),
		BOOL("Bool", "BOOLEAN"),
		VNUM("DblNumArr", "DOUBLE ARRAY"),
		VSTRING("StringArr", "LONGVARCHAR ARRAY"),
		VBOOL("BoolArr", "BOOLEAN ARRAY");

		private StoreT() {
			this("DblNum", "DOUBLE");
		}
		
		private StoreT(String b, String t) {
			base = b;
			type = t;
		}

		public String tableName(StoreP p, boolean dyn) {
			switch (p) {
				case CURRENT:
					return "ip" + (dyn ? "Dyn" : "") + base + "Store";
				case REPORT:
					return "ipRep" + (dyn ? "Dyn" : "") + base + "Store";
				case TMP:
					return "ipTmp" + (dyn ? "Dyn" : "") + base + "Store";
				default:
					return "ip" + (dyn ? "Dyn" : "") + base + "Store";
			}
		}

		public String getType() {
			return type;
		}
		
		private String base;
		private String type;
	}
	
	private static enum StoreP {
		CURRENT, REPORT, TMP
	}
}
