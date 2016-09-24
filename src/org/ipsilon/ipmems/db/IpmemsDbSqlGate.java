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
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * IPMEMS SQL-based database gate.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsDbSqlGate extends IpmemsDbGate<Connection> {
	/**
	 * Prepares the statement.
	 * @param sql SQL string.
	 * @return Prepared statement.
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException;
	
	/**
	 * Prepares the statement.
	 * @param sql SQL string.
	 * @param autoGeneratedKeys Auto generated keys flag.
	 * @return Prepared statement.
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException;
	
	/**
	 * Prepares the statement.
	 * @param sql SQL string.
	 * @param colIndices Column indices.
	 * @return Prepared statement.
	 */
	public PreparedStatement prepareStatement(String sql, int[] colIndices) 
			throws SQLException;
	
	/**
	 * Prepares the statement.
	 * @param sql SQL string.
	 * @param colNames Column names.
	 * @return Prepared statement.
	 */
	public PreparedStatement prepareStatement(String sql, String[] colNames) 
			throws SQLException;
		
	/**
	 * Sets the prepared statement parameters.
	 * @param ps Prepared statement.
	 * @param l Parameters.
	 * @throws Exception An exception.
	 */
	public void set(PreparedStatement ps, Object ... l) throws Exception;
	
	/**
	 * Get the statement result as java object.
	 * @param o A SQL object.
	 * @return Java object.
	 * @throws Exception Any exception.
	 */
	public Object get(Object o) throws Exception;
}