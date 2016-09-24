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

import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.ipsilon.ipmems.logging.IpmemsLogRec;
import org.ipsilon.ipmems.swingmems.images.IpmemsImages;

/**
 * IPMEMS log record renderer.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLogRecordRenderer extends DefaultListCellRenderer {
	/**
	 * Get list cell renderer component.
	 * @param l A list component.
	 * @param v Cell value.
	 * @param i Cell index.
	 * @param s Selected flag.
	 * @param f Focused flag.
	 * @return Cell renderer component.
	 */
	@Override
	public Component getListCellRendererComponent(
			JList l, Object v, int i, boolean s, boolean f) {
		IpmemsLogRec lr = (IpmemsLogRec)v;
		ImageIcon ic = getIconByLevel(lr.getLevel());
		IpmemsLogRecordRenderer c = (IpmemsLogRecordRenderer)
				super.getListCellRendererComponent(l, v, i, s, f);
		if (uFont == null) {
			Map<TextAttribute,Object> m = new HashMap<TextAttribute,Object>();
			m.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
			uFont = l.getFont().deriveFont(m);
		}
		if (ic != null) c.setIcon(ic);
		if (lr.getThrown() != null) c.setFont(uFont);
		return c;
	}
	
	/**
	 * Get the image icon by log level.
	 * @param lev Log level.
	 * @return Icon object.
	 */
	public static ImageIcon getIconByLevel(int lev) {
		switch (lev) {
			case 800: return icons[4];
			case 900: return icons[5];
			case 1000: return icons[6];
			case 500: return icons[2];
			case 400: return icons[1];
			case 300: return icons[0];
			case 700: return icons[3];
			default: return null;
		}
	}
		
	private Font uFont;
	private static final ImageIcon[] icons = {
		IpmemsImages.getIcon("amor.png"),
		IpmemsImages.getIcon("finer.gif"),
		IpmemsImages.getIcon("fine.gif"),
		IpmemsImages.getIcon("config.png"),
		IpmemsImages.getIcon("info.png"),
		IpmemsImages.getIcon("warning.png"),
		IpmemsImages.getIcon("error.png")
	};		
}
