package org.ipsilon.ipmems.swingmems;

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

import java.awt.EventQueue;

/**
 * IPMEMS swing util.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsSwingUtil {
	/**
	 * Invokes the AWT task.
	 * @param r Runnable.
	 */
	public static void invokeLater(Runnable r) {
		if (EventQueue.isDispatchThread()) r.run();
		else EventQueue.invokeLater(r);
	}
	
	/**
	 * Invokes the AWT task and wait.
	 * @param r Runnable.
	 * @throws Exception An exception.
	 */
	public static void invoke(Runnable r) throws Exception {
		if (EventQueue.isDispatchThread()) r.run();
		else EventQueue.invokeAndWait(r);
	}
	
	/**
	 * Invokes the AWT task safely.
	 * @param r Task.
	 */
	public static void safeInvoke(Runnable r) {
		try {
			invoke(r);
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
	}
}
