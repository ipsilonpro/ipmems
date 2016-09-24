package org.ipsilon.ipmems.xml;

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

/**
 * IPMEMS parse exception.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsParseException extends RuntimeException {
	/**
	 * Default constructor.
	 */
	public IpmemsParseException() {
		super();
	}
	
	/**
	 * Constructs the parse exception.
	 * @param msg Exception message.
	 */
	public IpmemsParseException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructs the parse exception.
	 * @param msg Exception message.
	 * @param cause Exception cause.
	 */
	public IpmemsParseException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
