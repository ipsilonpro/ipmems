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

import com.ipsilon.ipmems.rata.data.IpmemsRataRsAResult;
import com.ipsilon.ipmems.rata.data.IpmemsRataRsErrResult;
import com.ipsilon.ipmems.rata.data.IpmemsRataRsResult;
import com.ipsilon.ipmems.rata.format.IpmemsRataFormatter;
import com.ipsilon.ipmems.rata.format.IpmemsRataHtmlFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import javax.swing.*;
import javax.swing.border.Border;
import org.ipsilon.ipmems.swingmems.IpmemsSwingAction;

/**
 * IPMEMS RATA result pane.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataResultPane extends JPanel implements Scrollable {
	public IpmemsRataResultPane() {
		super(new GridBagLayout());
		int fsize = UIManager.getFont("Label.font").getSize();
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, fsize));
		cstr.fill = GridBagConstraints.HORIZONTAL;
		cstr.gridx = 0;
		cstr.weightx = 1.0;
		cstr.insets = new Insets(0, 0, 5, 0);
		cstr.anchor = GridBagConstraints.LINE_START;
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
		return getFontMetrics(getFont()).getHeight();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
		switch (o) {
			case SwingConstants.VERTICAL:
				return r.height;
			case SwingConstants.HORIZONTAL:
				return r.width;
			default:
				return 1;
		}
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/**
	 * Get the formatter.
	 * @return Formatter.
	 */
	public IpmemsRataFormatter getFormatter() {
		return formatter;
	}
	
	/**
	 * Sets the formatter.
	 * @param f Formatter.
	 */
	public void setFormatter(IpmemsRataFormatter f) {
		formatter = f;
	}

	/**
	* Clears all the component.
	*/
	public void clear() {
		removeAll();
	}
	
	private JPanel resultPanel(IpmemsRataRsAResult r, long dur) {
		JPanel p = new JPanel(new BorderLayout(0, 10));
		JPanel cap = new JPanel(new BorderLayout(10, 0));
		cap.setBackground(SystemColor.controlShadow);
		cap.setBorder(capBorder);
		long d = r.getDuration();
		String t = new Timestamp(System.currentTimeMillis()).toString();
		JLabel ts = new JLabel(t);
		ts.setOpaque(true);
		ts.setBackground(SystemColor.controlDkShadow);
		ts.setForeground(SystemColor.controlLtHighlight);
		ts.setBorder(BorderFactory.createEtchedBorder());
		cap.add(ts, BorderLayout.WEST);
		JLabel c = new JLabel(r.getClassName());
		c.setOpaque(true);
		c.setHorizontalAlignment(SwingConstants.CENTER);
		c.setBackground(SystemColor.controlDkShadow);
		c.setForeground(SystemColor.controlLtHighlight);
		c.setBorder(BorderFactory.createEtchedBorder());
		cap.add(c);
		JLabel ms = new JLabel(String.format("%d | %d | %d", dur, d, dur - d));
		ms.setOpaque(true);
		ms.setBackground(SystemColor.controlDkShadow);
		ms.setForeground(SystemColor.controlLtHighlight);
		ms.setBorder(BorderFactory.createEtchedBorder());
		cap.add(ms, BorderLayout.EAST);
		p.add(cap, BorderLayout.NORTH);
		return p;
	}

	/**
	* Adds a result.
	* @param r Result object.
	* @param dur Full duration.
	*/
	public void addResult(IpmemsRataRsResult r, long dur) {
		JPanel p = resultPanel(r, dur);
		String fstr = formatter.format(r.getObject());
		final JEditorPane tc = new JEditorPane(formatter.getMime(), fstr);
		tc.setFont(getFont());
		tc.setEditable(false);
		JPopupMenu pm = new JPopupMenu();
		pm.add(new IpmemsSwingAction("Copy") {
			@Override
			public void actionPerformed(ActionEvent e) {
				tc.selectAll();
				tc.copy();
				tc.setCaretPosition(0);
			}
		});
		tc.setComponentPopupMenu(pm);
		p.add(tc);
		add(p, cstr);
		add(new JSeparator(SwingConstants.HORIZONTAL), cstr);
	}

	/**
	* Adds an error result.
	* @param r Result object.
	* @param dur Full duration.
	*/
	public void addResult(IpmemsRataRsErrResult r, long dur) {
		JPanel p = resultPanel(r, dur);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		r.getObject().printStackTrace(pw);
		pw.close();
		JTextArea ta = new JTextArea(sw.toString());
		ta.setFont(getFont());
		ta.setEditable(false);
		ta.setLineWrap(true);
		p.add(ta);
		add(p, cstr);
		add(new JSeparator(SwingConstants.HORIZONTAL), cstr);
	}

	/**
	* Adds a text result.
	* @param result Result object.
	*/
	public void addTextResult(Object result) {
		JTextArea ta = new JTextArea(String.valueOf(result));
		ta.setLineWrap(true);
		ta.setEditable(false);
		ta.setFont(getFont());
		add(ta, cstr);
		add(new JSeparator(SwingConstants.HORIZONTAL), cstr);
	}

	/**
	* Adds an error result.
	* @param txt Error text.
	* @param t Error.
	*/
	public void addErrorResult(String txt, Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		JPanel p = new JPanel(new BorderLayout());
		if (!txt.isEmpty()) {
			JTextArea cap = new JTextArea(txt);
			cap.setEditable(false);
			cap.setBackground(SystemColor.control);
			cap.setLineWrap(true);
			cap.setFont(getFont());
			p.add(cap, BorderLayout.NORTH);
		}
		JTextArea ta = new JTextArea(sw.toString());
		ta.setLineWrap(true);
		ta.setEditable(false);
		ta.setFont(getFont());
		p.add(ta);
		add(p, cstr);
		add(new JSeparator(SwingConstants.HORIZONTAL), cstr);
	}

	private volatile IpmemsRataFormatter formatter =
			new IpmemsRataHtmlFormatter();
	private final GridBagConstraints cstr = new GridBagConstraints();
	private final Border capBorder = BorderFactory.createCompoundBorder(
			BorderFactory.createRaisedBevelBorder(),
			BorderFactory.createEmptyBorder(2, 2, 2, 2));
}
