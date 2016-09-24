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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import org.ipsilon.ipmems.io.IpmemsIOLib;

/**
 * IPMEMS logging file handler.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsFileHandler extends IpmemsLogAbstractHandler {
	/**
	 * Constructs the file handler.
	 * @param f Target file.
	 * @param t Formatter.
	 */
	public IpmemsFileHandler(File f, IpmemsFormatterItf t) throws Exception {
		file = f;
		w = new PrintWriter(file, "UTF-8");
		fmt = t;
	}
			
	@Override
	public synchronized boolean publish(IpmemsLogRec record) {
		if (!super.publish(record)) return false; else
		try {
			fmt.format(w, record);
			w.flush();
			return true;
		} catch (Exception x) {
			return false;
		}
	}

	@Override
	public synchronized IpmemsLogRec[] publish(IpmemsLogRec[] records) {
		IpmemsLogRec[] rs = super.publish(records);
		int n = 0;
		IpmemsLogRec[] recs = new IpmemsLogRec[rs.length];
		for (IpmemsLogRec r: rs) try {
			fmt.format(w, r);
			recs[n++] = r;
		} catch (Exception x) {}
		try {
			w.flush();
		} catch (Exception x) {}
		return Arrays.copyOf(recs, n);
	}

	@Override
	public synchronized void close() {
		try {
			w.close();
		} catch (Exception x) {
		}
	}
	
	private void createBak(String base, int index) throws IOException {
		String pfix = index == 0 ? "" : "." + index;
		File bakFile = new File(base + pfix + ".bak");
		if (bakFile.exists()) createBak(base, index + 1);
		IpmemsIOLib.copy(file, bakFile);
	}
	
	/**
	 * Reinitialize the file.
	 * @param bak Bak creation flag.
	 * @throws IOException An I/O exception.
	 */
	public synchronized void reinit(boolean bak) throws IOException {
		close();
		if (bak) createBak(file.getAbsolutePath(), 0);
		w = new PrintWriter(file, "UTF-8");
	}
	
	private final File file;
	private PrintWriter w;
	private final IpmemsFormatterItf fmt;
}
