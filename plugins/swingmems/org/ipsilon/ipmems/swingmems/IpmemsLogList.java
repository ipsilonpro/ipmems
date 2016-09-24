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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.logging.IpmemsLogEventData;
import org.ipsilon.ipmems.logging.IpmemsLogRec;
import org.ipsilon.ipmems.logging.IpmemsRemoteItf;

/**
 * IPMEMS log list.
 * @author Dmitry Ovchinnikov
 */
public final class IpmemsLogList extends JList implements ActionListener {
	/**
	 * Constructs the log list.
	 * @param e Log extractor.
	 * @param evData Event data.
	 * @param font Log list font.
	 */
	@SuppressWarnings("unchecked")
	public IpmemsLogList(IpmemsRemoteItf e, 
			IpmemsLogEventData evData, final Font font) {
		super(new IpmemsMemoryListModel(evData));
		extractor = e;
		setFont(font);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellRenderer(new IpmemsLogRecordRenderer());
		registerKeyboardAction(IpmemsLogList.this,
				KeyStroke.getKeyStroke("ENTER"),
				JComponent.WHEN_FOCUSED);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && 
						e.getClickCount() == 2) {
					actionPerformed(
							new ActionEvent(IpmemsLogList.this, 0, "select"));
				}
			}
		});
		final Action fa = new IpmemsSwingAction("Find") {
			@Override
			public void actionPerformed(ActionEvent e) {
				IpmemsLogListFinder f = 
						new IpmemsLogListFinder(IpmemsLogList.this);
				f.setVisible(true);
			}
		};
		final Action va = new IpmemsSwingAction("View") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object o = getSelectedValue();
				if (!(o instanceof IpmemsLogRec)) return;
				JTextArea a = new JTextArea(((IpmemsLogRec)o).toString());
				a.setLineWrap(true);
				a.setFont(font);
				JScrollPane scrollPane = new JScrollPane(a,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setPreferredSize(new Dimension(700, 500));
				JDialog dlg = new JDialog(
						SwingUtilities.windowForComponent(IpmemsLogList.this),
						Dialog.ModalityType.APPLICATION_MODAL);
				dlg.setLayout(new BorderLayout());
				dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dlg.add(scrollPane);
				dlg.pack();
				dlg.setLocationRelativeTo(null);
				dlg.setVisible(true);
			}
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
			}
		};
		registerKeyboardAction(va, KeyStroke.getKeyStroke("F3"),
				JComponent.WHEN_FOCUSED);
		setComponentPopupMenu(new JPopupMenu() {{
			add(new IpmemsSwingAction("Clear") {
				@Override
				public void actionPerformed(ActionEvent e) {
					getModel().clear();
				}
			});
			add(va);
			addSeparator();
			add(fa);
			JMenu m = new JMenu(IpmemsIntl.string("Set filter"));
			m.add(getFilterLogAction(Integer.MIN_VALUE));
			m.addSeparator();
			m.add(getFilterLogAction(700));
			m.add(getFilterLogAction(500));
			m.add(getFilterLogAction(400));
			m.add(getFilterLogAction(300));
			m.add(getFilterLogAction(800));
			m.add(getFilterLogAction(900));
			m.add(getFilterLogAction(1000));
			addSeparator();
			add(m);
		}});
		registerKeyboardAction(fa, KeyStroke.getKeyStroke("control F"),
				JComponent.WHEN_FOCUSED);
		registerKeyboardAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearSelection();
			}
		}, KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_FOCUSED);
	}

	@Override
	public IpmemsMemoryListModel getModel() {
		return (IpmemsMemoryListModel)super.getModel();
	}

	@Override
	public final void actionPerformed(ActionEvent e) {
		JList l = (JList)e.getSource();
		IpmemsLogRec lr = (IpmemsLogRec)l.getSelectedValue();
		if (lr != null && lr.getThrown() != null) {
			IpmemsErrorViewer v = new IpmemsErrorViewer(
					SwingUtilities.getWindowAncestor(this),
					extractor, lr.getThrown());
			v.setVisible(true);
		}
	}
	
	private Action getFilterLogAction(final int l) {
		if (l == Integer.MIN_VALUE) return new IpmemsSwingAction("All") {
			@Override
			public void actionPerformed(ActionEvent e) {
				getModel().setFilterLogLevel(l);
			}
		}; else return new AbstractAction(IpmemsLogRec.getLevelName(l),
				IpmemsLogRecordRenderer.getIconByLevel(l)) {
			@Override
			public void actionPerformed(ActionEvent e) {
				getModel().setFilterLogLevel(l);
			}
		};
	}
	
	private IpmemsRemoteItf extractor;
}
