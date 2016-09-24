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

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.util.IpmemsCollections;

/**
 * Abstract data store.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsDbSqlAbstractGate extends 
		IpmemsDbAbstractGate<Connection> implements IpmemsDbSqlGate {
	@Override
	public void close() {
		synchronized(cc) {
			for (PreparedStatement s: cc.values())
				try {s.close();} catch (Exception x) {}
			cc.clear();
		}
		super.close();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		synchronized(cc) {
			PreparedStatement s = cc.get(sql);
			if (s != null) return s; else {
				cc.put(sql, s = getGateObject().prepareStatement(sql));
				return s;
			}
		}
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] cn)
			throws SQLException {
		synchronized(cc) {
			PreparedStatement s = cc.get(sql);
			if (s != null) return s; else {
				cc.put(sql, s = getGateObject().prepareStatement(sql, cn));
				return s;
			}
		}
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int a)
			throws SQLException {
		synchronized(cc) {
			PreparedStatement s = cc.get(sql);
			if (s != null) return s; else {
				cc.put(sql, s = getGateObject().prepareStatement(sql, a));
				return s;
			}
		}
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] ci)
			throws SQLException {
		synchronized(cc) {
			PreparedStatement s = cc.get(sql);
			if (s != null) return s; else {
				cc.put(sql, s = getGateObject().prepareStatement(sql, ci));
				return s;
			}
		}
	}

	@Override
	public void set(PreparedStatement s, Object... l) throws Exception {
		ParameterMetaData md = s.getParameterMetaData();
		for (int i = 0; i < l.length; i++) switch (md.getParameterType(i + 1)) {
			case Types.TIME:
			case Types.TIMESTAMP:
			case Types.DATE:
				s.setObject(i + 1, getDate(l[i]));
				break;
			case Types.BIGINT:
				s.setObject(i + 1, getBigint(l[i]));
				break;
			case Types.DOUBLE:
			case Types.REAL:
				s.setObject(i + 1, getDouble(l[i]));
				break;
			case Types.BOOLEAN:
				s.setObject(i + 1, getBool(l[i]));
				break;
			case Types.ARRAY: {
				String type = md.getParameterTypeName(i + 1).toLowerCase();
				if (type.startsWith("bigint"))
					s.setObject(i + 1,
							getBigints(IpmemsCollections.array(l[i])));
				else if (type.startsWith("varchar"))
					s.setObject(i + 1,
							getStrings(IpmemsCollections.array(l[i])));
				else if (type.startsWith("double") || type.startsWith("real"))
					s.setObject(i + 1, getDbls(IpmemsCollections.array(l[i])));
				else if (type.startsWith("boolean"))
					s.setObject(i + 1, getBools(IpmemsCollections.array(l[i])));
				else s.setObject(i, l[i]);
				break;
			}
			default:
				s.setObject(i + 1, l[i]);
				break;
		}
	}

	@Override
	public Object get(Object o) throws Exception {
		if (o instanceof Array) {
			try {
				Object[] a = IpmemsCollections.array(((Array)o).getArray());
				ArrayList<Object> r = new ArrayList<Object>();
				for (Object w: a) r.add(get(w));
				return r;
			} catch (Exception x) {
				throw new IllegalStateException(x);
			} finally {
				try {((Array)o).free();} catch (Exception x) {}
			}
		} else if (o instanceof Clob) {
			try {
				String r = ((Clob)o).getSubString(1L, (int)((Clob)o).length());
				return r;
			} catch (Exception x) {
				throw new IllegalStateException(x);
			} finally {
				try {((Clob)o).free();} catch (Exception x) {}
			}
		} else if (o instanceof Blob) {
			try {
				byte[] r = ((Blob)o).getBytes(1L, (int)((Blob)o).length());
				return r;
			} catch (Exception x) {
				throw new IllegalStateException(x);
			} finally {
				try {((Blob)o).free();} catch (Exception x) {}
			}
		} else return o;
	}
	
	/**
	 * Get the date from a date object.
	 * @param o Date object.
	 * @return Date.
	 * @throws ParseException Parse exception.
	 */
	public static java.util.Date getDate(Object o) throws ParseException {
		if (o instanceof java.util.Date) return (java.util.Date)o;
		else if (o instanceof Calendar) return ((Calendar)o).getTime();
		else if (o instanceof Number) 
			return new java.util.Date(((Number)o).longValue());
		else if (o == null) return null;
		else return IpmemsIntl.parseIso(String.valueOf(o));
	}
	
	/**
	 * Get the big integer.
	 * @param o Bigint object.
	 * @return Big integer (long or big integer).
	 * @throws Exception An exception.
	 */
	public static Long getBigint(Object o) throws Exception {
		if (o == null) return null;
		else if (o instanceof Number) return ((Number)o).longValue();
		else if (o instanceof InetAddress) {
			byte[] adr = ((InetAddress)o).getAddress();
			ByteBuffer bb = ByteBuffer.allocate(8);
			bb.position(8 - adr.length);
			bb.put(adr);
			return bb.getLong(0);
		} else return IpmemsDbAddress.decode(String.valueOf(o));
	}
	
	/**
	 * Get the double.
	 * @param o Double object.
	 * @return Double.
	 * @throws Exception An exception.
	 */
	public static Double getDouble(Object o) throws Exception {
		if (o == null) return null;
		else if (o instanceof Number) return ((Number)o).doubleValue();
		else return Double.parseDouble(String.valueOf(o));
	}
	
	/**
	 * Get the boolean.
	 * @param o Boolean object.
	 * @return Boolean value.
	 * @throws Exception An exception.
	 */
	public static Boolean getBool(Object o) throws Exception {
		if (o == null) return null;
		else if (o instanceof Boolean) return (Boolean)o;
		else return Boolean.parseBoolean(String.valueOf(o));
	}
	
	/**
	 * Get the big integer list.
	 * @param l Source list.
	 * @return Big integer list.
	 * @throws Exception An exception.
	 */
	public static Long[] getBigints(Object[] l) throws Exception {
		Long[] r = new Long[l.length];
		for (int i = 0; i < r.length; i++) r[i] = getBigint(l[i]);
		return r;
	}
	
	/**
	 * Get the strings.
	 * @param l Source list.
	 * @return String array.
	 * @throws Exception An exception.
	 */
	public static String[] getStrings(Object[] l) throws Exception {
		String[] r = new String[l.length];
		for (int i = 0; i < r.length; i++)
			r[i] = l[i] == null ? null : l[i].toString();
		return r;
	}
	
	/**
	 * Get the strings.
	 * @param l Source list.
	 * @return String array.
	 * @throws Exception An exception.
	 */
	public static Double[] getDbls(Object[] l) throws Exception {
		Double[] r = new Double[l.length];
		for (int i = 0; i < r.length; i++)
			r[i] = l[i] instanceof Number ? ((Number)l[i]).doubleValue() : null;
		return r;
	}
	
	/**
	 * Get the boolean array.
	 * @param l Boolean object array.
	 * @return Boolean array.
	 * @throws Exception An exception.
	 */
	public static Boolean[] getBools(Object[] l) throws Exception {
		Boolean[] r = new Boolean[l.length];
		for (int i = 0; i < r.length; i++)
			r[i] = l[i] instanceof Boolean ? (Boolean)l[i] :
					Boolean.parseBoolean(String.valueOf(l[i]));
		return r;
	}
	
	/**
	 * Executes SQL update.
	 * @param s SQL statement.
	 * @param m Parameters map.
	 * @param l Data.
	 * @return Update count.
	 * @throws Exception An exception.
	 */
	public int upi(String s, Map m, Object[] ... l) throws Exception {
		int f = IpmemsCollections.value(Integer.class, m, "size", 0);
		int x = IpmemsCollections.value(Integer.class, m, "max", 0);
		int t = IpmemsCollections.value(Integer.class, m, "to", 0);
		int u = 0;
		PreparedStatement ps = prepareStatement(s);
		synchronized(ps) {
			ps.setFetchSize(f);
			ps.setMaxRows(x);
			ps.setQueryTimeout(t);
			for (Object[] p: l) {
				if (p == null) continue;
				set(ps, p);
				try {
					u += ps.executeUpdate();
				} catch (SQLIntegrityConstraintViolationException y) {
				}
			}
			return u;
		}
	}
	
	/**
	 * Executes SQL update.
	 * @param s SQL statement.
	 * @param m Parameters map.
	 * @param l Data.
	 * @return Update count.
	 * @throws Exception An exception.
	 */
	public int upd(String s, Map m, Object[] ... l) throws Exception {
		int f = IpmemsCollections.value(Integer.class, m, "size", 0);
		int x = IpmemsCollections.value(Integer.class, m, "max", 0);
		int t = IpmemsCollections.value(Integer.class, m, "to", 0);
		int u = 0;
		PreparedStatement ps = prepareStatement(s);
		synchronized(ps) {
			ps.setFetchSize(f);
			ps.setMaxRows(x);
			ps.setQueryTimeout(t);
			for (Object[] p: l) {
				if (p == null) continue;
				set(ps, p);
				u += ps.executeUpdate();
			}
			return u;
		}
	}
		
	/**
	 * Executes the query.
	 * @param s SQL statement.
	 * @param m Parameters map.
	 * @param l Data.
	 * @return Query result.
	 * @throws Exception An exception.
	 */
	public List<Map<String,Object>> rs(
			String s, Map m, Object ... l) throws Exception {
		int f = IpmemsCollections.value(Integer.class, m, "size", 0);
		int x = IpmemsCollections.value(Integer.class, m, "max", 0);
		int t = IpmemsCollections.value(Integer.class, m, "to", 0);
		ResultSet rs = null;
		List<Map<String,Object>> r = new ArrayList<Map<String,Object>>();
		PreparedStatement ps = prepareStatement(s);
		synchronized(ps) {
			ps.setFetchSize(f);
			ps.setMaxRows(x);
			ps.setQueryTimeout(t);
			try {
				set(ps, l);
				rs = ps.executeQuery();
				ResultSetMetaData d = rs.getMetaData();
				while (rs.next()) {
					Map<String,Object> z = new LinkedHashMap<String,Object>();
					for (int i = 1; i <= d.getColumnCount(); i++)
						z.put(d.getColumnLabel(i).toLowerCase(),
								get(rs.getObject(i)));
					r.add(z);
				}
				return r;
			} finally {
				if (rs != null) rs.close();
			}
		}
	}
		
	/**
	 * Executes a statement.
	 * @param s SQL statement.
	 * @param m Parameter map.
	 * @param l Data.
	 * @return Update count and rows list.
	 * @throws Exception An exception.
	 */
	public Object[] ex(String s, Map m, Object[] ... l) throws Exception {
		int f = IpmemsCollections.value(Integer.class, m, "size", 0);
		int x = IpmemsCollections.value(Integer.class, m, "max", 0);
		int t = IpmemsCollections.value(Integer.class, m, "to", 0);
		int u = 0;
		List<Object> r = new ArrayList<Object>();
		PreparedStatement ps = prepareStatement(s);
		synchronized(ps) {
			ps.setFetchSize(f);
			ps.setMaxRows(x);
			ps.setQueryTimeout(t);
			for (Object[] p: l) {
				if (p == null) continue;
				set(ps, p);
				if (ps.execute()) {
					List<Map<String,Object>> v = 
							new ArrayList<Map<String,Object>>();
					ResultSet rs = null;
					try {
						rs = ps.getResultSet();
						ResultSetMetaData md = rs.getMetaData();
						while (rs.next()) {
							Map<String,Object> z = 
									new LinkedHashMap<String,Object>();
							for (int i = 1; i <= md.getColumnCount(); i++)
								z.put(md.getColumnLabel(i).toLowerCase(),
										get(rs.getObject(i)));
							v.add(z);
						}
					} finally {
						if (rs != null) rs.close();
					}
					r.add(v);
				} else {
					if (ps.getUpdateCount() > 0) u += ps.getUpdateCount();
				}
			}
			return new Object[] {u, r};
		}
	}
		
	private final Map<String,PreparedStatement> cc = 
			new HashMap<String,PreparedStatement>();
}
