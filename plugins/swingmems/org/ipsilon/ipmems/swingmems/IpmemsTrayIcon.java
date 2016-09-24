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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.ipsilon.ipmems.Ipmems;
import org.ipsilon.ipmems.IpmemsInit;
import org.ipsilon.ipmems.IpmemsIntl;
import org.ipsilon.ipmems.IpmemsLib;
import org.ipsilon.ipmems.json.IpmemsJsonInvoke;
import org.ipsilon.ipmems.json.IpmemsJsonUtil;
import org.ipsilon.ipmems.logging.*;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * Tray icon class.
 * @author Dmitry Ovchinnikov, Yury Yeremeyev
 */
public class IpmemsTrayIcon extends IpmemsLogAbstractHandler implements 
		Runnable, ActionListener, IpmemsLoggersListener, IpmemsInit {
	@Override
	public void run() {
		if (!SystemTray.isSupported()) return;
		try {
			SYSTRAY = SystemTray.getSystemTray();
			TRAY_ICON = new TrayIcon(getImage(SYSTRAY.getTrayIconSize()),
					IpmemsLib.getLogo(), getMainMenu());
			TRAY_ICON.setImageAutoSize(true);
			TRAY_ICON.addActionListener(this);
			SYSTRAY.add(TRAY_ICON);
			IpmemsLoggers.addLogListener(this);
		} catch (HeadlessException x) {
		} catch (Exception x) {
			IpmemsLoggers.warning("err", "Tray icon", x);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IpmemsDefaultActions.showLogViewer();
	}

	@Override
	public void start() {
		EventQueue.invokeLater(this);
	}
		
	/**
	 * Get the IPMEMS icon with given color.
	 * @param size Image size.
	 * @param c Given color.
	 * @return IPMEMS image.
	 */
	public static Image getIpmemsImage(Dimension size, Color c) {
		int w = size.width;
		int h = size.height;
		int hw = w / 2 - 1;
		BufferedImage img = new BufferedImage(
				size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(c);
		g.fillRect(0, 0, hw, h);
		g.setColor(Color.BLACK);
		g.fillRect(hw, 0, w, h);
		g.setColor(Color.WHITE);
		g.fillPolygon(new int[] {0, w, hw}, new int[] {0, 0, h / 3}, 3);
		return img;
	}
	
	/**
	 * Get the IPMEMS icon.
	 * @param size Icon size.
	 * @return IPMEMS image.
	 */
	public static Image getImage(Dimension size) {
		return getIpmemsImage(size, IPMEMS_COLOR);
	}
		
	/**
	 * Clears the error status.
	 */
	public static void clearErrorStatus() {
		TRAY_ICON.setImage(getImage(SYSTRAY.getTrayIconSize()));
		lastLevel = 800;
	}
	
	@Override
	public boolean publish(IpmemsLogRec record) {
		if (!super.publish(record)) return false;
		if (record.getLevel() > lastLevel) {
			final int l = record.getLevel();
			lastLevel = record.getLevel();
			IpmemsSwingUtil.invokeLater(new Runnable() {
				@Override
				public void run() {
					switch (l) {
						case 900:
							TRAY_ICON.setImage(getWIcon());
							break;
						case 1000:
							TRAY_ICON.setImage(getSIcon());
							break;
					}
				}
			});
		}
		return true;
	}

	@Override
	public IpmemsLogRec[] publish(IpmemsLogRec[] records) {
		IpmemsLogRec[] rs = super.publish(records);
		int maxLevel = lastLevel;
		for (IpmemsLogRec r: rs)
			if (r.getLevel() > lastLevel && r.getLevel() > maxLevel)
				maxLevel = r.getLevel();
		final int l = maxLevel;
		IpmemsSwingUtil.invokeLater(new Runnable() {
				@Override
				public void run() {
					switch (l) {
						case 900:
							TRAY_ICON.setImage(getWIcon());
							break;
						case 1000:
							TRAY_ICON.setImage(getSIcon());
							break;
					}
				}
			});
		return rs;
	}

	private static Image getWIcon() {
		return wIcon != null ? wIcon : (wIcon = 
				getIpmemsImage(TRAY_ICON.getSize(), Color.YELLOW));
	}

	private static Image getSIcon() {
		return sIcon != null ? sIcon : (sIcon = 
				getIpmemsImage(TRAY_ICON.getSize(), Color.RED));
	}
	
	@SuppressWarnings("unchecked")
	private static PopupMenu getMainMenu() throws Exception {
		PopupMenu w = new PopupMenu();
		w.setFont(UIManager.getFont("TextField.font"));
		File mFile = new File(Ipmems.JAR_DIR, "menu.json");
		if (mFile.isFile() && mFile.canRead()) 
			fillMenu((Map)IpmemsJsonUtil.parse(mFile), w);
		return w;
	}
	
	@SuppressWarnings("unchecked")
	private static void fillMenuItem(String k, Map<String,Object> i, Menu mn) {
		if ("-".equals(i.get("label"))) {
			mn.addSeparator();
			return;
		} 
		String label = IpmemsIntl.string(i.remove("label").toString());
		Object type = i.remove("type");
		final String evar = i.containsKey("evar") ?
				String.valueOf(i.remove("evar")) : null;
		MenuItem mi = "menu".equals(type) ? new Menu(label) {
			@Override
			public boolean isEnabled() {
				return evar == null ? true : IpmemsScriptEngines.has(evar);
			}
		} : new MenuItem(label) {
			@Override
			public boolean isEnabled() {
				return evar == null ? true : IpmemsScriptEngines.has(evar);
			}
		};
		mi.setName(k);
		final String cn = String.valueOf(i.remove("class"));
		final Object[] args = i.containsKey("args") ?
				((Collection)i.remove("args")).toArray() : new Object[0];
		final String mt = String.valueOf(i.remove("method"));
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				try {
					Class<?> c = IpmemsScriptEngines.loadClass(cn);
					IpmemsJsonInvoke.invoke(c, null, mt, args);
				} catch (Exception x) {
					JOptionPane.showMessageDialog(null, x,
							IpmemsIntl.string("Error"),
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		Object style = i.remove("style");
		if ("bold".equals(style))
			mi.setFont(mn.getFont().deriveFont(Font.BOLD));
		else if ("italic".equals(style))
			mi.setFont(mn.getFont().deriveFont(Font.ITALIC));
		mn.add(mi);
		if (mi instanceof Menu)	fillMenu((Map)i.remove("items"), (Menu)mi);
	}
	
	private static void fillMenu(Map<String,Map<String,Object>> data, Menu mn) {
		if (data == null) return;
		for (Map.Entry<String,Map<String,Object>> e: data.entrySet()) {
			fillMenuItem(e.getKey(), e.getValue(), mn);
			e.getValue().clear();
		}
		data.clear();
	}

	@Override
	public void added(IpmemsLogEventData d) {
		d.getLogger().addHandler(this);
	}

	@Override
	public void removed(IpmemsLogEventData d) {
		d.getLogger().removeHandler(this);
	}
		
	/**
	 * System tray.
	 */
	public static SystemTray SYSTRAY;
	
	/**
	 * Tray icon.
	 */
	public static TrayIcon TRAY_ICON;
	
	/**
	 * IPMEMS color.
	 */
	public static Color IPMEMS_COLOR = new Color(170, 212, 0);
		
	/**
	 * Last logging level.
	 */
	private static volatile int lastLevel = 800;
		
	/**
	 * Warning icon.
	 */
	private static Image wIcon = null;
	
	/**
	 * Severe icon.
	 */
	private static Image sIcon = null;
}
