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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.ipsilon.ipmems.password.IpmemsPasswordInput;

/**
 * IPMEMS RATA authentication data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataAuthData implements Externalizable {
	/**
	 * Default constructor.
	 */
	public IpmemsRataAuthData() {
	}
	
	/**
	 * Constructs the RATA authentication data.
	 * @param user User name.
	 * @param psw Password.
	 */
	public IpmemsRataAuthData(String user, char[] psw) {
		name = user;
		password = psw;
	}
	
	/**
	 * Constructs the RATA authentication data.
	 * @param pi Password input.
	 */
	public IpmemsRataAuthData(IpmemsPasswordInput pi) {
		name = pi.getUser();
		password = pi.getPassword();
	}

	@Override
	public void writeExternal(ObjectOutput o) throws IOException {
		o.writeUTF(name);
		o.writeInt(password.length);
		for (char c: password) o.writeChar(c);
	}

	@Override
	public void readExternal(ObjectInput in) throws
			IOException, ClassNotFoundException {
		name = in.readUTF();
		int n = in.readInt();
		password = new char[n];
		for (int i = 0; i < n; i++) password[i] = in.readChar();
	}

	/**
	 * Get the user name.
	 * @return User name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the password.
	 * @return Password.
	 */
	public char[] getPassword() {
		return password;
	}
	
	private String name;
	private char[] password;
}
