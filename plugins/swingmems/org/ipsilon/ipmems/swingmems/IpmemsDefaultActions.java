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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.logging.IpmemsLocalItf;

/**
 *
 * @author IPMEMS default graphical actions.
 */
public class IpmemsDefaultActions {
	/**
	 * Shows the log viewer.
	 */
	public static void showLogViewer() {
		new IpmemsLogViewer(new IpmemsLocalItf()).setVisible(true);
	}
	
	/**
	 * Opens the script.
	 */
	public static void openScript() {
		if (scriptsDirectory == null)
			scriptsDirectory = new File(
					Ipmems.get("scriptsDirectory").toString());
		JFileChooser ch = new JFileChooser(scriptsDirectory);
		ch.setAcceptAllFileFilterUsed(false);
		ch.setFileFilter(new FileNameExtensionFilter("Groovy files", "groovy"));
		ch.setMultiSelectionEnabled(false);
		if (ch.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			scriptsDirectory = ch.getCurrentDirectory();
			IpmemsSimpleEditor e = new IpmemsSimpleEditor(
					ch.getSelectedFile().getAbsolutePath(),
					new IpmemsLocalItf(), 0);
			e.setVisible(true);
		}
	}
	
	/**
	 * Opens the web script.
	 */
	public static void openWebScript() {
		if (webDirectory == null) 
			webDirectory = new File(
					Ipmems.get("webDirectory").toString());
		JFileChooser ch = new JFileChooser(webDirectory);
		ch.setAcceptAllFileFilterUsed(false);
		ch.setFileFilter(new FileNameExtensionFilter("Groovy files", "groovy"));
		ch.setMultiSelectionEnabled(false);
		if (ch.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			webDirectory = ch.getCurrentDirectory();
			IpmemsSimpleEditor e = new IpmemsSimpleEditor(
					ch.getSelectedFile().getAbsolutePath(),
					new IpmemsLocalItf(), 0);
			e.setVisible(true);
		}
	}
			
	/**
	 * Web directory.
	 */
	private static volatile File webDirectory = null;		
	
	/**
	 * Scripts directory.
	 */
	private static volatile File scriptsDirectory = null;
}
