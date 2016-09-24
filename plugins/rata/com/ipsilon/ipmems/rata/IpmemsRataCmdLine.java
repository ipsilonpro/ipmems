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

import com.ipsilon.ipmems.rata.data.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import static java.awt.SystemColor.control;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.swingmems.IpmemsAutoPos;
import org.ipsilon.ipmems.swingmems.IpmemsCommandHistory;
import org.ipsilon.ipmems.swingmems.IpmemsSwingUtil;

/**
 * IPMEMS RATA console frame.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataCmdLine extends JInternalFrame implements IpmemsAutoPos {
	/**
	 * Constructs the IPMEMS RATA console frame.
	 * @param c GUI RATA client.
	 */
	public IpmemsRataCmdLine(IpmemsGuiRataClient c) {
		super(IpmemsIntl.string("Commands"), true, true, true, true);
		client = c;
		cmdHistory = new IpmemsCommandHistory();
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		commands = new RSyntaxTextArea();
		scrollPane = new RTextScrollPane(commands, true);
		add(scrollPane);
		firstColor = commands.getBackground();
		commands.setBackground(control);
		commands.setAntiAliasingEnabled(true);
		commands.setAutoIndentEnabled(true);
		commands.setBracketMatchingEnabled(true);
		commands.setCloseCurlyBraces(true);
		commands.setCloseMarkupTags(true);
		commands.setEnabled(false);
		setPreferredSize(new Dimension(750, 300));
		pack();
		commands.registerKeyboardAction(new Accept(), KeyStroke.getKeyStroke(
				"control ENTER"), WHEN_FOCUSED);
		commands.registerKeyboardAction(new Reject(), KeyStroke.getKeyStroke(
				"Escape"), WHEN_FOCUSED);
		commands.registerKeyboardAction(new Next(), KeyStroke.getKeyStroke(
				"control DOWN"), WHEN_FOCUSED);
		commands.registerKeyboardAction(new Previous(), KeyStroke.getKeyStroke(
				"control UP"), WHEN_FOCUSED);
		setVisible(true);
		readHistory();
	}
	
	private void readHistory() {
		File f = new File(System.getProperty("user.home"), "ipmems.cmd");
		if (f.exists() && f.canRead()) try {
			cmdHistory.readFrom(f);
		} catch (Exception x) {
			JOptionPane.showMessageDialog(this, x.getMessage(),
					IpmemsIntl.string("Read history error"),
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void writeHistory() {
		File f = new File(System.getProperty("user.home"), "ipmems.cmd");
		try {
			cmdHistory.writeTo(f);
		} catch (Exception x) {
			JOptionPane.showMessageDialog(this, x.getMessage(),
					IpmemsIntl.string("Write history error"),
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Sets the scripts MIME.
	 * @param mime MIME.
	 */
	public void setMime(final String mime) {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				commands.setSyntaxEditingStyle(mime);
			}
		});
	}

	@Override
	public void pos(JDesktopPane d) {
		setLocation(0, d.getHeight() - getHeight());
		setSize(d.getWidth(), getHeight());
	}

	@Override
	public void dispose() {
		writeHistory();
		super.dispose();
	}
	
	/**
	 * Enables the input.
	 * @param flag Flag.
	 */
	public void enableInput(final boolean flag) {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				commands.setEnabled(flag);
				commands.setBackground(flag ? firstColor : control);
			}
		});
	}
	
	private Color firstColor;
	private RTextScrollPane scrollPane;
	private RSyntaxTextArea commands;
	private IpmemsCommandHistory cmdHistory;
	private final IpmemsGuiRataClient client;
	
	private class Accept implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			commands.setEnabled(false);
			try {
				long s = System.currentTimeMillis();
				IpmemsRataRqCmd c = new IpmemsRataRqCmd(commands.getText());
				client.write(c);
				ResultWorker w = new ResultWorker(s);
				w.execute();
			} catch (Exception x) {
				client.printError("I/O error", x);
			}
		}
	}
	
	private class Reject implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String s = cmdHistory.getInitialSelection();
			if (s != null) commands.setText(s);
		}
	}
	
	private class Next implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (cmdHistory.isEmpty()) return;
			String s = cmdHistory.nextRecord(commands.getText());
			if (s != null) commands.setText(s);
		}
	}
	
	private class Previous implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (cmdHistory.isEmpty()) return;
			String s = cmdHistory.previousRecord(commands.getText());
			if (s != null) commands.setText(s);
		}
	}
	
	private class ResultWorker extends SwingWorker<IpmemsRataRs,Object> {
		public ResultWorker(long s) {
			start = s;
		}
		
		@Override
		protected IpmemsRataRs doInBackground() throws Exception {
			return client.read();
		}

		@Override
		protected void done() {
			try {
				IpmemsRataRs r = get();
				long d = System.currentTimeMillis() - start;
				if (r instanceof IpmemsRataRsErrResult)
					client.print((IpmemsRataRsErrResult)r, d);
				else if (r instanceof IpmemsRataRsResult)
					client.print((IpmemsRataRsResult)r, d);
				else if (r instanceof IpmemsRataRsErr)
					client.printError("Remote error",
							((IpmemsRataRsErr)r).getThrown());
			} catch (Exception x) {
				client.printError("Result error", x);
			} finally {
				cmdHistory.putRecord(commands.getText());
				commands.setText("");
				commands.setEnabled(true);
			}
		}
		
		private long start;
	}
}