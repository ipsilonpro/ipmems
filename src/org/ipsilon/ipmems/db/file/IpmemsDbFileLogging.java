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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import static java.util.Calendar.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import org.ipsilon.ipmems.json.IpmemsJsonUtil;

/**
 * IPMEMS file database logging.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDbFileLogging extends IpmemsDbFileGate {

	@Override
	public String getDirKey() {
		return "logging";
	}

	@Override
	public Map<String,Object> gf(String k, Map a) {
		if ("put".equals(k)) return put(a);
		else if ("get".equals(k)) return get(a);
		else if ("last".equals(k)) return last(a);
		else return Collections.emptyMap();
	}
	
	private Map<String,Object> put(Map map) {
		return cuf("updateCount", new Put(map));
	}

	private Map<String,Object> get(Map map) {
		return cuf("rows", new Get(map));
	}

	private Map<String,Object> last(Map map) {
		return cuf("rows", new Last(map));
	}
	
	private abstract class ALog<T> implements Callable<T>, Comparator<File> {
		public ALog(Map arg) {
			a = arg;
		}
		
		private Calendar c(File f) {
			Calendar cl = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cl.setTimeInMillis(0L);
			ArrayList<File> l = new ArrayList<File>();
			while (!getDirKey().equals(f.getName())) {
				l.add(0, f);
				f = f.getParentFile();
			}
			l.remove(0); // Id folder
			l.remove(0); // Msg folder
			for (int i = 0; i < l.size(); i++) switch (i) {
				case 0:
					cl.set(YEAR, Integer.parseInt(l.get(i).getName()));
					break;
				case 1:
					cl.set(MONTH, Integer.parseInt(l.get(i).getName()));
					break;
				case 2:
					cl.set(DATE, Integer.parseInt(l.get(i).getName()));
					break;
				case 3:
					cl.set(HOUR_OF_DAY,
							Integer.parseInt(l.get(i).getName()));
					break;
				case 4:
					cl.set(MINUTE, Integer.parseInt(l.get(i).getName()));
					break;
				case 5:
					cl.set(SECOND, Integer.parseInt(l.get(i).getName()));
					break;
			}
			return cl;
		}

		@Override
		public int compare(File o1, File o2) {
			return c(o1).compareTo(c(o2));
		}
		
		protected final Map a;
		protected final Calendar c = 
				Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	}
	
	private final class Put extends ALog<Integer> {
		public Put(Map arg) {
			super(arg);
		}

		@Override
		public Integer call() throws Exception {
			List rows = (List)a.get("rows");
			int u = 0;
			for (Object o: rows) {
				Map m = (Map)o;
				String aid = get(String.class, m, "aid", "").replace('/', '_');
				String msg = get(String.class, m, "msg", "");
				long ms = m.containsKey("t") ? millis(m.get("t")) :
						System.currentTimeMillis();
				c.setTimeInMillis(ms);
				List args = get(List.class, m, "args", Collections.EMPTY_LIST);
				String dp = aid + "/" + msg + "/" + c.get(YEAR) + "/" +
						c.get(MONTH) + "/" + c.get(DATE) + "/" +
						c.get(HOUR_OF_DAY) + "/" + c.get(MINUTE);
				File dir = new File(getDir(), dp);
				if (!dir.exists()) dir.mkdirs();
				File file = new File(dir, Integer.toString(c.get(SECOND)));
				IpmemsJsonUtil.make(args, file);
				u++;
			}
			return Integer.valueOf(u);
		}
	}
	
	private class Get extends ALog<List<Map<String,Object>>> {
		public Get(Map arg) {
			super(arg);
		}
		
		protected long getTime() throws Exception {
			return a.containsKey("t") ? millis(a.get("t")) : 0L;
		}
		
		protected boolean asc() {
			return true;
		}
		
		private Set<File> f(File d, File threshold, boolean inc) {
			File[] fl = d.listFiles();
			if (fl.length == 0) return Collections.emptySet();
			TreeSet<File> ts = new TreeSet<File>(this);
			ts.addAll(Arrays.asList(fl));
			return asc() ? ts.tailSet(threshold, inc) :
					ts.tailSet(threshold, inc).descendingSet();
		}
		
		private Set<File> f(File d, File threshold) {
			return f(d, threshold, true);
		}
		
		private List<Map<String,Object>> byMsg() throws Exception {
			List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
			int yy = c.get(YEAR);
			int mm = c.get(MONTH);
			int dd = c.get(DATE);
			int hh = c.get(HOUR_OF_DAY);
			int nn = c.get(MINUTE);
			int ss = c.get(SECOND);
			c.set(MILLISECOND, 0);
			int max = get(Integer.class, a, "max", 0);
			String msg = String.valueOf(a.get("msg"));
			Map<String,Object> w;
			for (File i: getDir().listFiles()) for (File msgf: i.listFiles())
				if (msgf.getName().equals(msg)) {
					File Y = new File(msgf, Integer.toString(yy));
					File M = new File(Y, Integer.toString(mm));
					File D = new File(M, Integer.toString(dd));
					File H = new File(D, Integer.toString(hh));
					File N = new File(H, Integer.toString(nn));
					File S = new File(N, Integer.toString(ss));
					for (File y: f(msgf, Y)) for (File m: f(y, M))
						for (File d: f(m, D)) for (File h: f(d, H))
							for (File n: f(h, N)) for (File s: f(n, S, false)) {
								c.set(YEAR, Integer.parseInt(y.getName()));
								c.set(MONTH, Integer.parseInt(m.getName()));
								c.set(DATE, Integer.parseInt(d.getName()));
								c.set(HOUR_OF_DAY, Integer.decode(h.getName()));
								c.set(MINUTE, Integer.parseInt(n.getName()));
								c.set(SECOND, Integer.parseInt(s.getName()));
								w = new LinkedHashMap<String,Object>(4);
								w.put("aid", i.getName().replace('_', '/'));
								w.put("msg", msgf.getName().replace('_', '/'));
								w.put("t", c.getTime());
								w.put("val", IpmemsJsonUtil.parse(s));
								l.add(w);
								if (max > 0 && l.size() >= max) return l;
							}
				}
			return l;
		}
		
		private List<Map<String,Object>> byAid() throws Exception {
			List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
			int yy = c.get(YEAR);
			int mm = c.get(MONTH);
			int dd = c.get(DATE);
			int hh = c.get(HOUR_OF_DAY);
			int nn = c.get(MINUTE);
			int ss = c.get(SECOND);
			c.set(MILLISECOND, 0);
			int max = get(Integer.class, a, "max", 0);
			String aid = String.valueOf(a.get("aid")).replace('/', '_');
			File sd = new File(getDir(), aid);
			if (!sd.exists()) return l;
			Map<String,Object> w;
			for (File msgf: sd.listFiles()) {
				File Y = new File(msgf, Integer.toString(yy));
				File M = new File(Y, Integer.toString(mm));
				File D = new File(M, Integer.toString(dd));
				File H = new File(D, Integer.toString(hh));
				File N = new File(H, Integer.toString(nn));
				File S = new File(N, Integer.toString(ss));
				for (File y: f(msgf, Y)) for (File m: f(y, M))
					for (File d: f(m, D)) for (File h: f(d, H))
						for (File n: f(h, N)) for (File s: f(n, S, false)) {
							c.set(YEAR, Integer.parseInt(y.getName()));
							c.set(MONTH, Integer.parseInt(m.getName()));
							c.set(DATE, Integer.parseInt(d.getName()));
							c.set(HOUR_OF_DAY, Integer.decode(h.getName()));
							c.set(MINUTE, Integer.parseInt(n.getName()));
							c.set(SECOND, Integer.parseInt(s.getName()));
							w = new LinkedHashMap<String,Object>(4);
							w.put("aid", sd.getName().replace('_', '/'));
							w.put("msg", msgf.getName().replace('_', '/'));
							w.put("t", c.getTime());
							w.put("val", IpmemsJsonUtil.parse(s));
							l.add(w);
							if (max > 0 && l.size() >= max) return l;
						}				
			}
			return l;
		}

		@Override
		public List<Map<String,Object>> call() throws Exception {
			c.setTimeInMillis(getTime());
			if (a.containsKey("msg")) return byMsg();
			else if (a.containsKey("aid")) return byAid();
			else return Collections.emptyList();
		}
	}
	
	private final class Last extends Get {
		public Last(Map arg) {
			super(arg);
		}

		@Override
		protected boolean asc() {
			return false;
		}

		@Override
		protected long getTime() throws Exception {
			Calendar cl = Calendar.getInstance();
			int dt = get(Integer.class, a, "dt", 60);
			Unit unit = get(Unit.class, a, "unit", Unit.SECOND);
			cl.add(SECOND, -dt * unit.getKoeff());
			return cl.getTimeInMillis();
		}
	}
	
	private enum Unit {
		SECOND(1), MINUTE(60), HOUR(3600), DAY(24 * 3600), WEEK(7 * 24 * 3600);
		
		Unit(int k) {
			koeff = k;
		}
		
		public int getKoeff() {
			return koeff;
		}
		
		private final int koeff;
	}
}
