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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.swingmems.IpmemsAutoPos;
import org.ipsilon.ipmems.swingmems.IpmemsSwingAction;
import org.ipsilon.ipmems.swingmems.IpmemsSwingUtil;

/**
 * IPMEMS RATA output window.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataOutput extends JInternalFrame implements IpmemsAutoPos {
	/**
	 * Default constructor.
	 */
	public IpmemsRataOutput() {
		super(IpmemsIntl.string("Output"), true, true, true, true);
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		setLayout(new BorderLayout());
		textArea = new JTextArea();
		textArea.setEditable(false);
		oldColor = textArea.getBackground();
		popupMenu = new JPopupMenu();
		popupMenu.add(new IpmemsSwingAction("Clear") {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				boolean b = SwingUtilities.isLeftMouseButton(e);
				if (b && e.getClickCount() == 2) {
					track = !track;
					textArea.setBackground(track ? oldColor : SystemColor.info);
				}
			}
		});
		textArea.setComponentPopupMenu(popupMenu);
		scrollPane = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);
		setPreferredSize(new Dimension(500, 400));
		pack();
		setVisible(true);
	}
	
	/**
	 * Appends the text.
	 * @param txt Text.
	 */
	public void append(String txt) {
		textArea.append(txt);
		track();
	}
	
	/**
	 * Appends the binary data.
	 * @param data Binary data.
	 */
	public void append(byte[] data) {
		try {
			textArea.append(new String(data, "UTF-8"));
		} catch (Exception x) {
		} finally {
			track();
		}
	}
	
	private void track() {
		if (track) IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				JScrollBar sb = scrollPane.getVerticalScrollBar();
				sb.setValue(sb.getMaximum());
			}
		});
	}

	@Override
	public void pos(JDesktopPane d) {
		Window w = SwingUtilities.windowForComponent(d);
		IpmemsGuiRataFrame f = (IpmemsGuiRataFrame)w;
		int x = d.getWidth() - getWidth();
		if (x >= 0) setLocation(x, 0);
		int h = d.getHeight() - f.getCmdLine().getHeight();
		if (h >= 0)	setSize(getWidth(), h);
	}
	
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private JPopupMenu popupMenu;
	private boolean track = true;
	private Color oldColor;
}
