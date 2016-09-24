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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;

/**
 * IPMEMS XML export interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsXmlExport {
	/**
	 * Writes the contents to an XML file.
	 * @param target Target XML file.
	 * @throws IOException An I/O exception.
	 * @throws IpmemsMakeException Make XML exception.
	 */
	public void writeXml(File target) throws IOException, IpmemsMakeException;
	
	/**
	 * Writes the contents to a resource by URL.
	 * @param target Target URL.
	 * @throws IOException An I/O exception.
	 * @throws IpmemsMakeException Make XML exception.
	 */
	public void writeXml(URL target) throws IOException, IpmemsMakeException;
	
	/**
	 * Writes the contents to an output stream.
	 * @param target Target output stream.
	 * @throws IOException An I/O exception.
	 * @throws IpmemsMakeException Make XML exception.
	 */
	public void writeXml(OutputStream target)
			throws IOException, IpmemsMakeException;
	
	/**
	 * Writes the contents to a writer.
	 * @param target Target writer.
	 * @throws IOException An I/O exception.
	 * @throws IpmemsMakeException Make XML exception.
	 */
	public void writeXml(Writer target) throws IOException, IpmemsMakeException;
	
	/**
	 * Writes the contents into a string.
	 * @return XML as string.
	 * @throws IpmemsMakeException Make XML exception.
	 */
	public String getXml() throws IpmemsMakeException;
}
