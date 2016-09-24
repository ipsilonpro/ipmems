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

import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.swingmems.IpmemsAdmFrame;
import org.ipsilon.ipmems.swingmems.IpmemsLogViewer;
import org.ipsilon.ipmems.swingmems.IpmemsSwingAction;

/**
 * IPMEMS RATA menu bar.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataMenu extends JMenuBar {
	/**
	 * Constructs the IPMEMS RATA menu.
	 * @param c RATA client.
	 */
	public IpmemsRataMenu(IpmemsGuiRataClient c) {
		client = c;
		add(getSessionMenu());
		add(getToolsMenu());
		add(getResultsMenu());
		add(getWindowsMenu());
	}
	
	private JMenu getSessionMenu() {
		JMenu m = new JMenu(IpmemsIntl.string("Session"));
		IpmemsSwingAction a = new IpmemsSwingAction("String mode", null, "F7") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem i = (JCheckBoxMenuItem)e.getSource();
				client.strMode(i.getState());
			}
		};
		m.add(new JCheckBoxMenuItem(a));
		a = new IpmemsSwingAction("Compressed mode", null, "F8") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem i = (JCheckBoxMenuItem)e.getSource();
				client.gzipped(i.getState());
				client.setGzipped(i.getState());
			}
		};
		m.add(new JCheckBoxMenuItem(a));
		m.addSeparator();
		m.add(new IpmemsSwingAction("Exit", null, "control Q") {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.getFrame().dispose();
			}
		});
		return m;
	}
	
	private JMenu getToolsMenu() {
		final JMenu m = new JMenu(IpmemsIntl.string("Tools"));
		m.add(new IpmemsSwingAction("Log viewer", "logger.png") {
			@Override
			public void actionPerformed(ActionEvent e) {
				IpmemsLogViewer lv = new IpmemsLogViewer(client);
				lv.setVisible(true);
			}
		});
		m.add(new IpmemsSwingAction("Administration window", "admitem.png") {
			@Override
			public void actionPerformed(ActionEvent e) {
				IpmemsAdmFrame f = new IpmemsAdmFrame(client);
				f.setVisible(true);
			}
		});
		m.add(new IpmemsSwingAction("File tree", "tree.png") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Window w = SwingUtilities.windowForComponent(m);
				IpmemsRataFileTree ftd = new IpmemsRataFileTree(w, client);
				ftd.setVisible(true);
			}
		});
		return m;
	}
	
	private JMenu getWindowsMenu() {
		JMenu m = new JMenu(IpmemsIntl.string("Windows"));
		m.add(new IpmemsSwingAction("Output", "console.png", "control O") {
			@Override
			public void actionPerformed(ActionEvent e) {
				IpmemsRataOutput o = client.getFrame().getOutput();
				o.setVisible(!o.isVisible());
			}
		});
		m.add(new IpmemsSwingAction("Results", "results.png", "control R") {
			@Override
			public void actionPerformed(ActionEvent e) {
				IpmemsRataResults r = client.getFrame().getResults();
				r.setVisible(!r.isVisible());
			}
		});
		m.add(new IpmemsSwingAction("Command line", "cmdline.png", "F8") {
			@Override
			public void actionPerformed(ActionEvent e) {
				IpmemsRataCmdLine c = client.getFrame().getCmdLine();
				c.setVisible(!c.isVisible());
			}
		});
		return m;
	}
	
	private JMenu getResultsMenu() {
		JMenu m = new JMenu(IpmemsIntl.string("Results"));
		m.add(new IpmemsSwingAction("Clear", "clear.png", "control N") {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.getFrame().getResults().clear();
			}
		});
		m.addSeparator();
		m.add(new IpmemsSwingAction("Select formatter...") {
			@Override
			public void actionPerformed(ActionEvent e) {
				IpmemsRataResults r = client.getFrame().getResults();
				IpmemsRataFormatterDialog d = new IpmemsRataFormatterDialog(r);
				d.setVisible(true);
			}
		});
		return m;
	}
		
	private final IpmemsGuiRataClient client;
}
