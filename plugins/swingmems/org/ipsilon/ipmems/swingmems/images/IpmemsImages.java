package org.ipsilon.ipmems.swingmems.images;

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

import java.awt.Image;
import javax.swing.ImageIcon;
import org.ipsilon.ipmems.logging.IpmemsLoggers;

/**
 * IPMEMS images.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsImages {
	/**
	 * Get image from name.
	 * @param n Name of an image.
	 * @return Raster image.
	 */
	public static Image getImage(String n) {
		if (n == null) return null; else	try {
			return new ImageIcon(IpmemsImages.class.getResource(n)).getImage();
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "Image error {0}", x, n);
			return null;
		}
	}
	
	/**
	 * Get an icon by name.
	 * @param name Icon name.
	 * @return Icon object.
	 */
	public static ImageIcon getIcon(String name) {
		if (name == null) return null; else	try {
			return new ImageIcon(IpmemsImages.class.getResource(name));
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "Icon error {0}", x, name);
			return null;
		}
	}
}
