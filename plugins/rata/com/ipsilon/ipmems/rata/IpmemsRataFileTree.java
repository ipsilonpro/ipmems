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

import com.ipsilon.ipmems.rata.data.IpmemsRataRqTree;
import com.ipsilon.ipmems.rata.data.IpmemsRataRs;
import com.ipsilon.ipmems.rata.data.IpmemsRataRsErr;
import com.ipsilon.ipmems.rata.data.IpmemsRataRsTree;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.io.IpmemsFileInfo;
import org.ipsilon.ipmems.logging.IpmemsRemoteItf;
import org.ipsilon.ipmems.swingmems.IpmemsSimpleEditor;

/**
 * IPMEMS RATA file tree dialog.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataFileTree extends JDialog {
	/**
	 * Constructs the IPMEMS RATA file tree dialog.
	 * @param w Parent window.
	 * @param c GUI RATA client.
	 */
	public IpmemsRataFileTree(Window w, IpmemsGuiRataClient c) {
		super(w, IpmemsIntl.string("%s File tree", c));
		client = c;
		setLayout(new BorderLayout());
		JLabel l = new JLabel(IpmemsIntl.message("Retrieving data ..."));
		l.setHorizontalAlignment(SwingConstants.CENTER);
		l.setPreferredSize(new Dimension(500, 500));
		add(l);
		pack();
		setLocationRelativeTo(w);
		new FileTreeWorker().execute();
	}
	
	private IpmemsGuiRataClient client;
	
	private class FileTreeWorker extends SwingWorker<IpmemsFileInfo,Object> {
		@Override
		protected IpmemsFileInfo doInBackground() throws Exception {
			client.write(new IpmemsRataRqTree());
			IpmemsRataRs rs = client.read();
			if (rs instanceof IpmemsRataRsErr) {
				client.printError("Tree", ((IpmemsRataRsErr)rs).getThrown());
				return null;
			} else if (rs instanceof IpmemsRataRsTree) {
				return ((IpmemsRataRsTree)rs).getFileInfo();
			} else throw new IllegalStateException(rs.toString());
		}

		@Override
		protected void done() {
			getContentPane().removeAll();
			try {
				JTree tree = new JTree(new FileTreeModel(get())) {
					@Override
					public void startEditingAtPath(TreePath path) {
						Object lpc = path.getLastPathComponent();
						if (!(lpc instanceof IpmemsFileInfo)) return;
						IpmemsFileInfo fi = (IpmemsFileInfo)lpc;
						String n = fi.getPath();
						IpmemsRemoteItf i = client;
						IpmemsSimpleEditor e = new IpmemsSimpleEditor(n, i, 0);
						e.setVisible(true);
					}
				};
				tree.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						JTree t = (JTree)e.getSource();
						if (t.getSelectionPath() == null) return;
						boolean b = SwingUtilities.isLeftMouseButton(e);
						if (b && e.getClickCount() == 2)
							t.startEditingAtPath(t.getSelectionPath());
					}
				});
				getContentPane().add(new JScrollPane(tree));
				getContentPane().validate();
			} catch (Exception x) {
				client.printError("Execution", x);
			} 
		}
	}	
}

class FileTreeModel implements TreeModel {
	public FileTreeModel(IpmemsFileInfo r) {
		root = r;
	}

	@Override
	public IpmemsFileInfo getRoot() {
		return root;
	}

	@Override
	public IpmemsFileInfo getChild(Object parent, int index) {
		return ((IpmemsFileInfo)parent).getChildren().get(index);
	}

	@Override
	public int getChildCount(Object parent) {
		return ((IpmemsFileInfo)parent).getChildren().size();
	}

	@Override
	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return ((IpmemsFileInfo)parent).getChildren().indexOf(child);
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
	}
	
	private final IpmemsFileInfo root;
}
