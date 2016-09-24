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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.swingmems.IpmemsSwingAction;
import org.ipsilon.ipmems.swingmems.IpmemsTrayIcon;

/**
 * IPMEMS RATA engine selection dialog.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataEngDialog extends JDialog  {
	/**
	 * Constructs the engine selection dialog.
	 * @param p Parent frame.
	 * @param engs Engine names.
	 */
	@SuppressWarnings("unchecked")
	public IpmemsRataEngDialog(JFrame p, Object[] engs) {
		super(p, IpmemsIntl.string("Engine selection"), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setIconImage(IpmemsTrayIcon.getImage(new Dimension(16, 16)));
		setResizable(false);
		final JList lst = new JList(engs);
		lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lst.setSelectedIndex(0);
		JScrollPane sp = new JScrollPane(lst);
		Action okAction = new IpmemsSwingAction("OK", "accept.png") {
			@Override
			public void actionPerformed(ActionEvent e) {
				valueIndex = lst.getSelectedIndex();
				dispose();
			}
		};
		final Action cAction = new IpmemsSwingAction("Cancel", "cancel.png") {
			@Override
			public void actionPerformed(ActionEvent e) {
				valueIndex = -1;
				dispose();
			}
		};
		JButton ok = new JButton(okAction);
		JButton cancel = new JButton(cAction);
		GroupLayout g = new GroupLayout(getContentPane());
		g.setAutoCreateContainerGaps(true);
		g.setAutoCreateGaps(true);
		GroupLayout.ParallelGroup h = g.createParallelGroup();
		GroupLayout.SequentialGroup v = g.createSequentialGroup();
		h.addComponent(sp);
		h.addGroup(g.createSequentialGroup()
				.addComponent(cancel).addComponent(ok));
		v.addComponent(sp);
		v.addGroup(g.createParallelGroup()
				.addComponent(cancel).addComponent(ok));
		g.setHorizontalGroup(h);
		g.setVerticalGroup(v);
		getContentPane().setLayout(g);
		pack();
		setLocationRelativeTo(p);
		getRootPane().setDefaultButton(ok);
		getRootPane().registerKeyboardAction(cAction,
				KeyStroke.getKeyStroke("ESCAPE"),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				removeWindowListener(this);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				valueIndex = -1;
			}
		});
	}

	/**
	 * Get the selection value index.
	 * @return Selection value index.
	 */
	public int getValueIndex() {
		return valueIndex;
	}
	
	private int valueIndex;
}
