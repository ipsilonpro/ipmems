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
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.GroupLayout.Alignment;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.password.IpmemsPasswordInput;
import org.ipsilon.ipmems.swingmems.images.IpmemsImages;

/**
 * IPMEMS GUI password input.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsGuiPasswordInput implements IpmemsPasswordInput, Runnable {
	@Override
	public char[] getPassword() {
		if (!initFlag && password == null) try {
			IpmemsSwingUtil.invoke(this);
			initFlag = true;
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
		return password;
	}

	@Override
	public String getUser() {
		if (!initFlag && user == null) try {
			IpmemsSwingUtil.invoke(this);
			initFlag = true;
		} catch (Exception x) {
			throw new IllegalStateException(x);
		}
		return user;
	}

	@Override
	public Object getUserData() {
		return null;
	}

	@Override
	public void setUserData(Object data) {
	}

	@Override
	public void run() {
		new PasswordFrame().setVisible(true);
	}
		
	private char[] password;	
	private String user;
	private boolean initFlag;
		
	private class PasswordFrame extends JDialog {
		public PasswordFrame() {
			super((Window)null, IpmemsIntl.string("Password input dialog"),
					ModalityType.APPLICATION_MODAL);
			setResizable(false);
			setIconImage(IpmemsTrayIcon.getImage(new Dimension(16, 16)));
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			JLabel iconLabel = new JLabel(IpmemsImages.getIcon("password.png"));
			JLabel userLabel = new JLabel(IpmemsIntl.string("User") + ":");
			JLabel passwordLabel = new JLabel(
					IpmemsIntl.string("Password") + ":");
			JButton cancelButton = new JButton(getCancelAction());
			JButton okButton = new JButton(getOkAction());
			userField = new JTextField("ipmems", 40);
			passwordField = new JPasswordField(40);
			GroupLayout g = new GroupLayout(getContentPane());
			g.setAutoCreateContainerGaps(true);
			g.setAutoCreateGaps(true);
			GroupLayout.SequentialGroup v = g.createSequentialGroup();
			v.addGroup(g.createParallelGroup()
					.addComponent(iconLabel)
					.addGroup(g.createSequentialGroup()
						.addGroup(g.createParallelGroup(Alignment.BASELINE)
							.addComponent(userLabel).addComponent(userField))
						.addGroup(g.createParallelGroup(Alignment.BASELINE)
							.addComponent(passwordLabel).addComponent(
								passwordField)))
			);
			v.addContainerGap(32, Integer.MAX_VALUE);
			v.addGroup(g.createParallelGroup()
					.addComponent(cancelButton).addComponent(okButton));
			GroupLayout.ParallelGroup h = g.createParallelGroup();
			h.addGroup(g.createSequentialGroup()
					.addComponent(iconLabel)
					.addGroup(g.createParallelGroup().
						addComponent(userLabel).addComponent(passwordLabel))
					.addGroup(g.createParallelGroup().
						addComponent(userField).addComponent(passwordField))
			);
			h.addGroup(g.createSequentialGroup()
					.addComponent(cancelButton)
					.addContainerGap(32, Integer.MAX_VALUE)
					.addComponent(okButton));
			g.setVerticalGroup(v);
			g.setHorizontalGroup(h);
			getContentPane().setLayout(g);
			getRootPane().setDefaultButton(okButton);
			pack();
			passwordField.requestFocusInWindow();
			setLocationRelativeTo(null);
		}

		private Action getCancelAction() {
			return new IpmemsSwingAction("Cancel", "cancel.png") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			};
		}

		private Action getOkAction() {
			return new IpmemsSwingAction("Accept", "accept.png") {
				@Override
				public void actionPerformed(ActionEvent e) {
					user = userField.getText();
					password = passwordField.getPassword();
					dispose();
				}
			};
		}

		private JPasswordField passwordField;
		private JTextField userField;
	}
}
