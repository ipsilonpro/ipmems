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

package com.ipsilon.ipmems.rcli;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.IpmemsLib;
import org.ipsilon.ipmems.io.IpmemsIOLib;
import org.ipsilon.ipmems.swingmems.*;

/**
 * IPMEMS RCLI GUI client.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsGuiRcliClient extends IpmemsAbstractRcliClient {
	/**
	 * Default constructor.
	 */
	public IpmemsGuiRcliClient() {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the GUI RCLI client.
	 * @param ps Properties.
	 */
	public IpmemsGuiRcliClient(Map ps) {
		super(ps);
	}
	
	@Override
	public boolean connect() {
		try {
			IpmemsSwingUtil.invoke(new Runnable() {
				@Override
				public void run() {
					frm = new ClientFrame(IpmemsGuiRcliClient.this.toString());
					frm.setVisible(true);
				}
			});
		} catch (Exception x) {
			return false;
		}
		if (!super.connect()) return false;
		try {
			IpmemsGuiPasswordInput pi = new IpmemsGuiPasswordInput();
			String user = pi.getUser();
			char[] password = pi.getPassword();
			printStream.println(false);
			printStream.println(user);
			printStream.println(password);
			printMessage("Authentication completed on {0}", this);
			frm.enableInput();
			return true;
		} catch (Exception x) {
			printError("Authentication error on {0}", x, this);
			return false;
		}
	}

	@Override
	public void start() {
		final char d = (char)0x0C;
		final char[] t;
		try {
			t = IpmemsIOLib.next(reader, d);
		} catch (Exception x) {
			printError("Unable to read the dialog title: {0}", x);
			return;
		}
		final String[] mimes;
		try {
			mimes = String.valueOf(IpmemsIOLib.next(reader, d)).split("\\n");
		} catch (Exception x) {
			printError("Unable to read the MIME types: {0}", x);
			return;
		}
		final char[] msg;
		try {
			msg = IpmemsIOLib.next(reader, d);
		} catch (Exception x) {
			printError("Unable to read the message: {0}", x);
			return;
		}
		try {
			IpmemsSwingUtil.invoke(new Runnable() {
				@Override
				public void run() {
					frm.setMime(String.valueOf(t), String.valueOf(msg), mimes);
				}
			});
		} catch (Exception x) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						int c = reader.read();
						if (c < 0) break;
						if (c == d) printLine(); else appendChar((char)c);
					}
				} catch (Exception x) {
					printError("I/O exception", x);
				} finally {
					printMessage("Input stream has closed by remote side");
					freezeCmd();
				}
			}
		}).start();
	};
	
	@Override
	public void printMessage(final String msg, final Object... args) {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				frm.printMessage(msg, args);
			}
		});
	}

	@Override
	public void printError(
			final String msg, final Throwable t, final Object ... args) {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				frm.printError(msg, t, args);
			}
		});
	}
	
	private void printLine() {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				frm.printLine();
			}
		});
	}
	
	private void appendChar(final char c) {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				frm.appendChar(c);
			}
		});
	}
	
	private void freezeCmd() {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				frm.freezeCommandArea();
			}
		});
	}

	@Override
	public String getKey() {
		return "gui";
	}
				
	private ClientFrame frm;
			
	private class ClientFrame extends 
			JFrame implements Highlighter.HighlightPainter {
		public ClientFrame(String l) {
			super(l + " RCLI " + IpmemsLib.getFullVersion());
			int fsize = UIManager.getFont("Label.font").getSize();
			setIconImage(IpmemsTrayIcon.getImage(new Dimension(16, 16)));
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			cmdHistory = new IpmemsCommandHistory();
			trackPosition = true;
			results = new JTextArea();
			results.setEditable(false);
			results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fsize));
			results.setLineWrap(true);
			results.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					boolean lb = SwingUtilities.isLeftMouseButton(e);
					if (lb && e.getClickCount() == 2) {
						trackPosition = !trackPosition;
						results.setBackground(trackPosition ?
								SystemColor.controlLtHighlight : 
								SystemColor.info);
					}
				}
			});
			taCommands = new RSyntaxTextArea();
			taCommands.setEnabled(false);
			taCommands.setAntiAliasingEnabled(true);
			taCommands.setCloseCurlyBraces(true);
			taCommands.setPaintTabLines(true);
			taCommands.setTabsEmulated(false);
			taCommands.setFont(results.getFont());
			RTextScrollPane csp = new RTextScrollPane(taCommands, true);
			scrollPane = new JScrollPane(results, 
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			splitPane = new JSplitPane(
					JSplitPane.VERTICAL_SPLIT, scrollPane, csp);
			add(splitPane);
			setPreferredSize(new Dimension(800, 600));
			pack();
			taCommands.requestFocusInWindow();
			setLocationRelativeTo(null);
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					splitPane.setDividerLocation(2.0f / 3.0f);
				}
			});
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
				}
				
				@Override
				public void windowClosed(WindowEvent e) {
					try {
						cmdHistory.writeTo(new File(
								System.getProperty("user.home"), "ipmems.cmd"));
					} catch (Exception x) {}
					try {
						close();
					} catch (Exception x) {
					}
				}
			});
			taCommands.getActionMap().put("ctrlEnter", enterAction);
			taCommands.getInputMap().put(KeyStroke.getKeyStroke(
					"control ENTER"), "ctrlEnter");
			taCommands.getActionMap().put("ctrlUp", prevAction);
			taCommands.getInputMap().put(KeyStroke.getKeyStroke(
					"control UP"), "ctrlUp");
			taCommands.getActionMap().put("ctrlDown", nextAction);
			taCommands.getInputMap().put(KeyStroke.getKeyStroke(
					"control DOWN"), "ctrlDown");
			taCommands.getActionMap().put("escape", cancelAction);
			taCommands.getInputMap().put(KeyStroke.getKeyStroke(
					"ESCAPE"), "escape");
			try {
				File ch = new File(
						System.getProperty("user.home"), "ipmems.cmd");
				if (ch.exists() && ch.canRead()) cmdHistory.readFrom(ch);
			} catch (Exception x) {
				printError("Unable to load the command history: {0}", x);
			}
		}

		@Override
		public void paint(Graphics g, int s, int e, Shape b, JTextComponent c) {
			try {
				Rectangle r = c.getUI().modelToView(c, s);
				int h = (r.height - 4) / 2;
				g.setColor(SystemColor.control);
				int x = r.x;
				int w = c.getWidth();
				g.fill3DRect(x, r.y + h, w, r.height - h * 2, true);
			} catch (Exception x) {
				x.printStackTrace(System.err);
			}
		}
		
		private void printLine() {
			results.append("\n \n");
			int end = results.getDocument().getEndPosition().getOffset() - 2;
			int start = end - 1;
			try {
				results.getHighlighter().addHighlight(start, end, this);
			} catch (Exception x) {
				x.printStackTrace(System.err);
			}
			if (trackPosition) track();
		}
		
		private void appendResult(String text) {
			results.append(text);
		}
				
		private void appendErrorResult(String text) {
			results.append(text);
		}
		
		private void freezeCommandArea() {
			taCommands.setEnabled(false);
			taCommands.setBackground(SystemColor.control);
		}

		private void printMessage(String msg, Object... args) {
			appendResult(IpmemsIntl.message(msg, args));
			printLine();
			if (trackPosition) track();
		}
		
		private void printError(String msg, Throwable t, Object ... args) {
			appendErrorResult(IpmemsIntl.message(msg, args));
			if (t != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				pw.close();
				appendResult(sw.toString());
			}
			printLine();
			if (trackPosition) track();
		}
				
		private void appendChar(char c) {
			results.append(Character.toString(c));
			if (c == '\n' && isTrackPosition()) track();
		}
				
		private void setMime(String t, String m, String[] ms) {
			if (ms.length == 0) return;
			String mime = null;
			if (ms.length == 1) mime = ms[0]; else {
				int r = JOptionPane.showOptionDialog(this, m, t, 
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						new ImageIcon(getIconImage()), ms, ms[0]);
				if (r >= 0) mime = ms[r];
			}
			if (mime != null) {
				printStream.print(mime);
				printStream.print((char)0x0C);
				printStream.flush();
				taCommands.setSyntaxEditingStyle(mime);
			}
		}
				
		private void enableInput() {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					taCommands.setEnabled(true);
					setTitle(IpmemsGuiRcliClient.this.toString());
				}
			});
		}
		
		private void track() {
			JScrollBar vsb = scrollPane.getVerticalScrollBar();
			vsb.setValue(vsb.getMaximum());
		}

		private boolean isTrackPosition() {
			return trackPosition;
		}
		
		private boolean trackPosition;		
		private JTextArea results;
		private RSyntaxTextArea taCommands;
		private JScrollPane scrollPane;
		private JSplitPane splitPane;
		private IpmemsCommandHistory cmdHistory;
				
		private final Action enterAction = new IpmemsSwingAction("Accept") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					printStream.print(taCommands.getText());
					printStream.print((char)0x0C);
					printStream.flush();
				} catch (Exception x) {
					printMessage("I/O error", x);
				}
				cmdHistory.putRecord(taCommands.getText());
				taCommands.setText("");
			}
		};
		
		private final Action prevAction = new IpmemsSwingAction("Previous") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cmdHistory.isEmpty()) return;
				String s = cmdHistory.previousRecord(taCommands.getText());
				if (s != null) taCommands.setText(s);
			}
		};
		
		private final Action nextAction = new IpmemsSwingAction("Next") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cmdHistory.isEmpty()) return;
				String s = cmdHistory.nextRecord(taCommands.getText());
				if (s != null) taCommands.setText(s);
			}
		};
		
		private final Action cancelAction = new IpmemsSwingAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = cmdHistory.getInitialSelection();
				if (s != null) taCommands.setText(s);
			}
		};
	}
}
