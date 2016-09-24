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

package com.ipsilon.ipmems;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.*;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.password.IpmemsAbstractPasswordInput;
import org.ipsilon.ipmems.password.IpmemsPasswordInput;

/**
 * IPMEMS SSL utilities.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsSslUtil {
	/**
	 * Get the SSL context.
	 * @return SSL context.
	 * @throws NoSuchAlgorithmException No such algorithm exception.
	 */
	public static SSLContext ctx() throws NoSuchAlgorithmException {
		return SSLContext.getDefault();
	}
	
	/**
	 * Get the default server socket factory.
	 * @return Default server socket factory.
	 */
	public static SSLServerSocketFactory ssf() {
		return (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
	}
	
	/**
	 * Get the default socket factory.
	 * @return Default socket factory.
	 */
	public static SSLSocketFactory sf() {
		return (SSLSocketFactory)SSLSocketFactory.getDefault();
	}
	
	static {
		InputStream is = null;
		try {
			IpmemsPasswordInput pi = IpmemsAbstractPasswordInput.getDefault();
			KeyStore s = KeyStore.getInstance(Ipmems.sst("secureKST", "JKS"));
			char[] psw = pi.getPassword();
			String fn = Ipmems.sst("secureKeyStore", "@{jarDir}/ipmems.ss");
			s.load(is = new FileInputStream(fn), psw);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(
					Ipmems.sst("secureTmf", "PKIX"));
			tmf.init(s);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(
					Ipmems.sst("secureKmf", "SunX509"));
			kmf.init(s, psw);
			SSLContext c = SSLContext.getInstance(
					Ipmems.sst("secureSSLCtxType", "TLS"));
			c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLContext.setDefault(c);
		} catch (Exception x) {
			x.printStackTrace(System.err);
		} finally {
			if (is != null) try {is.close();} catch (Exception x) {}
		}
	}		
}
