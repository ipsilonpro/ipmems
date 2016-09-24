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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.swingmems.images.IpmemsImages;
import org.ipsilon.ipmems.util.IpmemsAdm;
import org.ipsilon.ipmems.util.IpmemsLocalAdm;

/**
 * IPMEMS administration frame.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsAdmFrame extends JFrame {
	/**
	 * Constructs the administration frame.
	 * @param a Administration object.
	 */
	public IpmemsAdmFrame(IpmemsAdm a) {
		super(IpmemsIntl.string("IPMEMS Administration Window"));
		adm = a;
		mm = adm.getMethodMap();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(IpmemsTrayIcon.getImage(new Dimension(24, 24)));
		getRootPane().setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		setLayout(new BorderLayout());
		add(new JScrollPane(new ObjectList()));
		setPreferredSize(new Dimension(400, 400));
		pack();
		setLocationRelativeTo(null);
	}

	@Override
	public void dispose() {
		mm.clear();
		super.dispose();
	}
	
	/**
	 * Shows the frame.
	 */
	public static void showFrame() {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				IpmemsAdmFrame f = new IpmemsAdmFrame(new IpmemsLocalAdm());
				f.setVisible(true);
			}
		});
	}
		
	private final IpmemsAdm adm;
	private final Map<String,Set<String>> mm;
	
	private class ObjectList extends JList {
		@SuppressWarnings("unchecked")
		public ObjectList() {
			super(mm.keySet().toArray());
			setFont(UIManager.getFont("TextField.font"));
			setFixedCellHeight(getFontMetrics(getFont()).getHeight() + 6);
			setCellRenderer(new ObjectListRenderer());
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (!e.isPopupTrigger()) return;
					int i = locationToIndex(e.getPoint());
					if (i >= 0) setSelectedIndex(i);
					showPopupMenu(e.getPoint());
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (!e.isPopupTrigger()) return;
					int i = locationToIndex(e.getPoint());
					if (i >= 0) setSelectedIndex(i);
					showPopupMenu(e.getPoint());
				}
			});
		}
		
		private void showPopupMenu(Point point) {
			final Object obj = getSelectedValue();
			if (obj == null) return;
			if (!mm.containsKey(obj.toString())) return;
			JPopupMenu menu = new JPopupMenu();
			for (final String m: mm.get(obj.toString())) 
				menu.add(new AbstractAction(m) {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							Object r = adm.invoke(obj.toString(), m);
							if (r == null) return;
							JOptionPane.showMessageDialog(rootPane, r,
									IpmemsIntl.string("Result"),
									JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception x) {
							JOptionPane.showMessageDialog(rootPane,	x,
									IpmemsIntl.string("Error"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			menu.show(this, point.x, point.y);
		}
	}
	
	private class ObjectListRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			DefaultListCellRenderer c = (DefaultListCellRenderer)
					super.getListCellRendererComponent(list, value, index,
						isSelected, cellHasFocus);
			c.setIcon(ic);
			return c;
		}
		
		private final ImageIcon ic = IpmemsImages.getIcon("admitem.png");
	}
}
