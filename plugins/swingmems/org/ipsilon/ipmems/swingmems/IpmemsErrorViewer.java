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

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.logging.IpmemsLocalItf;
import org.ipsilon.ipmems.logging.IpmemsRemoteItf;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;
import org.ipsilon.ipmems.util.IpmemsFile;

/**
 * IPMEMS error viewer.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsErrorViewer extends JDialog {
	/**
	 * Constructs the error viewer dialog.
	 * @param frm Parent frame.
	 * @param t Any throwable object.
	 */
	public IpmemsErrorViewer(Window frm, IpmemsRemoteItf e, Throwable t) {
		super(frm, IpmemsIntl.string("IPMEMS error viewer"));
		extractor = e;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(t, true);
		appendNodes(root);
		DefaultTreeModel m = new DefaultTreeModel(root);
		final JTree tree = new JTree(m);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.registerKeyboardAction(editAction, 
				KeyStroke.getKeyStroke("ENTER"), JComponent.WHEN_FOCUSED);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && 
						e.getClickCount() == 2)
					editAction.actionPerformed(
							new ActionEvent(tree, 0, "edit"));
			}
		});
		tree.setCellRenderer(new TraceElementRenderer());
		add(new JScrollPane(tree));
		for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
		pack();
		setLocationRelativeTo(frm);
	}
	
	/**
	 * Constructs the error viewer dialog.
	 * @param frm Parent frame.
	 * @param t Any throwable object.
	 */
	public IpmemsErrorViewer(Window frm, Throwable t) {
		this(frm, new IpmemsLocalItf(), t);
	}
	
	/**
	 * Appends the nodes to a complex node.
	 * @param node A node.
	 */
	private void appendNodes(DefaultMutableTreeNode node) {
		if (node.getUserObject() instanceof Throwable) {
			Throwable t = (Throwable)node.getUserObject();
			for (StackTraceElement e: t.getStackTrace())
				node.add(new DefaultMutableTreeNode(e, false));
			if (t.getCause() != null) {
				DefaultMutableTreeNode n = 
						new DefaultMutableTreeNode(t.getCause(), true);
				node.add(n);
				appendNodes(n);
			}
		}
	}
	
	private final IpmemsRemoteItf extractor;
	
	/**
	 * Edit script action.
	 */
	private ActionListener editAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JTree tree = (JTree)e.getSource();
			if (tree.getSelectionPath() != null) {
				DefaultMutableTreeNode n = (DefaultMutableTreeNode)
						tree.getSelectionPath().getLastPathComponent();
				if (n.getUserObject() instanceof StackTraceElement) {
					StackTraceElement el = (StackTraceElement)
							n.getUserObject();
					String fn = el.getFileName();
					if (fn != null) {
						int ln = el.getLineNumber();
						IpmemsSimpleEditor ed = 
								new IpmemsSimpleEditor(fn, extractor, ln);
						ed.setVisible(true);
					}
				}
			}
		}
	};
	
	/**
	 * Stack trace element renderer.
	 */
	class TraceElementRenderer extends DefaultTreeCellRenderer {
		/**
		 * Default constructor.
		 */
		public TraceElementRenderer() {
			underlinedMap.put(
					TextAttribute.UNDERLINE,
					TextAttribute.UNDERLINE_LOW_DOTTED);
		}
		
		/**
		 * Get the tree cell renderer component.
		 * @param t A tree object.
		 * @param v Cell value.
		 * @param s Selected status.
		 * @param e Expanded status.
		 * @param l Leaf status.
		 * @param r Row index.
		 * @param f Focused state.
		 * @return Cell renderer component.
		 */
		@Override
		public Component getTreeCellRendererComponent(
				JTree t, Object v, boolean s, 
				boolean e, boolean l, int r, boolean f) {
			Component c = super.getTreeCellRendererComponent(
					t, v, s, e, l, r, f);
			if (!fontInitialized) {
				fontInitialized = true;
				oldFont = c.getFont();
			}
			DefaultMutableTreeNode n = (DefaultMutableTreeNode)v;
			if (n.getUserObject() instanceof StackTraceElement) {
				StackTraceElement el = (StackTraceElement)n.getUserObject();
				if (oldFont != null) {
					if (IpmemsScriptEngines.getEngineByScriptExt(
							IpmemsFile.getFileExtension(
								el.getFileName())) != null) {
						c.setFont(oldFont.deriveFont(underlinedMap));
						c.validate();
					} else if (el.getClassName() != null && 
							el.getClassName().contains(".ipsilon")) {
						c.setFont(oldFont.deriveFont(Font.ITALIC));
						c.validate();
					} else {
						c.setFont(oldFont);
						c.validate();
					}
				}
			} else if (n.getUserObject() instanceof Throwable) {
				if (oldFont != null) {
					c.setFont(oldFont.deriveFont(Font.BOLD));
					c.validate();
				}
			} else {
				if (oldFont != null) {
					c.setFont(oldFont);
					c.validate();
				}
			}
			return c;
		}
		
		/**
		 * Font initialized flag.
		 */
		private boolean fontInitialized = false;
		
		/**
		 * Old font.
		 */
		private Font oldFont;
		
		/**
		 * Underlined attribute map.
		 */
		private final Map<TextAttribute,Object> underlinedMap = 
				new HashMap<TextAttribute,Object>();
	}
}
