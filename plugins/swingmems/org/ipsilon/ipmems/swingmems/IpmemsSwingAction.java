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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.swingmems.images.IpmemsImages;

/**
 * IPMEMS swing action.
 * @author Dmitry Ovchinnikov
 */
public abstract class IpmemsSwingAction extends AbstractAction {
	/**
	 * Constructs the action.
	 * @param label Action label.
	 */
	public IpmemsSwingAction(String label) {
		super(IpmemsIntl.string(label));
	}
	
	/**
	 * Constructs the action.
	 * @param label Action label.
	 * @param icon Action icon.
	 */
	public IpmemsSwingAction(String label, String icon) {
		super(IpmemsIntl.string(label), IpmemsImages.getIcon(icon));
	}
	
	/**
	 * Constructs the action.
	 * @param label Action label.
	 * @param icon Action icon.
	 * @param key Action key.
	 */
	public IpmemsSwingAction(String label, String icon, String key) {
		this(label, icon);
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
	}
}
