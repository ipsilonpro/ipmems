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

import com.ipsilon.ipmems.rata.format.IpmemsRataFormatter;
import com.ipsilon.ipmems.rata.format.IpmemsRataHtmlFormatter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.ServiceLoader;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.swingmems.IpmemsSwingAction;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS formatter selection dialog.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataFormatterDialog extends JDialog {
	/**
	 * Constructs the RATA formatter dialog.
	 * @param r Results pane.
	 */
	public IpmemsRataFormatterDialog(final IpmemsRataResults r) {
		super(SwingUtilities.windowForComponent(r),
				IpmemsIntl.string("Formatter selection"),
				ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		ButtonGroup bg = new ButtonGroup();
		final JTextField tf = new JTextField(getDefaultClass());
		final FormatterList l = new FormatterList();
		final JScrollPane sp = new JScrollPane(l);
		l.setEnabled(true);
		tf.setEnabled(false);
		final JRadioButton rba = new JRadioButton(
				new IpmemsSwingAction("Standard") {
			@Override
			public void actionPerformed(ActionEvent e) {
				l.setEnabled(true);
				tf.setEnabled(false);
			}
		});
		rba.setSelected(true);
		final JRadioButton rbu = new JRadioButton(
				new IpmemsSwingAction("Custom") {
			@Override
			public void actionPerformed(ActionEvent e) {
				l.setEnabled(false);
				tf.setEnabled(true);
			}
		});
		JButton ok = new JButton(new IpmemsSwingAction("Accept", "accept.png") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (rba.isSelected()) {
					IpmemsRataFormatter f = (IpmemsRataFormatter)
							l.getSelectedValue();
					if (f != null) r.setFormatter(f);
				} else if (rbu.isSelected()) try {
					Class<IpmemsRataFormatter> c =
							IpmemsScriptEngines.loadClass(tf.getText());
					r.setFormatter(c.newInstance());
				} catch (Exception x) {
					JOptionPane.showMessageDialog(rootPane, x,
							IpmemsIntl.string("Error"),
							JOptionPane.ERROR_MESSAGE);
				}
				dispose();
			}
		});
		JButton cn = new JButton(new IpmemsSwingAction("Cancel", "cancel.png") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		bg.add(rba);
		bg.add(rbu);
		GroupLayout g = new GroupLayout(getContentPane());
		g.setAutoCreateContainerGaps(true);
		g.setAutoCreateGaps(true);
		GroupLayout.ParallelGroup h = g.createParallelGroup()
				.addComponent(rba)
				.addComponent(sp)
				.addComponent(rbu)
				.addComponent(tf)
				.addGroup(g.createSequentialGroup()
					.addComponent(cn)
					.addContainerGap(10, Integer.MAX_VALUE)
					.addComponent(ok));
		GroupLayout.SequentialGroup v = g.createSequentialGroup()
				.addComponent(rba)
				.addComponent(sp)
				.addComponent(rbu)
				.addComponent(tf,
					tf.getPreferredSize().height,
					tf.getPreferredSize().height,
					tf.getPreferredSize().height)
				.addContainerGap(10, Integer.MAX_VALUE)
				.addGroup(g.createParallelGroup()
					.addComponent(cn)
					.addComponent(ok));
		g.setHorizontalGroup(h);
		g.setVerticalGroup(v);
		getContentPane().setLayout(g);
		pack();
		setLocationRelativeTo(SwingUtilities.windowForComponent(r));
	}
	
	private String getDefaultClass() {
		return IpmemsRataHtmlFormatter.class.getName();
	}
	
	private IpmemsRataFormatter[] getFormatters() {
		ServiceLoader<IpmemsRataFormatter> sl =	ServiceLoader.load(
				IpmemsRataFormatter.class,
				IpmemsScriptEngines.getDefaultClassLoader());
		ArrayList<IpmemsRataFormatter> l = new ArrayList<IpmemsRataFormatter>();
		for (IpmemsRataFormatter f: sl) l.add(f);
		sl.reload();
		return l.toArray(new IpmemsRataFormatter[l.size()]);
	}
	
	private class FormatterRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
			IpmemsRataFormatter f = (IpmemsRataFormatter)value;
			return super.getListCellRendererComponent(list, f.getName(), index,
					isSelected, cellHasFocus);
		}
	}
	
	@SuppressWarnings("unchecked")
	private class FormatterList extends JList {
		public FormatterList() {
			super(getFormatters());
			setCellRenderer(new FormatterRenderer());
			if (getModel().getSize() > 0) setSelectedIndex(0);
			setPreferredSize(new Dimension(300, 300));
		}
	}
}
