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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.logging.IpmemsLogEventData;
import org.ipsilon.ipmems.logging.IpmemsLogRec;
import org.ipsilon.ipmems.logging.IpmemsLoggersListener;
import org.ipsilon.ipmems.logging.IpmemsRemoteItf;


/**
 * IPMEMS Log Viewer.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsLogViewer extends JFrame implements
		IpmemsLoggersListener, ActionListener,	WindowListener {
	/**
	 * Constructs the IPMEMS Log Viewer.
	 * @param i Remote object.
	 */
	public IpmemsLogViewer(IpmemsRemoteItf i) {
		super(i + ": " + IpmemsIntl.string("IPMEMS Log Viewer"));
		iri = i;
		setIconImage(IpmemsTrayIcon.getImage(new Dimension(16, 16)));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		int fsize = UIManager.getFont("Label.font").getSize();
		monoFont = new Font(Font.MONOSPACED, Font.PLAIN, fsize);
		add(tabPane = new JTabbedPane());
		log = new JTextArea();
		log.setFont(monoFont);
		log.setEditable(false);
		log.setComponentPopupMenu(new JPopupMenu() {{
			add(new AbstractAction(IpmemsIntl.string("Update")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateMainLogText();
				}
			});
		}});
		tabPane.addTab("ipmems.log", new JScrollPane(log));
		addWindowListener((WindowListener)this);
		setPreferredSize(new Dimension(750, 550));
		pack();
		setLocationRelativeTo(null);
	}
	
	private void updateMainLogText() {
		SwingWorker<String,Object> sw = new SwingWorker<String,Object>() {
			@Override
			protected String doInBackground() throws Exception {
				return iri.getMainLogText();
			}

			@Override
			protected void done() {
				try {
					log.setText(get());
				} catch (Exception x) {}
			}
		};
		sw.execute();
	}
	
	@Override
	public void added(final IpmemsLogEventData data) {
		IpmemsSwingUtil.safeInvoke(new Runnable() {
			@Override
			public void run() {
				IpmemsLogList l = new IpmemsLogList(iri, data, monoFont);
				tabPane.addTab(data.getLogger().getKey(), new JScrollPane(l));
			}
		});
	}

	@Override
	public void removed(final IpmemsLogEventData data) {
		IpmemsSwingUtil.safeInvoke(new Runnable() {
			@Override
			public void run() {
				for (int i = 1; i < tabPane.getTabCount(); i++) {
					String k = tabPane.getTitleAt(i);
					if (!k.equals(data.getLogger().getKey())) continue;
					JScrollPane sp = (JScrollPane)tabPane.getComponentAt(i);
					IpmemsLogList l = (IpmemsLogList)sp.getViewport().getView();
					l.getModel().close();
					tabPane.removeTabAt(i);
					break;
				}
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JList l = (JList)e.getSource();
		IpmemsLogRec lr = (IpmemsLogRec)l.getSelectedValue();
		if (lr != null && lr.getThrown() != null) {
			IpmemsErrorViewer v = new IpmemsErrorViewer(
					this, iri, lr.getThrown());
			v.setVisible(true);
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		iri.removeListener(this);
		iri.log(false);
		removeWindowListener(this);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		for (int i = 1; i < tabPane.getTabCount(); i++) {
			JScrollPane sp = (JScrollPane)tabPane.getComponentAt(i);
			IpmemsLogList ll = (IpmemsLogList)sp.getViewport().getView();
			ll.getModel().close();
		}
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
		if (first) {
			first = false;
			iri.addListener(this);
			iri.log(true);
			updateMainLogText();
		}
	}

	private JTabbedPane tabPane;
	private JTextArea log;
	private final Font monoFont;
	private boolean first = true;
	private final IpmemsRemoteItf iri;
}