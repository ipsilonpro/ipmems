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

import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.logging.IpmemsLogRec;

/**
 * IPMEMS log list record finder dialog.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLogListFinder extends JDialog {
	/**
	 * Constructs the log record finder dialog.
	 * @param l Target list.
	 */
	@SuppressWarnings("unchecked")
	public IpmemsLogListFinder(IpmemsLogList l) {
		super(SwingUtilities.getWindowAncestor(l),
				IpmemsIntl.string("IPMEMS log record finder"),
				ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		list = l;
		currentRow = list.getSelectedIndex();
		JLabel patternLabel = new JLabel(IpmemsIntl.string("Pattern") + ":");
		patternBox = new JComboBox(history.toArray(new String[history.size()]));
		patternBox.setEditable(true);
		wholeWordBox = new JCheckBox(IpmemsIntl.string("Whole word"));
		regexpBox = new JCheckBox(IpmemsIntl.string("Regular expression"));
		caseSensitiveBox = new JCheckBox(
				IpmemsIntl.string("Case sensitive"));
		rewindBox = new JCheckBox(
				IpmemsIntl.string("Rewind"), true);
		JButton nextButton = new JButton(nextAction);
		JButton prevButton = new JButton(prevAction);
		JButton resetButton = new JButton(resetAction);
		JButton exitButton = new JButton(exitAction);
		GroupLayout g = new GroupLayout(getContentPane());
		g.setAutoCreateContainerGaps(true);
		g.setAutoCreateGaps(true);
		GroupLayout.ParallelGroup h = g.createParallelGroup()
				.addGroup(g.createSequentialGroup()
					.addComponent(patternLabel).addComponent(patternBox))
				.addComponent(wholeWordBox)
				.addComponent(regexpBox)
				.addComponent(caseSensitiveBox)
				.addComponent(rewindBox)
				.addGroup(g.createSequentialGroup()
					.addComponent(prevButton)
					.addGap(40, 50, Integer.MAX_VALUE)
					.addComponent(nextButton)
					.addGap(40, 50, Integer.MAX_VALUE)
					.addComponent(resetButton)
					.addGap(40, 50, Integer.MAX_VALUE)
					.addComponent(exitButton));
		g.setHorizontalGroup(h);
		GroupLayout.SequentialGroup v = g.createSequentialGroup()
				.addGroup(g.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(patternLabel).addComponent(patternBox))
				.addGap(20, 22, 40)
				.addComponent(wholeWordBox)
				.addComponent(regexpBox)
				.addComponent(caseSensitiveBox)
				.addComponent(rewindBox)
				.addGap(20, 22, Integer.MAX_VALUE)
				.addGroup(g.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(prevButton)
					.addComponent(nextButton)
					.addComponent(resetButton)
					.addComponent(exitButton));
		g.setVerticalGroup(v);
		getContentPane().setLayout(g);
		patternLabel.setLabelFor(patternBox);
		getRootPane().setDefaultButton(nextButton);
		pack();
		getRootPane().registerKeyboardAction(exitAction,
				KeyStroke.getKeyStroke("ESCAPE"), 
				JComboBox.WHEN_IN_FOCUSED_WINDOW);
		setLocationRelativeTo(getOwner());
	}
	
	/**
	 * Find the substring.
	 * @param p Regexp pattern.
	 * @param src Source pattern.
	 * @param s Current string.
	 * @param cs Case sensitive flag.
	 * @return Find result.
	 */
	private boolean find(Pattern p, String src, String s,
			boolean cs, boolean ww) {
		if (p != null && !ww && p.matcher(s).find()) return true;
		else if (p != null && ww && p.matcher(s).matches()) return true;
		else if (cs && ww && s.equals(src)) return true;
		else if (cs && !ww && s.contains(src)) return true;
		else if (!cs && !ww && s.toLowerCase().contains(src.toLowerCase())) 
			return true;
		else if (!cs && ww && s.equalsIgnoreCase(src)) return true;
		else return false;
	}
	
	/**
	 * Find the substring by whole word.
	 * @param p Regexp pattern.
	 * @param src Source pattern.
	 * @param ss Current string words.
	 * @param cs Case sensitive flag.
	 * @return Find result.
	 */
	private boolean find(Pattern p, String src, String[] ss,
			boolean cs, boolean ww) {
		for (String s: ss) if (find(p, src, s.trim(), cs, ww)) return true;
		return false;
	}
	
	/**
	 * Find the pattern between the rows.
	 * @param start Start row (inclusive).
	 * @param end End row (exclusive).
	 * @return Row index.
	 */
	private int findBetweenForward(int start, int end) {
		if (patternBox.getSelectedItem() == null) return -1;
		String pattern = String.valueOf(patternBox.getSelectedItem());
		history.add(pattern);
		Pattern p = null;
		int flags = caseSensitiveBox.isSelected() ? 0 : 
				Pattern.CASE_INSENSITIVE;
		if (regexpBox.isSelected()) p = Pattern.compile(pattern, flags);
		for (int r = start; r < end; r++) {
			IpmemsLogRec lr = list.getModel().getElementAt(r);
			if (wholeWordBox.isSelected()) {
				if (find(p, pattern, lr.toString().split("\\s"), 
						caseSensitiveBox.isSelected(),
						wholeWordBox.isSelected())) return r;
			} else {
				if (find(p, pattern, lr.toString(), 
						caseSensitiveBox.isSelected(),
						wholeWordBox.isSelected()))	return r;
			}
		}
		return -1;
	}
	
	/**
	 * Find the pattern between the rows.
	 * @param start Start row (inclusive).
	 * @param end End row (inclusive).
	 * @return Row index.
	 */
	private int findBetweenBackward(int start, int end) {
		if (patternBox.getSelectedItem() == null) return -1;
		String pattern = String.valueOf(patternBox.getSelectedItem());
		history.add(pattern);
		Pattern p = null;
		int flags = caseSensitiveBox.isSelected() ? 0 : 
				Pattern.CASE_INSENSITIVE;
		if (regexpBox.isSelected()) p = Pattern.compile(pattern, flags);
		for (int r = start; r >= end; r--) {
			IpmemsLogRec lr = list.getModel().getElementAt(r);
			if (wholeWordBox.isSelected()) {
				if (find(p, pattern, lr.toString().split("\\s"), 
						caseSensitiveBox.isSelected(),
						wholeWordBox.isSelected())) return r;
			} else {
				if (find(p, pattern, lr.toString(), 
						caseSensitiveBox.isSelected(),
						wholeWordBox.isSelected()))	return r;
			}
		}
		return -1;
	}
	
	/**
	 * Current row.
	 */
	private int currentRow = -1;
	
	/**
	 * Target list.
	 */
	private final IpmemsLogList list;
	
	/**
	 * Pattern box.
	 */
	private JComboBox patternBox;
	
	/**
	 * Whole word checkbox.
	 */
	private JCheckBox wholeWordBox;
	
	/**
	 * Regexp box.
	 */
	private JCheckBox regexpBox;
	
	/**
	 * Case sensitive box.
	 */
	private JCheckBox caseSensitiveBox;
	
	/**
	 * Rewind box.
	 */
	private JCheckBox rewindBox;
	
	/**
	 * Next action.
	 */
	private Action nextAction = new AbstractAction(
			IpmemsIntl.string("Next")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			int r = findBetweenForward(
					currentRow + 1, list.getModel().getSize());
			if (r >= 0) {
				list.setSelectedIndex(r);
				list.ensureIndexIsVisible(r);
				currentRow = r;
			} else if (rewindBox.isSelected()) {
				currentRow = -1;
				int rr = findBetweenForward(
						currentRow + 1, list.getModel().getSize());
				if (rr >= 0) {
					list.setSelectedIndex(rr);
					list.ensureIndexIsVisible(rr);
					currentRow = rr;
				} else resetAction.actionPerformed(null);
			}
		}
	};
	
	/**
	 * Previous action.
	 */
	private Action prevAction = new AbstractAction(
			IpmemsIntl.string("Previous")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			int r = findBetweenBackward(currentRow - 1, 0);
			if (r >= 0) {
				list.setSelectedIndex(r);
				list.ensureIndexIsVisible(r);
				currentRow = r;
			} else if (rewindBox.isSelected()) {
				currentRow = list.getModel().getSize();
				int rr = findBetweenBackward(currentRow - 1, 0);
				if (rr >= 0) {
					list.setSelectedIndex(rr);
					list.ensureIndexIsVisible(rr);
					currentRow = rr;
				} else resetAction.actionPerformed(null);
			}
		}
	};
	
	/**
	 * Exit dialog action.
	 */
	private Action exitAction = new AbstractAction(
			IpmemsIntl.string("Close")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};
	
	/**
	 * Reset action.
	 */
	private Action resetAction = new AbstractAction(
			IpmemsIntl.string("Reset")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			currentRow = list.getSelectedIndex();
		}
	};
	
	/**
	 * Find pattern history.
	 */
	private static Set<String> history = new LinkedHashSet<String>();
}
