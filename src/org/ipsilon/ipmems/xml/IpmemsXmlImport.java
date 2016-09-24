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
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 * IPMEMS XML import interface.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsXmlImport {
	/**
	 * Reads the XML contents from a file.
	 * @param source Source file.
	 * @throws IOException An I/O exception.
	 * @throws IpmemsParseException Parse XML exception.
	 */
	public void readXml(File source) throws IOException, IpmemsParseException;
	
	/**
	 * Reads the XML contents from an URL.
	 * @param source Source URL.
	 * @throws IOException An I/O exception.
	 * @throws IpmemsParseException Parse XML exception.
	 */
	public void readXml(URL source) throws IOException, IpmemsParseException;
	
	/**
	 * Reads the XML contents from an input stream.
	 * @param i Source input stream.
	 * @throws IOException An I/O exception.
	 * @throws IpmemsParseException Parse XML exception.
	 */
	public void readXml(InputStream i) throws IOException, IpmemsParseException;
	
	/**
	 * Reads the XML contents from a reader.
	 * @param reader Source reader.
	 * @throws IOException An I/O exception.
	 * @throws IpmemsParseException Parse XML exception.
	 */
	public void readXml(Reader reader) throws IOException, IpmemsParseException;
	
	/**
	 * Reads the XML contents from string.
	 * @param source Source string.
	 * @throws IpmemsParseException Parse XML exception. 
	 */
	public void readXml(String source) throws IpmemsParseException;
}
