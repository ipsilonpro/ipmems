package org.ipsilon.ipmems.sch;

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

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.ipsilon.ipmems.logging.IpmemsLoggers;

/**
 * IPMEMS abstract task.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsStdTask extends IpmemsStdObject implements IpmemsTask {
	/**
	 * Constructs the task.
	 * @param parent Parent of task.
	 * @param n Task name.
	 * @param props Task properties.
	 */
	public IpmemsStdTask(IpmemsStdObject parent, String n, Map props) {
		super(parent, n, props);
		IpmemsLoggers.info("sch", "{0} +", toString());
		period = remove(Long.class, props, "period", 0L);
		delay = remove(Long.class, props, "delay", period);
		boolean callQueue = remove(Boolean.class, props, "callQueue", true);
		boolean killable = remove(Boolean.class, props, "killable", true);
		boolean realtime = remove(Boolean.class, props, "realtime", false);
		tu = remove(TimeUnit.class, props, "timeUnit",
				getTaskQueue().getTimeUnit());
		date = remove(Date.class, props, "date", null);
		timeout = remove(Long.class, props, "timeout",
				period > 0 ? period : Long.MAX_VALUE);
		flags = (byte)(
			(callQueue	? 0x01 : 0) |
			(realtime	? 0x04 : 0) |
			(killable	? 0x02 : 0));
	}
	
	@Override
	public final IpmemsStdTaskQueue getTaskQueue() {
		for (IpmemsStdObject o = getParent(); o != null; o = o.getParent())
			if (o instanceof IpmemsStdTaskQueue) return (IpmemsStdTaskQueue)o;
		return null;
	}

	@Override
	public Object get(String k) {
		return (!super.containsKey(k) && getParent() instanceof IpmemsTaskGroup)
				? getParent().get(k) : super.get(k);
	}

	@Override
	public boolean containsKey(String k) {
		return (!super.containsKey(k) && getParent() instanceof IpmemsTaskGroup)
				? getParent().containsKey(k) : super.containsKey(k);
	}
	
	@Override
	public Set<String> getKeys() {
		if (getParent() instanceof IpmemsTaskGroup) {
			Set<String> s = new HashSet<String>(getParent().getKeys());
			s.addAll(getPropertyKeys());
			return s;
		} else return super.getKeys();
	}
	
	@Override
	public long getPeriod() {
		return period;
	}

	@Override
	public long getDelay() {
		return delay;
	}

	@Override
	public boolean isDelayed() {
		return period <= 0L && delay >= 0 && date == null;
	}

	@Override
	public boolean isCallQueue() {
		return (flags & 0x01) != 0;
	}

	@Override
	public boolean isKillable() {
		return (flags & 0x02) != 0;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public TimeUnit getTimeUnit() {
		return tu;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public boolean isPeriodic() {
		return period > 0L;
	}

	@Override
	public boolean isAlarm() {
		return date != null;
	}
	
	@Override
	public boolean isRealtime() {
		return (flags & 0x04) != 0;
	}

	@Override
	public Long call() throws Exception {
		return isCallQueue() ?
				getTaskQueue().call() + super.call() : super.call();
	}

	@Override
	public void start() {
		if (f != null) return;
		if (isAlarm()) {
			if (period <= 0) f = getTaskQueue().getTimer().schedule(
					(Runnable)this,
					date.getTime() - System.currentTimeMillis(),
					TimeUnit.MILLISECONDS);
			else f = getTaskQueue().getTimer().scheduleAtFixedRate(
					this, 
					date.getTime() - System.currentTimeMillis(),
					TimeUnit.MILLISECONDS.convert(period, tu),
					TimeUnit.MILLISECONDS);
		} else if (isPeriodic()) {
			if (isRealtime()) {
				long c = System.currentTimeMillis();
				long p = TimeUnit.MILLISECONDS.convert(period, tu);
				long d = TimeUnit.MILLISECONDS.convert(delay, tu);
				d = d <= 0 ? ((c + p + d) / p) * p - c : ((c + d) / p) * p - c;
				f = getTaskQueue().getTimer().scheduleAtFixedRate(
						this, d, p, TimeUnit.MILLISECONDS);
			} else f = getTaskQueue().getTimer().scheduleWithFixedDelay(
					this, delay, period, tu);
		} else if (isDelayed())
			f = getTaskQueue().getTimer().schedule((Runnable)this, delay, tu);
		if (f != null)
			IpmemsLoggers.fine("sch", "{0} ~ {1} ms", getName(),
					f.getDelay(TimeUnit.MILLISECONDS));
	}

	@Override
	public void stop() {
		if (f != null) {
			f.cancel(false);
			f = null;
		}
		super.stop();
	}

	@Override
	public void run() {
		getTaskQueue().submit(this);
	}
		
	private final long period;
	private final long delay;
	private final byte flags;	
	private final long timeout;
	private final TimeUnit tu;
	private final Date date;
	private volatile ScheduledFuture<?> f = null;
}
