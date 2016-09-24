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

package com.ipsilon.ipmems.rata.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * IPMEMS log enable/disable request.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataRqLog implements IpmemsRataRq {
	/**
	 * Default constructor.
	 */
	public IpmemsRataRqLog() {
	}
	
	/**
	 * Constructs the log enable data.
	 * @param flag Log enable flag.
	 */
	public IpmemsRataRqLog(boolean flag) {
		enable = flag;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(enable);
	}

	@Override
	public void readExternal(ObjectInput in) throws 
			IOException, ClassNotFoundException {
		enable = in.readBoolean();
	}
	
	/**
	 * Returns the log enabled flag.
	 * @return Log enabled flag.
	 */
	public boolean isEnable() {
		return enable;
	}

	@Override
	public String toString() {
		return enable ? "Enable log" : "Disable log";
	}
	
	private boolean enable;
}
