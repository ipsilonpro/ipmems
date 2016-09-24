package org.ipsilon.ipmems.db.file;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import static java.util.Calendar.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import org.ipsilon.ipmems.json.IpmemsJsonUtil;

/**
 * IPMEMS file data store.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDbFileDataStore extends IpmemsDbFileGate {
	/**
	 * Writes a data to file.
	 * @param sd Subdirectory.
	 * @param ts Timestamp.
	 * @param val Value.
	 * @return Write flag.
	 * @throws Exception An I/O exception.
	 */
	public boolean write(File sd, long t, final Object v) throws Exception {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		c.setTimeInMillis(t);
		String path = c.get(YEAR) + "/" + c.get(MONTH) + "/" +
				c.get(DATE) + "/" + c.get(HOUR_OF_DAY) + "/" + c.get(MINUTE);
		final String f = Integer.toString(c.get(SECOND));
		final File td = new File(sd, path);
		final File file = new File(td, f);
		return cuf(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				if (file.exists()) return Boolean.FALSE; else {
					if (!td.exists() && !td.mkdirs())
						throw new FileNotFoundException(td.toString());
					IpmemsJsonUtil.make(v, file);
					return Boolean.TRUE;
				}
			}
		});
	}
	
	private File subDir(String p, String id) {
		return new File(getDir(), p.toLowerCase() + "/" + id.replace('/', '_'));
	}

	@Override
	public String getDirKey() {
		return "dataStore";
	}

	@Override
	public Map<String,Object> gf(String k, Map a) {
		if ("store".equals(k)) return store(a);
		else if ("ids".equals(k)) return ids(a);
		else if ("values".equals(k)) return values(a);
		else if ("last".equals(k)) return last(a);
		else if ("first".equals(k)) return first(a);
		else if ("count".equals(k)) return count(a);
		else if ("after".equals(k)) return after(a);
		else if ("before".equals(k)) return before(a);
		else if ("between".equals(k)) return between(a);
		else if ("update".equals(k)) return update(a);
		else if ("delete".equals(k)) return delete(a);
		else return Collections.emptyMap();
	}
	
	private Map<String,Object> store(Map arg) {
		String p = get(String.class, arg, "prefix", "current").toLowerCase();
		try {
			int u = 0;
			List l = (List)arg.get("rows");
			for (Object o: l) {
				String id;
				long t;
				Object val;
				if (o instanceof List) {
					List v = (List)o;
					id = String.valueOf(v.get(0));
					t = millis(v.get(1));
					val = v.get(2);
				} else if (o instanceof Map) {
					Map m = (Map)o;
					id = String.valueOf(m.get("id"));
					t = millis(m.get("t"));
					val = m.get("val");
				} else continue;
				if (write(subDir(p, id), t, val)) u++;
			}
			return umap("updateCount", u);
		} catch (Exception x) {
			return umap("error", x);
		}
	}

	private Map<String,Object> ids(Map arg) {
		String p = get(String.class, arg, "prefix", "current").toLowerCase();
		final File sd = new File(getDir(), p);
		return cuf("ids", new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				ArrayList<String> l = new ArrayList<String>();
				for (String d: sd.list()) l.add(d.replace('_', '/'));
				return l;
			}
		});
	}

	private Map<String,Object> values(Map arg) {
		boolean dyn = get(Boolean.class, arg, "dyn", false);
		return cuf("rows", dyn ? new DynValues(arg) : new StdValues(arg));
	}

	private Map<String,Object> last(Map arg) {
		return cuf("result", new Limits(arg, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return Integer.valueOf(o1.getName()).compareTo(
						Integer.valueOf(o2.getName()));
			}
		}));
	}

	private Map<String,Object> first(Map arg) {
		return cuf("result", new Limits(arg, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return Integer.valueOf(o2.getName()).compareTo(
						Integer.valueOf(o1.getName()));
			}
		}));
	}

	private Map<String,Object> count(Map arg) {
		return cuf("count", new Count(arg));
	}

	private Map<String,Object> after(Map arg) {
		return cuf("rows", new AB(arg, true));
	}

	private Map<String,Object> before(Map arg) {
		return cuf("rows", new AB(arg, false));
	}

	private Map<String,Object> between(Map arg) {
		return cuf("rows", new Between(arg));
	}

	private Map<String,Object> update(Map arg) {
		return cuf("updateCount", new Update(arg));
	}

	private Map<String,Object> delete(Map arg) {
		return cuf("updateCount", new Delete(arg));
	}
		
	private abstract class Values implements Callable<List<Map<String,Object>>>{
		public Values(Map arg) {
			a = arg;
		}

		@Override
		public List<Map<String,Object>> call() throws Exception {
			String p = get(String.class, a, "prefix", "current");
			if (a.containsKey("t")) {
				if (a.containsKey("ids")) return idsT(p, (List)a.get("ids"));
				else if (a.containsKey("id"))
					return idsT(p, Collections.singletonList(a.get("id")));
				else {
					File dir = new File(getDir(), p);
					return idsT(p, Arrays.asList(dir.list()));
				}
			} else {
				if (a.containsKey("ids")) return ids(p, (List)a.get("ids"));
				else if (a.containsKey("id"))
					return ids(p, Collections.singletonList(a.get("id")));
				else {
					File dir = new File(getDir(), p);
					return ids(p, Arrays.asList(dir.list()));
				}
			}
		}
		
		public List<Map<String,Object>> idsT(String p,List v) throws Exception {
			List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
			int max = get(Integer.class, a, "max", 0);
			long ts = millis(a.get("t"));
			c.setTimeInMillis(ts);
			c.set(Calendar.MILLISECOND, 0);
			for (Object oid: v) {
				if (max > 0 && l.size() >= max) break;
				String id = String.valueOf(oid).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				String path = c.get(YEAR) + "/" + c.get(MONTH) + "/" +
						c.get(DATE) + "/" + c.get(HOUR_OF_DAY) + "/" +
						c.get(MINUTE) + "/" + c.get(SECOND);
				File file = new File(sd, path);
				if (!file.exists()) continue;
				Object val = IpmemsJsonUtil.parse(file);
				Map<String,Object> m = new LinkedHashMap<String,Object>(4);
				m.put("id", id.replace('_', '/'));
				m.put("t", new Timestamp(c.getTimeInMillis()));
				m.put("val", val);
				l.add(m);
			}
			return l;
		}
		
		public abstract List<Map<String,Object>> ids(
				String p, List v) throws Exception;
		
		protected final Comparator<File> fc = new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return Integer.valueOf(o1.getName()).compareTo(
						Integer.valueOf(o2.getName()));
			}
		};
		protected final Comparator<File> rc = new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return Integer.valueOf(o2.getName()).compareTo(
						Integer.valueOf(o1.getName()));
			}
		};
		protected final Map a;
		protected final Calendar c = 
				Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	}
	
	private final class DynValues extends Values {
		public DynValues(Map arg) {
			super(arg);
		}
		
		@Override
		public final List<Map<String,Object>> ids(
				String p, List v) throws Exception {
			List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
			int max = get(Integer.class, a, "max", 0);
			for (Object oid: v) {
				String id = String.valueOf(oid).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				File[] fy = sd.listFiles();
				if (fy.length == 0) continue;
				File fymax = Collections.max(Arrays.asList(fy),	fc);
				File[] fm = fymax.listFiles();
				if (fm.length == 0) continue;
				File fmmax = Collections.max(Arrays.asList(fm), fc);
				File[] fd = fmmax.listFiles();
				if (fd.length == 0) continue;
				File fdmax = Collections.max(Arrays.asList(fd), fc);
				File[] fh = fdmax.listFiles();
				if (fh.length == 0) continue;
				File fhmax = Collections.max(Arrays.asList(fh), fc);
				File[] fn = fhmax.listFiles();
				if (fn.length == 0) continue;
				File fnmax = Collections.max(Arrays.asList(fn), fc);
				File[] fs = fnmax.listFiles();
				if (fs.length == 0) continue;
				File fsmax = Collections.max(Arrays.asList(fs), fc);
				c.set(YEAR, Integer.parseInt(fymax.getName()));
				c.set(MONTH, Integer.parseInt(fmmax.getName()));
				c.set(DATE, Integer.parseInt(fdmax.getName()));
				c.set(HOUR_OF_DAY,
						Integer.parseInt(fhmax.getName()));
				c.set(MINUTE, Integer.parseInt(fnmax.getName()));
				c.set(SECOND, Integer.parseInt(fsmax.getName()));
				c.set(MILLISECOND, 0);
				Map<String,Object> m = new LinkedHashMap<String,Object>(4);
				m.put("id", id.replace('_', '/'));
				m.put("t", c.getTime());
				m.put("val", IpmemsJsonUtil.parse(fsmax));
				l.add(m);
				if (max > 0 && l.size() >= max) break;
			}
			return l;
		}
	}
	
	private final class StdValues extends Values {
		public StdValues(Map arg) {
			super(arg);
		}
		
		private File[] f(File f, int field, Comparator<File> cm) {
			c.set(field, Integer.parseInt(f.getName()));
			File[] r = f.listFiles();
			Arrays.sort(r, cm);
			return r;
		}
		
		private boolean values(File d, List<Map<String,Object>> l,
				int max, Comparator<File> cm) throws Exception {
			File[] fy = d.listFiles();
			Arrays.sort(fy, cm);
			c.set(MILLISECOND, 0);
			Map<String,Object> m;
			for (File yf: fy) for (File mf: f(yf, YEAR, cm))
				for (File df: f(mf, MONTH, cm)) for (File hf: f(df, DATE, cm))
					for (File nf: f(hf, HOUR_OF_DAY, cm)) 
						for (File sf: f(nf, MINUTE, cm)) {
							c.set(SECOND, Integer.parseInt(sf.getName()));
							m = new LinkedHashMap<String,Object>(4);
							m.put("id", d.getName().replace('_', '/'));
							m.put("t", c.getTime());
							m.put("val", IpmemsJsonUtil.parse(sf));
							l.add(m);
							if (max > 0 && l.size() >= max) return true;
						}
			return false;
		}

		@Override
		public final List<Map<String,Object>> ids(
				String p, List v) throws Exception {
			List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
			int max = get(Integer.class, a, "max", 0);
			String ord = get(String.class, a, "ord", "asc");
			boolean asc = "asc".equalsIgnoreCase(ord);
			for (Object oid: v) {
				String id = String.valueOf(oid).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				if (values(sd, l, max, asc ? fc : rc)) break;
			}
			return l;
		}		
	}
	
	private final class Limits implements Callable<Map<String,Date>> {
		public Limits(Map arg, Comparator<File> comp) {
			a = arg;
			cm = comp;
		}

		@Override
		public Map<String,Date> call() throws Exception {
			String p = get(String.class, a, "prefix", "current").toLowerCase();
			if (a.containsKey("ids")) return ids(p, (List)a.get("ids"));
			else if (a.containsKey("id")) 
				return ids(p, Collections.singletonList(a.get("id")));
			else {
				File dir = new File(getDir(), p);
				return ids(p, Arrays.asList(dir.list()));
			}
		}
		
		private Map<String,Date> ids(String p, List ids) {
			Map<String,Date> m = new HashMap<String,Date>();
			int max = get(Integer.class, a, "max", 0);
			for (Object oid: ids) {
				if (max > 0 && m.size() >= max) break;
				String id = String.valueOf(oid).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				File[] fy = sd.listFiles();
				if (fy.length == 0) continue;
				File fym = Collections.max(Arrays.asList(fy), cm);
				File[] fm = fym.listFiles();
				if (fm.length == 0) continue;
				File fmm = Collections.max(Arrays.asList(fm), cm);
				File[] fd = fmm.listFiles();
				if (fd.length == 0) continue;
				File fdm = Collections.max(Arrays.asList(fd), cm);
				File[] fh = fdm.listFiles();
				if (fh.length == 0) continue;
				File fhm = Collections.max(Arrays.asList(fh), cm);
				File[] fn = fhm.listFiles();
				if (fn.length == 0) continue;
				File fnm = Collections.max(Arrays.asList(fn), cm);
				File[] fs = fnm.listFiles();
				if (fs.length == 0) continue;
				File fsm = Collections.max(Arrays.asList(fs), cm);
				c.set(MILLISECOND, 0);
				c.set(YEAR, Integer.parseInt(fym.getName()));
				c.set(MONTH, Integer.parseInt(fmm.getName()));
				c.set(DATE, Integer.parseInt(fdm.getName()));
				c.set(HOUR_OF_DAY, Integer.parseInt(fhm.getName()));
				c.set(MINUTE, Integer.parseInt(fnm.getName()));
				c.set(SECOND, Integer.parseInt(fsm.getName()));
				m.put(id.replace('_', '/'), c.getTime());
			}
			return m;
		}
		
		private final Map a;
		private final Calendar c = 
				Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		private final Comparator<File> cm;
	}
	
	private final class Count implements Callable<Integer> {
		public Count(Map arg) {
			a = arg;
		}

		@Override
		public Integer call() throws Exception {
			String p = get(String.class, a, "prefix", "current").toLowerCase();
			List ids;
			if (a.containsKey("ids")) ids = (List)a.get("ids");
			else if (a.containsKey("id")) 
				ids = Collections.singletonList(a.get("id"));
			else ids = Arrays.asList(new File(getDir(), p).list());
			if (a.containsKey("t")) {
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
				c.setTime(date(a.get("t")));
				int n = 0;
				for (Object oid: ids) {
					String id = String.valueOf(oid).replace('/', '_');
					File sd = subDir(p, id);
					if (!sd.exists()) continue;
					String path = c.get(YEAR) + "/" + c.get(MONTH) + "/" +
							c.get(DATE) + "/" + c.get(HOUR_OF_DAY) + "/" +
							c.get(MINUTE) + "/" + c.get(SECOND);
					File file = new File(sd, path);
					if (file.exists()) n++;
				}
				return Integer.valueOf(n);
			} else {
				int q = 0;
				for (Object oid: ids) {
					String id = String.valueOf(oid).replace('/', '_');
					File sd = subDir(p, id);
					if (!sd.exists()) continue;
					for (File y: sd.listFiles()) for (File m: y.listFiles())
						for (File d: m.listFiles()) for (File h: d.listFiles())
							for (File n: h.listFiles()) q += n.list().length;
				}
				return Integer.valueOf(q);
			}
		}
		
		private final Map a;
	}
	
	private abstract class ABB implements Callable<List<Map<String,Object>>> {
		public ABB(Map arg) {
			a = arg;
			asc = !"desc".equalsIgnoreCase(String.valueOf(a.get("ord")));
		}
		
		protected final Comparator<File> fc = new ForwardComparator();
		protected final Comparator<File> rc = new BackwardComparator();
		protected final Map a;
		protected final boolean asc;
		protected final Calendar c = 
				Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		
		private class ForwardComparator implements Comparator<File> {
			private Calendar c(File f) {
				Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
				c.setTimeInMillis(0L);
				ArrayList<File> l = new ArrayList<File>();
				while (!getDirKey().equals(f.getName())) {
					l.add(0, f);
					f = f.getParentFile();
				}
				l.remove(0); // Type folder
				l.remove(0); // Id folder
				for (int i = 0; i < l.size(); i++) switch (i) {
					case 0:
						c.set(YEAR, Integer.parseInt(l.get(i).getName()));
						break;
					case 1:
						c.set(MONTH, Integer.parseInt(l.get(i).getName()));
						break;
					case 2:
						c.set(DATE, Integer.parseInt(l.get(i).getName()));
						break;
					case 3:
						c.set(HOUR_OF_DAY,
								Integer.parseInt(l.get(i).getName()));
						break;
					case 4:
						c.set(MINUTE, Integer.parseInt(l.get(i).getName()));
						break;
					case 5:
						c.set(SECOND, Integer.parseInt(l.get(i).getName()));
						break;
				}
				return c;
			}
			
			@Override
			public int compare(File o1, File o2) {
				return c(o1).compareTo(c(o2));
			}
		}
		
		private final class BackwardComparator extends ForwardComparator {
			@Override
			public int compare(File o1, File o2) {
				return super.compare(o2, o1);
			}
		}
	}
	
	private final class AB extends ABB {
		public AB(Map arg, boolean af) {
			super(arg);
			after = af;
			cm = after ? fc : rc;
		}
		
		private Set<File> f(File d, File threshold, boolean inc) {
			File[] fl = d.listFiles();
			if (fl.length == 0) return Collections.emptySet();
			TreeSet<File> ts = new TreeSet<File>(cm);
			ts.addAll(Arrays.asList(fl));
			NavigableSet<File> rs = ts.tailSet(threshold, inc);
			return asc && after || !asc && !after ? rs : rs.descendingSet();
		}
		
		private Set<File> f(File d, File threshold) {
			return f(d, threshold, true);
		}
		
		@Override
		public List<Map<String,Object>> call() throws Exception {
			c.setTime(date(a.get("t")));
			String p = get(String.class, a, "prefix", "current").toLowerCase();
			List ids;
			if (a.containsKey("ids")) ids = (List)a.get("ids");
			else if (a.containsKey("id")) 
				ids = Collections.singletonList(a.get("id"));
			else ids = Arrays.asList(new File(getDir(), p).list());
			List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
			int y = c.get(YEAR);
			int m = c.get(MONTH);
			int d = c.get(DATE);
			int h = c.get(HOUR_OF_DAY);
			int n = c.get(MINUTE);
			int s = c.get(SECOND);
			c.set(MILLISECOND, 0);
			boolean i = get(Boolean.class, a, "inc", false);
			int max = get(Integer.class, a, "max", 0);
			Map<String,Object> w;
			for (Object oid: ids) {
				String id = String.valueOf(oid).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				File thY = new File(sd, Integer.toString(y));
				File thM = new File(thY, Integer.toString(m));
				File thD = new File(thM, Integer.toString(d));
				File thH = new File(thD, Integer.toString(h));
				File thN = new File(thH, Integer.toString(n));
				File thS = new File(thN, Integer.toString(s));
				for (File yf: f(sd, thY)) for (File mf: f(yf, thM))
					for (File df: f(mf, thD)) for (File hf: f(df, thH))
						for (File nf: f(hf, thN)) for (File sf: f(nf, thS, i)) {
							c.set(YEAR, Integer.parseInt(yf.getName()));
							c.set(MONTH, Integer.parseInt(mf.getName()));
							c.set(DATE, Integer.parseInt(df.getName()));
							c.set(HOUR_OF_DAY, Integer.parseInt(hf.getName()));
							c.set(MINUTE, Integer.parseInt(nf.getName()));
							c.set(SECOND, Integer.parseInt(sf.getName()));
							w = new LinkedHashMap<String,Object>(4);
							w.put("id", id.replace('_', '/'));
							w.put("t", c.getTime());
							w.put("val", IpmemsJsonUtil.parse(sf));
							l.add(w);
							if (max > 0 && l.size() >= max) return l;
						}
			}
			return l;
		}
		
		private final boolean after;
		private final Comparator<File> cm;
	}
	
	private final class Between extends ABB {
		public Between(Map arg) {
			super(arg);
		}
		
		private Set<File> f(File d, File t1, File t2, boolean i1, boolean i2) {
			File[] fl = d.listFiles();
			if (fl.length == 0) return Collections.emptySet();
			TreeSet<File> ts = new TreeSet<File>(fc);
			ts.addAll(Arrays.asList(fl));
			NavigableSet<File> rs = ts.subSet(t1, i1, t2, i2);
			return asc ? rs : rs.descendingSet();
		}
		
		private Set<File> f(File d, File t1, File t2) {
			return f(d, t1, t2, true, true);
		}

		@Override
		public List<Map<String,Object>> call() throws Exception {
			Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			c1.setTime(date(a.get("t1")));
			c2.setTime(date(a.get("t2")));
			String p = get(String.class, a, "prefix", "current").toLowerCase();
			List ids;
			if (a.containsKey("ids")) ids = (List)a.get("ids");
			else if (a.containsKey("id")) 
				ids = Collections.singletonList(a.get("id"));
			else ids = Arrays.asList(new File(getDir(), p).list());
			List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
			int y1 = c1.get(YEAR), y2 = c2.get(YEAR);
			int m1 = c1.get(MONTH), m2 = c2.get(MONTH);
			int d1 = c1.get(DATE), d2 = c2.get(DATE);
			int h1 = c1.get(HOUR_OF_DAY), h2 = c2.get(HOUR_OF_DAY);
			int n1 = c1.get(MINUTE), n2 = c2.get(MINUTE);
			int s1 = c1.get(SECOND), s2 = c2.get(SECOND);
			c.set(MILLISECOND, 0);
			boolean L = get(Boolean.class, a, "linc", false);
			boolean R = get(Boolean.class, a, "rinc", false);
			int max = get(Integer.class, a, "max", 0);
			Map<String,Object> w;
			for (Object oid: ids) {
				String id = String.valueOf(oid).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				File y = new File(sd, Integer.toString(y1));
				File m = new File(y, Integer.toString(m1));
				File d = new File(m, Integer.toString(d1));
				File h = new File(d, Integer.toString(h1));
				File n = new File(h, Integer.toString(n1));
				File s = new File(n, Integer.toString(s1));
				File Y = new File(sd, Integer.toString(y2));
				File M = new File(Y, Integer.toString(m2));
				File D = new File(M, Integer.toString(d2));
				File H = new File(D, Integer.toString(h2));
				File N = new File(H, Integer.toString(n2));
				File S = new File(N, Integer.toString(s2));
				for (File yf: f(sd, y, Y)) for (File mf: f(yf, m, M))
					for (File df: f(mf, d, D)) for (File hf: f(df, h, H))
						for (File nf: f(hf, n, N)) for (File sf:f(nf,s,S,L,R)) {
							c.set(YEAR, Integer.parseInt(yf.getName()));
							c.set(MONTH, Integer.parseInt(mf.getName()));
							c.set(DATE, Integer.parseInt(df.getName()));
							c.set(HOUR_OF_DAY, Integer.parseInt(hf.getName()));
							c.set(MINUTE, Integer.parseInt(nf.getName()));
							c.set(SECOND, Integer.parseInt(sf.getName()));
							w = new LinkedHashMap<String,Object>(4);
							w.put("id", id.replace('_', '/'));
							w.put("t", c.getTime());
							w.put("val", IpmemsJsonUtil.parse(sf));
							l.add(w);
							if (max > 0 && l.size() >= max) return l;
						}
			}
			return l;
		}		
	}
	
	private abstract class UD implements Callable<Integer> {
		public UD(Map arg) {
			a = arg;
		}
		
		protected int delete(File d, long t) throws Exception {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			c.setTimeInMillis(t);
			String path = c.get(YEAR) + "/" + c.get(MONTH) + "/" +
					c.get(DATE) + "/" + c.get(HOUR_OF_DAY) + "/" +
					c.get(MINUTE) + "/" + c.get(SECOND);
			File file = new File(d, path);
			if (file.exists()) {
				if (!file.delete()) return 0;
				File n = file.getParentFile();
				n.delete();
				File h = n.getParentFile();
				h.delete();
				d = h.getParentFile();
				d.delete();
				File m = d.getParentFile();
				m.delete();
				File y = m.getParentFile();
				y.delete();
				return 1;
			} else return 0;
		}
		
		protected int delete(File d) throws Exception {
			int c = 0;
			if (d.isDirectory()) {
				for (File f: d.listFiles()) c += delete(f);
				d.delete();
				return c;
			} else {
				d.delete();
				return 1;
			}
		}
		
		protected final Map a;
	}
	
	private final class Delete extends UD {
		public Delete(Map arg) {
			super(arg);
		}
		
		@Override
		public Integer call() throws Exception {
			String p = get(String.class, a, "prefix", "current");
			List rows = (List)a.get("rows");
			int u = 0;
			for (Object o: rows) if (o instanceof Map) {
				Map m = (Map)o;
				String id = String.valueOf(m.get("id")).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				if (m.containsKey("t")) u += delete(sd, millis(m.get("t")));
				else u += delete(sd);
			} else if (o instanceof List) {
				List l = (List)o;
				String id = String.valueOf(l.get(0)).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				if (l.size() == 1) u += delete(sd);
				else u += delete(sd, millis(l.get(1)));
			}
			return Integer.valueOf(u);
		}		
	}
	
	private final class Update extends UD {
		public Update(Map arg) {
			super(arg);
		}
		
		private int updtv(File d, long t, Object val) throws Exception {
			int n = delete(d);
			write(d, t, val);
			return n;
		}
		
		private int updv(File d, long t, Object val) throws Exception {
			int n = delete(d, t);
			if (n > 0) {
				write(d, t, val);
				return 1;
			} else return 0;
		}

		@Override
		public Integer call() throws Exception {
			String p = get(String.class, a, "prefix", "current");
			List rows = (List)a.get("rows");
			int u = 0;
			for (Object o: rows) if (o instanceof Map) {
				Map m = (Map)o;
				String id = String.valueOf(m.get("id")).replace('/', '_');
				File sd = subDir(p, id);
				if (!sd.exists()) continue;
				if (m.containsKey("nt")) 
					u += updtv(sd, millis(m.get("nt")), m.get("val"));
				else if (m.containsKey("t"))
					u += updv(sd, millis(m.get("t")), m.get("val"));
			} else if (o instanceof List) {
				List l = (List)o;
				if (l.size() == 3) {
					String id = String.valueOf(l.get(0)).replace('/', '_');
					File sd = subDir(p, id);
					if (!sd.exists()) continue;
					u += updv(sd, millis(l.get(1)), l.get(2));
				}
			}
			return Integer.valueOf(u);
		}
	}
}
