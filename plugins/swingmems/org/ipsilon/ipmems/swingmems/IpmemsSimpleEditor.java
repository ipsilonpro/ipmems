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
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.logging.IpmemsRemoteItf;
import org.ipsilon.ipmems.util.IpmemsFile;

/**
 * Groovy simple editor.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public final class IpmemsSimpleEditor extends JFrame {
	/**
	 * Constructs the editor.
	 * @param n File name.
	 * @param e Log extractor.
	 * @param line A line number.
	 */
	public IpmemsSimpleEditor(String n, IpmemsRemoteItf e, final int line) {
		super(e + ": " + n);
		if (getIconImages().isEmpty())
			setIconImage(IpmemsTrayIcon.getImage(new Dimension(16, 16)));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		fName = n;
		extractor = e;
		statusLabel = new JLabel();
		posLabel = new JLabel();
		JPanel stPanel = new JPanel(new BorderLayout());
		stPanel.add(statusLabel);
		stPanel.add(posLabel, BorderLayout.EAST);
		add(stPanel, BorderLayout.SOUTH);
		editor = new RSyntaxTextArea();
		editor.setAntiAliasingEnabled(true);
		add(new RTextScrollPane(editor, true));
		editor.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				StringBuilder sb = new StringBuilder();
				sb.append(editor.getCaretLineNumber() + 1);
				sb.append(':');
				sb.append(editor.getCaretOffsetFromLineStart() + 1);
				posLabel.setText(sb.toString());
			}
		});
		editor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (!getTitle().startsWith("*")) setTitle("*" + getTitle());
				statusLabel.setText("*");
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (!getTitle().startsWith("*")) setTitle("*" + getTitle());
				statusLabel.setText("*");
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				if (!getTitle().startsWith("*")) setTitle("*" + getTitle());
				statusLabel.setText("*");
			}
		});
		String ext = IpmemsFile.getFileExtension(fName);
		editor.setSyntaxEditingStyle(em.containsKey(ext) ? em.get(ext) :
				SyntaxConstants.SYNTAX_STYLE_NONE);
		editor.setCloseCurlyBraces(true);
		editor.setCloseMarkupTags(true);
		editor.setHighlightCurrentLine(true);
		editor.setMarginLineEnabled(true);
		editor.setMarginLinePosition(80);
		editor.setTabsEmulated(false);
		editor.setPaintTabLines(true);
		editor.setTabSize(4);
		editor.getActionMap().put("ctrlS", saveAction);
		editor.getInputMap().put(KeyStroke.getKeyStroke("control S"), "ctrlS");
		load();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if ("*".equals(statusLabel.getText())) {
					int r = JOptionPane.showConfirmDialog(
							IpmemsSimpleEditor.this,
							IpmemsIntl.message(
								"File {0} has modified. Save it?", fName),
							IpmemsIntl.string("Save file dialog"),
							JOptionPane.YES_NO_CANCEL_OPTION);
					if (r == JOptionPane.YES_OPTION) {
						try {
							save();
						} catch (Exception x) {
							JOptionPane.showMessageDialog(
									IpmemsSimpleEditor.this,
									x.getLocalizedMessage(), 
									IpmemsIntl.string("Saving file error"),
									JOptionPane.ERROR_MESSAGE);
						} finally {
							dispose();
						}
					} else if (r == JOptionPane.NO_OPTION) dispose();
				} else dispose();
			}
		});
		setPreferredSize(new Dimension(750, 550));
		pack();
		setLocationRelativeTo(null);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				if (line > 0) gotoLine(line - 1);
				else editor.setCaretPosition(0);
			}
		});
	}
		
	/**
	 * Locates the script file by name.
	 * @param name File name.
	 * @return File object.
	 */
	public static File locateScriptFile(final String name) {
		FilenameFilter f = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String n) {
				return n.equals(name);
			}
		};
		List<File> scripts = IpmemsFile.listFilesRecurse(new File(
				Ipmems.sst("scriptsDirectory", "@{jarDir}/scripts")), f);
		if (scripts.size() > 0) return scripts.get(0);
		List<File> wscripts = IpmemsFile.listFilesRecurse(new File(
				Ipmems.sst("webDirectory", "@{jarDir}/web")), f);
		if (wscripts.size() > 0) return wscripts.get(0);
		return null;
	}
	
	/**
	 * Loads the file.
	 */
	private void load() {
		try {
			String t = extractor.getFileInterface().download(fName);
			if (t == null) {
				statusLabel.setText(
						IpmemsIntl.message("{0} not found", fName));
				return;
			}
			editor.setText(t);
			statusLabel.setText(" ");
			if (getTitle().startsWith("*")) setTitle(getTitle().substring(1));
		} catch (Exception x) {
			statusLabel.setText(x.getLocalizedMessage());
		}
	}
	
	private void save() {
		try {
			extractor.getFileInterface().upload(fName, editor.getText());
			statusLabel.setText(" ");
			if (getTitle().startsWith("*")) setTitle(getTitle().substring(1));
		} catch (Exception x) {
			statusLabel.setText(x.getLocalizedMessage());
		}
	}
	
	private void gotoLine(int line) {
		try {
			editor.setCaretPosition(editor.getLineStartOffset(line));
		} catch (Exception x) {
			statusLabel.setText(x.getLocalizedMessage());
		}
	}
	
	private AbstractAction saveAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			save();
		}
	};
	
	private String fName;
	private JLabel statusLabel;
	private JLabel posLabel;
	private RSyntaxTextArea editor;
	private IpmemsRemoteItf extractor;
	
	private static final Map<String,String> em = new HashMap<String,String>();
	static {
		em.put("groovy", SyntaxConstants.SYNTAX_STYLE_GROOVY);
		em.put("js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		em.put("java", SyntaxConstants.SYNTAX_STYLE_JAVA);
		em.put("clj", SyntaxConstants.SYNTAX_STYLE_CLOJURE);
		em.put("c", SyntaxConstants.SYNTAX_STYLE_C);
		em.put("html", SyntaxConstants.SYNTAX_STYLE_HTML);
		em.put("properties", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
		em.put("php", SyntaxConstants.SYNTAX_STYLE_PHP);
		em.put("css", SyntaxConstants.SYNTAX_STYLE_CSS);
		em.put("cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
		em.put("py", SyntaxConstants.SYNTAX_STYLE_PYTHON);
		em.put("rb", SyntaxConstants.SYNTAX_STYLE_RUBY);
		em.put("xml", SyntaxConstants.SYNTAX_STYLE_XML);
		em.put("bat", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
		em.put("jsp", SyntaxConstants.SYNTAX_STYLE_JSP);
		em.put("sh", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
		em.put("tcl", SyntaxConstants.SYNTAX_STYLE_TCL);
		em.put("scala", SyntaxConstants.SYNTAX_STYLE_SCALA);
		em.put("pl", SyntaxConstants.SYNTAX_STYLE_PERL);
		em.put("lua", SyntaxConstants.SYNTAX_STYLE_LUA);
		em.put("lsp", SyntaxConstants.SYNTAX_STYLE_LISP);
		em.put("f", SyntaxConstants.SYNTAX_STYLE_FORTRAN);
		em.put("sql", SyntaxConstants.SYNTAX_STYLE_SQL);
	}
}
