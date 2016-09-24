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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.ipsilon.ipmems.io.IpmemsIO;
import org.ipsilon.ipmems.logging.IpmemsLoggers;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS protocol slot processor.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsProtProcessor extends IpmemsPropertized implements Runnable {
	/**
	 * Constructs the protocol slot processor.
	 * @param props Slot processor properties.
	 * @param sl Protocol slot.
	 * @param o Protocol I/O object.
	 */
	@SuppressWarnings("unchecked")
	public IpmemsProtProcessor(Map props, IpmemsProtSlot sl, IpmemsIO o) {
		super(props.containsKey("params") ?
				(Map)props.get("params") : Collections.EMPTY_MAP);
		slot = sl;
		io = o;
		enc = get(String.class, props, "encoding", null);
		urlEncoded = get(Boolean.class, props, "urlEncoded", false);
		checkAll = get(Boolean.class, props, "checkAll", true);
		inConfs = get(Map.class, props, "inputs", Collections.EMPTY_MAP);
		timerConfs = get(Map.class, props, "timers", Collections.EMPTY_MAP);
		outConfs = get(Map.class, props, "outputs", Collections.EMPTY_MAP);
		guards = get(Map.class, props, "guards", new LinkedHashMap());
		for (String k: inConfs.keySet()) 
			if (!guards.containsKey(k)) guards.put(k, true);
		keySeparator = get(Character.class, props, "keySeparator", '_');
		funcSelector = props.get("funcSelector");
		delay = get(Long.class, props, "delay", 100L);
		timeout = get(Long.class, props, "timeout", 1000L);
		int tps = get(Integer.class, props, "timerPoolSize", 1);
		timer = new ScheduledThreadPoolExecutor(tps);
		IpmemsLoggers.info(log(), "{0} +", toString());
	}

	@Override
	public void run() {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = io.getInputStream();
			os = io.getOutputStream();
			process(is, os, new byte[0]);
		} catch (InterruptedException x) {
			IpmemsLoggers.info(log(), "{0} Interrupted");
		} catch (Exception x) {
			IpmemsLoggers.warning(log(), "{0}", x, this);
		} finally {
			timer.shutdownNow();
			if (is != null) try {is.close();} catch (Exception x) {}
			if (os != null) try {os.close();} catch (Exception x) {}
			try {io.close();} catch (Exception x) {}
		}
	}
	
	@SuppressWarnings("SleepWhileInLoop")
	private void process(InputStream i, OutputStream o, byte[] b) throws 
			InterruptedException, IOException {
		for (long to = 0; true; to += delay) {
			Thread.sleep(delay);
			int s = i.available();
			if (s <= 0) continue;
			int idx = b.length;
			b = Arrays.copyOf(b, idx + s);
			int n = i.read(b, idx, s);
			if (n < 0) b = Arrays.copyOf(b, idx);
			else if (n < s) b = Arrays.copyOf(b, idx + n);
			data = newData();
			for (Map.Entry<String,Object> e: guards.entrySet())
				if (!check(b, e.getKey(), e.getValue())) continue; else	try {
					Map<String,Object> m = parse(b, e.getKey());
					if (m.keySet().equals(inConfs.get(e.getKey()).keySet())) {
						data.put(e.getKey(), m);
						if (!checkAll) break;
					}
				} catch (Exception x) {
					IpmemsLoggers.warning(log(), "{0}/{1}", x, this,e.getKey());
				}
			if (data.isEmpty()) {
				if (to > timeout) {
					b = new byte[0];
					to = 0;
					continue;
				} else continue;
			}
			IpmemsProtUtil.log(log(), urlEncoded, enc, "<-", this, b, data);
			b = new byte[0];
			to = -delay;
			if (funcSelector != null) try {
				Object k = IpmemsScriptEngines.call(funcSelector, this, data);
				if (k != null) key = k.toString();
			} catch (Exception x) {
				IpmemsLoggers.warning(log(), "{0} Selector", x, this);
			} else if (data.size() > 1) {
				String[] ks = data.keySet().toArray(new String[data.size()]);
				Arrays.sort(ks);
				StringBuilder sb = new StringBuilder(ks[0]);
				for (int k = 1; k < ks.length; k++) {
					sb.append(keySeparator);
					sb.append(ks[k]);
				}
				key = sb.toString();
			} else key = data.keySet().toArray()[0].toString();
			conf = outConfs.get(key);
			if (conf == null) {
				IpmemsLoggers.warning(log(), "{0} Empty output", this);
				continue;
			}
			byte[] m = b;
			try {
				m = IpmemsProtUtil.makeMessage(this, enc, conf);
				o.write(m);
				o.flush();
				IpmemsProtUtil.log(log(), urlEncoded, enc, "->", this, m, key);
			} catch (Exception x) {
				IpmemsProtUtil.log(log(), urlEncoded, enc, "!!", this, m, x);
			}
		}
	}
	
	private boolean check(byte[] buf, String k, Object g) {
		if (g instanceof Number) {
			return buf.length == ((Number)g).intValue();
		} else if (g instanceof Boolean) {
			return Boolean.TRUE.equals((Boolean)g);
		} else if (g != null) try {
			return Boolean.TRUE.equals(IpmemsScriptEngines.call(g, buf, this));
		} catch (Exception x) {
			IpmemsLoggers.warning(log(), "{0}/{1} Guard error", x, this, k);
			return false;
		} else return true;
	}
	
	private Map<String,Map<String,Object>> newData() {
		return new LinkedHashMap<String,Map<String,Object>>();
	}
	
	private Map<String,Object> parse(byte[] b, String k) throws Exception {
		return IpmemsProtUtil.parseMessage(b, this, enc, inConfs.get(k));
	}
	
	/**
	 * Get the associated slot.
	 * @return Associated slot.
	 */
	public IpmemsProtSlot getSlot() {
		return slot;
	}

	/**
	 * Get the associated I/O.
	 * @return Associated I/O.
	 */
	public IpmemsIO getIo() {
		return io;
	}

	/**
	 * Get the checkAll flag.
	 * @return checkAll flag.
	 */
	public boolean isCheckAll() {
		return checkAll;
	}

	/**
	 * Get the I/O delay.
	 * @return I/O delay.
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * Get the associated timer.
	 * @return Associated timer.
	 */
	public ScheduledThreadPoolExecutor getTimer() {
		return timer;
	}

	/**
	 * Get guards.
	 * @return Guards.
	 */
	public Map<String,Object> getGuards() {
		return guards;
	}

	/**
	 * Get the input configuration map.
	 * @return Input configuration map.
	 */
	public Map<String,Map<String,Object>> getInConfs() {
		return inConfs;
	}

	/**
	 * Get the output configuration map.
	 * @return Output configuration map.
	 */
	public Map<String,Map<String,Object>> getOutConfs() {
		return outConfs;
	}

	/**
	 * Get the timer configuration map.
	 * @return Timer configuration map.
	 */
	public Map<String,Map<String,Object>> getTimerConfs() {
		return timerConfs;
	}

	/**
	 * Get the log name.
	 * @return Log name.
	 */
	public final String log() {
		return slot.log();
	}

	/**
	 * Get the current output key.
	 * @return Current output key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get the current output configuration.
	 * @return Current output configuration.
	 */
	public Map<String,Object> getConf() {
		return conf;
	}

	/**
	 * Get the current data.
	 * @return Current data.
	 */
	public Map<String,Map<String,Object>> getData() {
		return data;
	}
	
	@Override
	public final String toString() {
		return slot + ":" + io;
	}
		
	private final IpmemsProtSlot slot;
	private final IpmemsIO io;
	private final boolean checkAll;
	private final long delay;
	private final long timeout;
	private final char keySeparator;
	private final Map<String,Map<String,Object>> inConfs;
	private final Map<String,Object> guards;
	private final Map<String,Map<String,Object>> timerConfs;
	private final Map<String,Map<String,Object>> outConfs;
	private final Object funcSelector;
	private final ScheduledThreadPoolExecutor timer;
	private final String enc;
	private final boolean urlEncoded;
	private String key;
	private Map<String,Object> conf;
	private Map<String,Map<String,Object>> data;
}
