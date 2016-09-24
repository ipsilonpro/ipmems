package org.ipsilon.ipmems.logging;

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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import static java.util.Calendar.*;
import org.ipsilon.ipmems.IpmemsIntl;

/**
 * IPMEMS log formatter.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsFormatter implements IpmemsFormatterItf {
	@Override
	public void format(Appendable w, IpmemsLogRec r) throws IOException {
		Calendar c = Calendar.getInstance();
		w.append(Integer.toString(c.get(YEAR)));
		w.append('-');
		int f = c.get(MONTH) + 1;
		if (f < 10) w.append('0');
		w.append(Integer.toString(f));
		w.append('-');
		f = c.get(DATE);
		if (f < 10) w.append('0');
		w.append(Integer.toString(f));
		w.append(' ');
		f = c.get(HOUR_OF_DAY);
		if (f < 10) w.append('0');
		w.append(Integer.toString(f));
		w.append(':');
		f = c.get(MINUTE);
		if (f < 10) w.append('0');
		w.append(Integer.toString(f));
		w.append(':');
		f = c.get(SECOND);
		if (f < 10) w.append('0');
		w.append(Integer.toString(f));
		f = c.getTimeZone().getRawOffset() / (60 * 1000);
		w.append(f >= 0 ? '+' : '-');
		if (f < 600) w.append('0');
		w.append(Integer.toString(f / 60));
		f = f % 60;
		if (f < 10) w.append('0');
		w.append(Integer.toString(f));
		w.append(' ');
		w.append(r.getLevelName());
		w.append(' ');
		try {
			String fmt = IpmemsIntl.MB.containsKey(r.getMessage()) ?
					IpmemsIntl.MB.getString(r.getMessage()) : r.getMessage();
			if (r.getParams() == null || r.getParams().length == 0)
				w.append(fmt);
			else IpmemsIntl.append(w, fmt, r.getParams());
		} catch (Exception x) {
			w.append(r.getMessage());
			w.append(Arrays.deepToString(r.getParams()));
		}
		w.append('\n');
		if (r.getThrown() != null) {
			if (w instanceof PrintWriter)
				r.getThrown().printStackTrace((PrintWriter)w);
			else if (w instanceof PrintStream)
				r.getThrown().printStackTrace((PrintStream)w);
			else {
				StringWriter sw = new StringWriter(512);
				PrintWriter pw = new PrintWriter(sw);
				r.getThrown().printStackTrace(pw);
				pw.close();
				w.append(sw.getBuffer());
			}
		}
	}	
}
