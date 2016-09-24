package org.ipsilon.ipmems.db;

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
import java.sql.Connection;

/**
 * IPMEMS SQL gate wrapper.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsDbSqlWrapper implements IpmemsDbGateWrapper {
	/**
	 * Constructs the SQL gate wrapper.
	 * @param c Associated connection.
	 */
	public IpmemsDbSqlWrapper(Connection c) {
		this(c, true);
	}
	
	/**
	 * Constructs the SQL gate wrapper.
	 * @param c Associated connection.
	 * @param f Close flag.
	 */
	public IpmemsDbSqlWrapper(Connection c, boolean f) {
		connection = c;
		mustClose = f;
	}

	@Override
	public Connection getGateObject() {
		return connection;
	}

	@Override
	public void close() throws IOException {
		if (mustClose) try {
			connection.close();
		} catch (Exception x) {
			throw new IOException(x);
		}
	}
	
	private final boolean mustClose;
	private final Connection connection;
}
