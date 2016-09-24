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

package com.ipsilon.ipmems.rata;

import com.ipsilon.ipmems.rata.data.IpmemsRataRq;
import com.ipsilon.ipmems.rata.data.IpmemsRataRsErrResult;
import com.ipsilon.ipmems.rata.data.IpmemsRataRsResult;
import java.io.Externalizable;
import java.io.IOException;
import org.ipsilon.ipmems.net.IpmemsTcpClient;

/**
 * IPMEMS RATA client interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsRataClient extends IpmemsTcpClient {
	/**
	 * Prints the error.
	 * @param msg Message.
	 * @param t Exception.
	 * @param args Message arguments.
	 */
	public void printError(String msg, Throwable t, Object ... args);
	
	/**
	 * Prints the message.
	 * @param msg Message.
	 * @param args Message arguments.
	 */
	public void printMessage(String msg, Object ... args);
	
	/**
	 * Prints the error result.
	 * @param r Result object.
	 * @param dur Duration.
	 */
	public void print(IpmemsRataRsErrResult r, long dur);
	
	/**
	 * Prints the result.
	 * @param r Result object.
	 * @param dur Duration.
	 */
	public void print(IpmemsRataRsResult r, long dur);
	
	/**
	 * Prints the binary data.
	 * @param data Binary data.
	 */
	public void printBin(byte[] data);
	
	/**
	 * Reads an externalizable.
	 * @param <T> Return type.
	 * @param c Return type class.
	 * @return Value.
	 * @throws IOException An exception.
	 */
	public <T extends Externalizable> T read(Class<T> c) throws Exception;
	
	/**
	 * Writes an request.
	 * @param rq A request.
	 * @throws IOException An exception.
	 */
	public void write(IpmemsRataRq rq) throws IOException;
	
	/**
	 * Closes the client streams.
	 */
	@Override
	public void close();
	
	/**
	 * Checks whether the connection is gzipped.
	 * @return Gzipped state.
	 */
	public boolean isGzipped();
	
	/**
	 * Sets the gzipped state.
	 * @param gz Gzipped state.
	 */
	public void setGzipped(boolean gz);
}
