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

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import org.ipsilon.ipmems.swingmems.images.IpmemsImages;

/**
 * IPMEMS RATA GUI client.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsGuiRataFrame extends JFrame implements WindowListener {
	/**
	 * Constructs the GUI RATA frame.
	 * @param c GUI RATA client.
	 */
	public IpmemsGuiRataFrame(IpmemsGuiRataClient c) {
		super(c.toString());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(IpmemsImages.getImage("rata.png"));
		client = c;
		add(desktop = new IpmemsRataDesktop());
		desktop.add(output = new IpmemsRataOutput());
		desktop.add(results = new IpmemsRataResults());
		desktop.add(cmdLine = new IpmemsRataCmdLine(client));
		setJMenuBar(menu = new IpmemsRataMenu(client));
		pack();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		addWindowListener((WindowListener)this);
	}

	@Override
	public void windowActivated(WindowEvent e) {
		if (!aligned) {
			desktop.alignComponents();
			aligned = true;
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
		client.close();
		removeWindowListener(this);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		for (JInternalFrame f: desktop.getAllFrames()) f.dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
	
	/**
	 * Shows the status.
	 * @param text Status text.
	 */
	public void status(final String text) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				setTitle(client.toString() + " [" + text + "]");
			}
		});
	}
	
	/**
	 * Hides the status.
	 */
	public void hideStatus() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				setTitle(client.toString());
			}
		});
	}

	/**
	 * Get the associated desktop.
	 * @return Associated desktop.
	 */
	public IpmemsRataDesktop getDesktop() {
		return desktop;
	}

	/**
	 * Get the output frame.
	 * @return Output frame.
	 */
	public IpmemsRataOutput getOutput() {
		return output;
	}

	/**
	 * Get the command line.
	 * @return Command line.
	 */
	public IpmemsRataCmdLine getCmdLine() {
		return cmdLine;
	}

	/**
	 * Get the results.
	 * @return Results.
	 */
	public IpmemsRataResults getResults() {
		return results;
	}

	/**
	 * Get the RATA menu.
	 * @return RATA menu.
	 */
	public IpmemsRataMenu getMenu() {
		return menu;
	}
		
	private boolean aligned;
	private final IpmemsRataDesktop desktop;
	private final IpmemsRataOutput output;
	private final IpmemsRataCmdLine cmdLine;
	private final IpmemsRataResults results;
	private final IpmemsRataMenu menu;
	private final IpmemsGuiRataClient client;
}
