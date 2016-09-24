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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.db.IpmemsDbAbstractGate;

/**
 * IPMEMS file database gate.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsDbFileGate extends 
		IpmemsDbAbstractGate<IpmemsDbFileConn> {
	@Override
	public void close() {
	}
	
	/**
	 * Get the gate directory key.
	 * @return Gate directory key.
	 */
	public abstract String getDirKey();
	
	/**
	 * Get the gate directory.
	 * @return Gate directory.
	 */
	public File getDir() {
		return getGateObject().dir(getDirKey());
	}
	
	/**
	 * Get the millisecond from object.
	 * @param o An object.
	 * @return Milliseconds.
	 * @throws Exception An exception.
	 */
	protected long millis(Object o) throws Exception {
		if (o instanceof Date) return ((Date)o).getTime();
		else if (o instanceof Number) return ((Number)o).longValue();
		else return IpmemsIntl.parseIso(String.valueOf(o)).getTime();
	}
	
	/**
	 * Get the date from object.
	 * @param o An object.
	 * @return Date.
	 * @throws Exception An exception.
	 */
	protected Date date(Object o) throws Exception {
		if (o instanceof Date) return (Date)o;
		else if (o instanceof Number) return new Date(((Number)o).longValue());
		else return IpmemsIntl.parseIso(String.valueOf(o));
	}
	
	/**
	 * Calls the user function.
	 * @param rk Result key.
	 * @param c User function.
	 * @return Result.
	 */
	public Map<String,Object> cuf(String rk, Callable<?> c) {
		try {
			return umap(rk, cuf(c));
		} catch (Exception x) {
			return umap("error", x);
		}
	}
	
	/**
	 * Calls an user function.
	 * @param <T> Result type.
	 * @param c User function.
	 * @return Result.
	 * @throws Exception An exception.
	 */
	public <T> T cuf(Callable<T> c) throws Exception {
		return getGateObject().call(getDirKey(), c);
	}
}
