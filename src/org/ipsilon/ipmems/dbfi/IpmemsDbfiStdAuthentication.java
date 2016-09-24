package org.ipsilon.ipmems.dbfi;

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

import org.ipsilon.ipmems.Ipmems;

/**
 * IPMEMS DBFI standard authentication.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDbfiStdAuthentication implements IpmemsDbfiAuthentication {
	@Override
	public boolean auth(String user, String password) {
		if (Ipmems.get("dbfiPassword") == null)
			return Ipmems.get("dbfiUser", "ipmems").equals(user);
		else return Ipmems.get("dbfiPassword", "").equals(password);
	}	
}
