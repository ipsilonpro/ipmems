package org.ipsilon.ipmems.swingmems;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

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
 * IPMEMS command history.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsCommandHistory extends ArrayList<String> {
	/**
	 * Constructs the command history.
	 */
	public IpmemsCommandHistory() {
		super(16);
	}
		
	/**
	* Writes the history to file.
	* @param f A file.
	* @throws IOException An I/O exception.
	*/
	public synchronized void writeTo(File f) throws IOException {
		PrintWriter w = null;
		try {
			w = new PrintWriter(f, "UTF-8");
			LinkedList<String> set = new LinkedList<String>(this);
			LinkedList<Integer> toRemoveList = new LinkedList<Integer>();
			for (int i = 0; i < set.size(); i++)
				if (i != set.lastIndexOf(set.get(i))) toRemoveList.add(i);
			for (int i = toRemoveList.size() - 1; i >= 0; i--) 
				set.remove(toRemoveList.get(i).intValue());
			set.remove("");
			for (String line: set) w.println(URLEncoder.encode(line, "UTF-8"));
		} catch (IOException x) {
			throw x;
		} finally {
			if (w != null) try {w.close();} catch (Exception x) {}
		}
	}

	/**
	* Reads the history from file.
	* @param f A file.
	* @throws IOException An I/O exception.
	*/
	public synchronized void readFrom(File f) throws IOException {
		Scanner s = null;
		try {
			s = new Scanner(f, "UTF-8");
			ArrayList<String> l = new ArrayList<String>();
			while (s.hasNextLine())
				l.add(URLDecoder.decode(s.nextLine(), "UTF-8"));
			clear();
			addAll(l);
		} catch (IOException x) {
			throw x;
		} finally {
			if (s != null) try {s.close();} catch (Exception x) {}
		}
		position = size();
	}

	/**
	* Put the history record.
	* @param str History record.
	*/
	public synchronized void putRecord(String str) {
		add(str);
		position = size();
		selection = null;
	}

	/**
	* Get the previous record.
	* @param s Current selection.
	* @return Previous record.
	*/
	public synchronized String previousRecord(String s) {
		if (selection == null) selection = s;
		if (position == 0) return get(position);
		else if (position > 0) return get(--position);
		else return selection;
	}

	/**
	* Get the next record.
	* @param s Current selection.
	* @return Next record.
	*/
	public synchronized String nextRecord(String s) {
		if (selection == null) return null;
		if (position == size()) return selection;
		else if (position < size() - 1) return get(++position);
		else if (position < size()) return get(position++);
		else return selection;
	}

	/**
	* Get the initial selection.
	* @return Initial selection.
	*/
	public synchronized String getInitialSelection() {
		return selection;
	}

	private int position;
	private String selection;
}
