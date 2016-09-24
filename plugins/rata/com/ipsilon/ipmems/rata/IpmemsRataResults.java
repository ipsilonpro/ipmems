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

import com.ipsilon.ipmems.rata.data.IpmemsRataRsErrResult;
import com.ipsilon.ipmems.rata.data.IpmemsRataRsResult;
import com.ipsilon.ipmems.rata.format.IpmemsRataFormatter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.*;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.swingmems.IpmemsAutoPos;
import org.ipsilon.ipmems.swingmems.IpmemsSwingUtil;

/**
 * IPMEMS RATA results.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsRataResults extends JInternalFrame implements IpmemsAutoPos {
	/**
	 * Default constructor.
	 */
	public IpmemsRataResults() {
		super(IpmemsIntl.string("Results"), true, true, true, true);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
		results = new IpmemsRataResultPane();
		scrollPane = new JScrollPane(results,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);
		setPreferredSize(new Dimension(400, 400));
		pack();
		setVisible(true);
	}
	
	/**
	 * Clears the results pane.
	 */
	public void clear() {
		results.clear();
		scrollPane.validate();
	}

	@Override
	public void pos(JDesktopPane d) {
		Window win = SwingUtilities.windowForComponent(d);
		IpmemsGuiRataFrame f = (IpmemsGuiRataFrame)win;
		int w = d.getWidth() - f.getOutput().getWidth();
		int h = d.getHeight() - f.getCmdLine().getHeight();
		if (w >= 0 && h >= 0) setSize(w, h);
	}
	
	/**
	 * Adds an error result.
	 * @param r RATA error result.
	 * @param dur Duration.
	 */
	public void addResult(final IpmemsRataRsErrResult r, final long dur) {
		track(new Runnable() {
			@Override
			public void run() {
				results.addResult(r, dur);
			}
		});
	}

	/**
	 * Adds a result.
	 * @param r RATA result.
	 * @param dur Duration.
	 */
	public void addResult(final IpmemsRataRsResult r, final long dur) {
		track(new Runnable() {
			@Override
			public void run() {
				results.addResult(r, dur);
			}
		});
	}

	/**
	 * Adds a text result.
	 * @param txt Text.
	 */
	public void addTextResult(final String txt) {
		track(new Runnable() {
			@Override
			public void run() {
				results.addTextResult(txt);
			}
		});
	}

	/**
	 * Adds an error result.
	 * @param txt Text.
	 * @param t Error.
	 */
	public void addErrorResult(final String txt, final Throwable t) {
		track(new Runnable() {
			@Override
			public void run() {
				results.addErrorResult(txt, t);
			}
		});
	}
	
	/**
	 * Sets the formatter.
	 * @param f Formatter.
	 */
	public void setFormatter(IpmemsRataFormatter f) {
		results.setFormatter(f);
	}
	
	/**
	 * Get the current formatter.
	 * @return Current formatter.
	 */
	public IpmemsRataFormatter getFormatter() {
		return results.getFormatter();
	}
			
	private void track(final Runnable r) {
		IpmemsSwingUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				r.run();
				scrollPane.validate();
				JScrollBar sb = scrollPane.getVerticalScrollBar();
				sb.setValue(sb.getMaximum());
			}
		});
	}
	
	private IpmemsRataResultPane results;
	private JScrollPane scrollPane;
}
