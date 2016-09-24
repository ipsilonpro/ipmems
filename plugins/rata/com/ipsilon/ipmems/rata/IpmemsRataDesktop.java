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

import java.awt.Component;
import java.awt.SystemColor;
import javax.swing.JDesktopPane;
import org.ipsilon.ipmems.swingmems.IpmemsAutoPos;

/**
 * IPMEMS RATA desktop.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataDesktop extends JDesktopPane {
	/**
	 * Constructs the RATA desktop.
	 */
	public IpmemsRataDesktop() {
		setBackground(SystemColor.desktop);
	}
	
	/**
	 * Aligns the auto-aligned components.
	 */
	public void alignComponents() {
		for (Component c: getComponents()) if (c instanceof IpmemsAutoPos)
			((IpmemsAutoPos)c).pos(this);
	}
}
