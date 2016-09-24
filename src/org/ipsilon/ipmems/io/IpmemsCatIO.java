package org.ipsilon.ipmems.io;

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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.util.IpmemsPropertized;

/**
 * IPMEMS UNIX I/O based on 'cat' utility.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsCatIO extends IpmemsPropertized implements IpmemsIO {
	/**
	 * Default constructor.
	 */
	public IpmemsCatIO() {
	}
	
	/**
	 * Constructs the Unix I/O object.
	 * @param ps I/O properties.
	 */
	public IpmemsCatIO(Map ps) {
		super(ps);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void connect() throws IOException {
		File f = new File(Ipmems.JAR_DIR, "ipEcho.sh");
		if (!f.exists()) {
			IpmemsIOLib.setText(f,
					get(String.class, "echoCode", "#!/bin/sh\ncat > $1"));
			f.setExecutable(true);
		}
		if (containsKey("stty")) {
			ArrayList<String> args = new ArrayList<String>();
			args.add("stty");
			args.add("-F");
			args.add(getFile());
			args.addAll(get(List.class, "stty"));
			ProcessBuilder stty = new ProcessBuilder(args);
			Process p = stty.start();
			try {
				p.waitFor();
			} catch (InterruptedException x) {
				throw new InterruptedIOException(x.getMessage());
			}
		}
		eargs.set(1, getFile());
		echoProcess = new ProcessBuilder(eargs).start();
		cargs.set(1, getFile());
		catProcess = new ProcessBuilder(cargs).start();
		input = catProcess.getInputStream();
		output = echoProcess.getOutputStream();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return input;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return output;
	}

	@Override
	public Object getTransceiver() {
		return this;
	}

	@Override
	public boolean isActive() throws IOException {
		try {
			catProcess.exitValue();
			return false;
		} catch (IllegalThreadStateException x) {
			return true;
		} catch (Exception x) {
			return false;
		}
	}

	@Override
	public void close() throws IOException {
		try {output.close();} catch (Exception x) {}
		try {input.close();} catch (Exception x) {}
		try {catProcess.destroy();} catch (Exception x) {}
		try {echoProcess.destroy();} catch (Exception x) {}
		output = null;
		input = null;
		catProcess = null;
		echoProcess = null;
	}
	
	/**
	 * Get the UNIX file name.
	 * @return UNIX file name.
	 */
	public String getFile() {
		return get(String.class, "file", "/dev/null");
	}
	
	@Override
	public String toString() {
		return getFile();
	}
	
	private OutputStream output;
	private InputStream input;
	private Process catProcess;
	private Process echoProcess;
	private final List<String> cargs = Arrays.asList("cat", null);
	private final List<String> eargs = Arrays.asList("./ipEcho.sh", null);
}
