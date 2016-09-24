package org.ipsilon.ipmems.prot;

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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import org.ipsilon.ipmems.IpmemsStrings;
import org.ipsilon.ipmems.io.IpmemsIO;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.sch.IpmemsObjItf;
import org.ipsilon.ipmems.sch.IpmemsTask;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsCollections;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * Protocol utilities.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsProtUtil {
	/**
	 * Make the binary message.
	 * @param p Any object.
	 * @param e Encoding.
	 * @param c Message configuration.
	 * @return Message as byte array.
	 * @throws IpmemsProtException Protocol exception.
	 */
	@SuppressWarnings("unchecked")
	public static byte[] makeMessage(
			final Object p,
			final String e, 
			final Map<String,Object> c) {
		IpmemsProtBinding b = new IpmemsProtBinding(p, c);
		for (Map.Entry<String,Object> i: c.entrySet()) try {
			b.key = i.getKey();
			Object o = IpmemsScriptEngines.call(i.getValue(), b);
			byte[] buf;
			if (o == null) buf = new byte[0];
			else if (o instanceof byte[]) buf = (byte[])o;
			else if (o instanceof CharSequence) 
				buf = o.toString().getBytes(e == null ? "ISO-8859-1" : e);
			else if (o instanceof Number) 
				buf = new byte[] {((Number)o).byteValue()};
			else if (o instanceof Iterable)
				buf = IpmemsCollections.getBytes((Iterable)o);
			else if (o.getClass().isArray()) 
				buf = IpmemsCollections.getBytes(o);
			else buf = new byte[0];
			b.data.put(i.getKey(), buf);
			b.offset += buf.length;
			if (buf.length > 0) {
				int ofs = b.message.length;
				b.message = Arrays.copyOf(b.message, ofs + buf.length);
				System.arraycopy(buf, 0, b.message, ofs, buf.length);
			}
		} catch (Exception x) {
			throw new IllegalStateException(i.getKey(), x);
		}
		return b.message;
	}
		
	/**
	 * Parse the binary message.
	 * @param msg Binary message.
	 * @param p Any object.
	 * @param e Encoding.
	 * @param config Configuration map.
	 * @return Data map.
	 * @throws IpmemsProtException Protocol exception.
	 */
	public static Map<String,Object> parseMessage(
			final byte[] msg,
			final Object p,
			final String e,
			final Map<String,Object> config) {
		IpmemsProtBinding b = new IpmemsProtBinding(p, config, msg, 0);
		for (Map.Entry<String,Object> i: config.entrySet()) try {
			b.key = i.getKey();
			Object r;
			if (i.getValue() instanceof Map) {
				Map m = (Map)i.getValue();
				int off = b.offset;
				if (m.containsKey("o")) {
					Object o = m.get("o");
					if (o instanceof Number) off = ((Number)o).intValue();
					else {
						o = IpmemsScriptEngines.call(o, b);
						if (o instanceof Number) off = ((Number)o).intValue();
					}
				}
				if (off < 0 || off >= msg.length) break;
				int l = 0;
				if (m.containsKey("l")) {
					Object ln = m.get("l");
					if (ln instanceof Number) l = ((Number)ln).intValue();
					else {
						ln = IpmemsScriptEngines.call(ln, b);
						if (ln instanceof Number) l = ((Number)ln).intValue();
					}
				}
				if (off + l > msg.length) break;
				b.arg = Arrays.copyOfRange(msg, off, off + l);
				b.v = Boolean.TRUE.equals(m.get("s")) ? new String(
						b.arg, e == null ? "ISO-8859-1" : e) : b.arg;
				r = IpmemsScriptEngines.call(m.get("c"), b);
				b.offset = off + l;
			} else {
				b.arg = msg;
				b.v = msg;
				r = IpmemsScriptEngines.call(i.getValue(), b);
			}
			if (r != null) {
				if (r instanceof Exception) {
					b.data.put("$e", r);
					break;
				} else b.data.put(i.getKey(), r);
			} else break;
		} catch (Exception x) {
			throw new IllegalStateException(i.getKey(), x);
		}
		return b.data;
	}
	
	private static <T> T get(Class<T> c, IpmemsObjItf t, String n, T def) {
		try {
			return t.get(Boolean.class, "forceEval", false) ?
					t.eval(c, n, def) :	t.get(c, n, def);
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "{0} ?{1}", x, t, n);
			return def;
		}
	}
		
	/**
	 * Universal vectorized I/O task.
	 * @param t IPMEMS task.
	 * @param r Previous result.
	 * @return Data map.
	 * @throws Exception An exception.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> ioVectorTask(
			IpmemsTask t, Object r) throws Exception {
		if (r == null) return null;
		List<Map<String,Object>> o = 
				get(List.class, t, "output", Collections.EMPTY_LIST);
		List<List<Map<String,Object>>> i = 
				get(List.class, t, "input", Collections.EMPTY_LIST);
		List<List<Object>> guards =
				get(List.class, t, "guards", Collections.EMPTY_LIST);
		if (i.size() != o.size()) {
			IpmemsLoggers.warning("err", "{0} Inputs/outputs mismatch", t);
			return null;
		}
		if (i.size() != guards.size()) {
			IpmemsLoggers.warning("err", "{0} Inputs/guards mismatch", t);
			return null;
		}
		return protIO(
				o,
				t.containsKey("io") ?
					get(IpmemsIO.class, t, "io", null) :
					get(IpmemsIO.class, t.getTaskQueue(), "io", null),
				i,
				get(Boolean.class, t, "urlenc", false),
				get(Long.class, t, "ioTimeout", 1000L),
				get(Long.class, t, "ioDelay", 100L),
				get(Long.class, t, "ioPostDelay", 0L),
				get(String.class, t, "encoding", null),
				t,
				guards,
				get(Object.class, t, "resultRule", null),
				get(Boolean.class, t, "purgeIn", true),
				r instanceof Debug ? (Debug)r : null);
	}
		
	/**
	 * Universal I/O task.
	 * @param t IPMEMS task.
	 * @param r Previous result.
	 * @return Data map.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> ioTask(
			IpmemsTask t, Object r) throws Exception {
		if (r == null) return null;
		List in = get(List.class, t, "input", Collections.EMPTY_LIST);
		Map o = get(Map.class, t, "output", Collections.EMPTY_MAP);
		List guards = get(List.class, t, "guards", Collections.EMPTY_LIST);
		return protIO(
				o,
				t.containsKey("io") ?
					get(IpmemsIO.class, t, "io", null) :
					get(IpmemsIO.class, t.getTaskQueue(), "io", null),
				in,
				get(Boolean.class, t, "urlenc", false),
				get(Long.class, t, "ioTimeout", 1000L),
				get(Long.class, t, "ioDelay", 100L),
				get(Long.class, t, "ioPostDelay", 0L),
				get(String.class, t, "encoding", null),
				t,
				guards,
				get(Object.class, t, "resultRule", null),
				get(Boolean.class, t, "purgeIn", true),
				r instanceof Debug ? (Debug)r : null);
	}
	
	/**
	 * Get the encoded message.
	 * @param msg Binary message.
	 * @param enc Encoding.
	 * @param urlenc URL encoded flag.
	 * @return Encoded message.
	 */
	public static String encodeMsg(byte[] msg, String enc, boolean urlenc) {
		try {
			return urlenc ?
					URLEncoder.encode(new String(
						msg, enc == null ? "ISO-8859-1" : enc), "UTF-8") :
					IpmemsStrings.hex(msg);
		} catch (Exception x) {
			return IpmemsStrings.hex(msg);
		}
	}
	
	/**
	 * Logs the message.
	 * @param l Log name.
	 * @param u URL encoding flag.
	 * @param e Encoding.
	 * @param d Direction.
	 * @param o Object.
	 * @param b Message.
	 * @param r Result.
	 * @throws UnsupportedEncodingException 
	 */
	public static void log(
			String l,
			boolean u,
			String e,
			String d,
			Object o,
			byte[] b,
			Object r){
		String m = encodeMsg(b, e, u);
		IpmemsLoggers.fine(l, "{0} {1} {2}|{3} {4}", o, d, b.length, m, r);
	}
		
	@SuppressWarnings({
		"SleepWhileInLoop",
		"SleepWhileHoldingLock",
		"unchecked"
	})
	private static Map<String,Object> protIO(
			List<Map<String,Object>> c,
			IpmemsIO io,
			List<List<Map<String,Object>>> csl,
			boolean urlenc,
			long timeout,
			long delay,
			long postDelay,
			String enc,
			IpmemsTask o,
			List<List<Object>> gs,
			Object resultRule,
			boolean purge,
			Debug debug) throws IOException, InterruptedException {
		String lg = o.get(String.class, "logName", "usr");
		List<Map<String,Object>> rl = new ArrayList<Map<String,Object>>();
		InputStream is = io.getInputStream();
		OutputStream os = io.getOutputStream();
		byte[][] mo = new byte[c.size()][];
		byte[][] lb = new byte[c.size()][];
		synchronized(io) {for (int j = 0; j < c.size(); j++) {
			mo[j] = makeMessage(o, enc, c.get(j));
			log(lg, urlenc, enc, "->", o, mo[j], "");
			if (purge && is.available() > 0) is.read(new byte[is.available()]);
			os.write(mo[j]);
			os.flush();
			lb[j] = new byte[0];
			Map<String,Object> pm = null;
			MLOOP: for (long tm = 0; tm < timeout; tm += delay) {
				Thread.sleep(delay);
				int a = is.available();
				if (a <= 0) continue;
				lb[j] = Arrays.copyOf(lb[j], lb[j].length + a);
				int n = is.read(lb[j], lb[j].length - a, a);
				if (n < 0) {
					lb[j] = Arrays.copyOf(lb[j], lb[j].length - a);
					break;
				}
				if (n < a) lb[j] = Arrays.copyOf(lb[j], lb[j].length + n - a);
				for (int i = 0; i < csl.get(j).size(); i++) {
					Object g = gs.get(j) == null ? null : i < gs.get(j).size() ?
							gs.get(j).get(i) : null;
					if (g instanceof Number && 
							lb[j].length != ((Number)g).intValue())
						continue;
					else if (g != null) try {
						if (Boolean.FALSE.equals(IpmemsScriptEngines.call(
								g, o, lb[j]))) continue;
					} catch (Exception x) {
						throw new IllegalArgumentException("G" + j +","+ i, x);
					}
					pm = parseMessage(lb[j], o, enc, csl.get(j).get(i));
					if (pm.keySet().equals(csl.get(j).get(i).keySet())) {
						rl.add(pm);
						break MLOOP;
					} else if (pm.containsKey("$e")) {
						log(lg, urlenc, enc, "!!", o, lb[j], pm.get("$e"));
						pm = null;
					} else pm = null;
				}
			}
			log(lg, urlenc, enc, "<-", o, lb[j], pm);
			if (postDelay > 0) Thread.sleep(postDelay);
		}}
		if (rl.size() != lb.length) return null;
		Map<String,Object> res;
		if (resultRule != null) {
			if (resultRule instanceof Number)
				res = rl.get(((Number)resultRule).intValue());
			else if (resultRule instanceof List) {
				Map<String,Object> r = new LinkedHashMap<String,Object>();
				List l = (List)resultRule;
				for (int i = 0; i < Math.min(rl.size(), l.size()); i++) {
					Map<String,String> es = (Map)l.get(i);
					for (Map.Entry<String,String> e: es.entrySet()) {
						if (rl.get(i).containsKey(e.getKey())) 
							r.put(e.getValue(),	rl.get(i).get(e.getKey()));
					}
				}
				res = r;
			} else try {
				res = (Map)IpmemsScriptEngines.call(resultRule, o, rl);
			} catch (Exception x) {
				throw new IllegalArgumentException("Result rule", x);
			}
		} else res = rl.isEmpty() ? null : rl.get(rl.size() - 1);
		if (debug != null && res != null) {
			boolean hex = debug.get(Boolean.class, "hex", true);
			Object oo;
			Object oi;
			if (hex) {
				String[] so = new String[c.size()], si = new String[c.size()];
				for (int i = 0; i < so.length; i++) {
					so[i] = IpmemsStrings.hex(mo[i]);
					si[i] = IpmemsStrings.hex(lb[i]);
				}
				oo = so;
				oi = si;
			} else {
				oo = mo;
				oi = lb;
			}
			res.put("$o", oo);
			res.put("$i", oi);
		}
		return res;
	}
	
	@SuppressWarnings({
		"SleepWhileInLoop", 
		"SleepWhileHoldingLock",
		"unchecked"
	})
	private static Map<String,Object> protIO(
			Map<String,Object> c,
			IpmemsIO io,
			List<Map<String,Object>> cs,
			boolean urlenc,
			long timeout, 
			long delay, 
			long postDelay,
			String enc,
			IpmemsTask o,
			List gs,
			Object resultRule,
			boolean purge,
			Debug debug) throws IOException, InterruptedException {
		String lg = o.get(String.class, "logName", "usr");
		Map<String,Object> r = null;
		byte[] msg = makeMessage(o, enc, c);
		log(lg, urlenc, enc, "->", o, msg, "");
		InputStream is = io.getInputStream();
		OutputStream os = io.getOutputStream();
		byte[] b = new byte[0];
		synchronized(io) {
			if (purge && is.available() > 0) is.read(new byte[is.available()]);
			os.write(msg);
			os.flush();
			MLOOP: for (long tm = 0; tm < timeout; tm += delay) {
				Thread.sleep(delay);
				int a = is.available();
				if (a <= 0) continue;
				b = Arrays.copyOf(b, b.length + a);
				int n = is.read(b, b.length - a, a);
				if (n < 0) {
					b = Arrays.copyOf(b, b.length - a);
					break;
				}
				if (n < a) b = Arrays.copyOf(b, b.length + n - a);
				for (int i = 0; i < cs.size(); i++) {
					Object g = i < gs.size() ? gs.get(i) : null;
					if (g instanceof Number && 
							b.length != ((Number)g).intValue())	continue;
					else if (g != null) try {
						if (Boolean.FALSE.equals(IpmemsScriptEngines.call(
								g, o, b))) continue;
					} catch (Exception x) {
						throw new IllegalArgumentException("G" + i, x);
					}
					Map<String,Object> pm = parseMessage(b, o, enc, cs.get(i));
					if (pm.keySet().equals(cs.get(i).keySet())) {
						r = pm;
						break MLOOP;
					} else if (pm.containsKey("$e")) {
						log(lg, urlenc, enc, "!!", o, b, pm.get("$e"));
					}
				}
			}
		}
		log(lg, urlenc, enc, "<-", o, b, r);
		if (postDelay > 0) Thread.sleep(postDelay);
		if (resultRule != null) try {
			r = (Map)IpmemsScriptEngines.call(resultRule, o, r);
		} catch (Exception x) {
			throw new IllegalArgumentException("Rule", x);
		}
		if (debug != null && r != null) {
			boolean hex = debug.get(Boolean.class, "hex", true);
			r.put("$o", hex ? IpmemsStrings.hex(msg) : msg);
			r.put("$i", hex ? IpmemsStrings.hex(b) : b);
		}
		return r;
	}
	
	/**
	 * Debugging class.
	 */
	public static class Debug extends IpmemsPropertized {
		/**
		 * Constructs the debug object.
		 * @param args Arguments.
		 */
		public Debug(Object ... args) {
			for (int i = 0; i < args.length; i += 2)
				put(args[i].toString(), args[i + 1]);
		}
	}
}
