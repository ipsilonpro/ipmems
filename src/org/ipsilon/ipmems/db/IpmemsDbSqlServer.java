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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * IPMEMS SQL-based database server.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsDbSqlServer extends IpmemsDbServer {
	/**
	 * Get the server protocol.
	 * @return Server protocol.
	 */
	public String getProtocol();
	
	/**
	 * Get the JDBC driver name.
	 * @return JDBC driver name.
	 */
	public String getDriverName();
	
	/**
	 * Get the server port.
	 * @return Server port. 
	 */
	public int getPort();
	
	/**
	 * Connects to remote DB.
	 * @param url DB url.
	 * @param p Connection properties.
	 * @return Connection object.
	 * @throws IpmemsDbException An exception.
	 */
	public Connection connect(String url, Properties p) throws SQLException;
		
	/**
	 * Get the driver.
	 * @return DB driver.
	 */
	public Driver getDriver();
	
	@Override
	public IpmemsDbSqlGate gate(String f, String db);
	
	@Override
	public Connection gateObject(String db);
}
