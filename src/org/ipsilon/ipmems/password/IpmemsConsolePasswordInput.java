package org.ipsilon.ipmems.password;

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

import org.ipsilon.ipmems.IpmemsIntl;

/**
 * IPMEMS console password input class.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsConsolePasswordInput extends IpmemsAbstractPasswordInput {	
	/**
	 * Get the password entered from console.
	 * @return Password.
	 */
	@Override
	public char[] getPassword() {
		if (READLINE) {
			System.out.print("Password: ");
			System.out.flush();
			char[] psw = System.console().readLine().toCharArray();
			System.out.println();
			return psw;
		} else {
			if (System.console() == null) return null;
			else return System.console().readPassword(
					IpmemsIntl.string("Password") + ": ");
		}
	}

	/**
	 * Get the user from console.
	 * @return User name.
	 */
	@Override
	public String getUser() {
		if (READLINE) {
			System.out.print("User: ");
			System.out.flush();
			return System.console().readLine();
		} else {
			if (System.console() == null) return "root";
			else return System.console().readLine(
					IpmemsIntl.string("User") + ": ");
		}
	}
		
	/**
	 * READLINE flag.
	 */
	public static boolean READLINE;	
}
