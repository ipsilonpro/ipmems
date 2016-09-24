package org.ipsilon.ipmems.data;

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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import org.ipsilon.ipmems.scripting.IpmemsScriptEngines;

/**
 * IPMEMS throwable data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsThrowableData extends IpmemsAbstractData<Throwable> {
	@Override
	public void writeExternal(ObjectOutput o) throws IOException {
		o.writeUTF(getData().getClass().getName());
		o.writeBoolean(getData().getMessage() != null);
		if (getData().getMessage() != null)	o.writeUTF(getData().getMessage());
		StackTraceElement[] st = getData().getStackTrace();
		o.writeInt(st.length);
		for (StackTraceElement e: st) {
			o.writeInt(e.getLineNumber());
			o.writeUTF(e.getClassName());
			o.writeUTF(e.getMethodName());
			o.writeBoolean(e.getFileName() != null);
			if (e.getFileName() != null) o.writeUTF(e.getFileName());
		}
		o.writeBoolean(getData().getCause() != null);
		if (getData().getCause() != null) {
			IpmemsThrowableData d = new IpmemsThrowableData();
			d.setData(getData().getCause());
			o.writeObject(d);
		}
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		String className = in.readUTF();
		boolean hasMessage = in.readBoolean();
		String msg = hasMessage ? in.readUTF() : null;
		int n = in.readInt();
		StackTraceElement[] st = new StackTraceElement[n];
		for (int i = 0; i < n; i++) {
			int ln = in.readInt();
			String cn = in.readUTF();
			String mn = in.readUTF();
			boolean hasFilename = in.readBoolean();
			String fn = hasFilename ? in.readUTF() : null;
			st[i] = new StackTraceElement(cn, mn, fn, ln);
		}
		Throwable cause = in.readBoolean() ?
				((IpmemsThrowableData)in.readObject()).getData() : null;
		Throwable t = null;
		try {
			Class<Throwable> ct = IpmemsScriptEngines.loadClass(className);
			try {
				Constructor<Throwable> c = ct.getDeclaredConstructor(
						String.class, Throwable.class);
				t = c.newInstance(msg, cause);
			} catch (Exception xx) {}
			if (t == null) try {
				Constructor<Throwable> c = ct.getDeclaredConstructor(
						String.class);
				t = c.newInstance(msg);
				t.initCause(cause);
			} catch (Exception xx) {}
			if (t == null) try {
				Constructor<Throwable> c = ct.getDeclaredConstructor(
						Throwable.class);
				t = c.newInstance(cause);
			} catch (Exception xx) {}
			if (t == null) try {
				t = ct.newInstance();
				t.initCause(cause);
			} catch (Exception xx) {}
			if (t == null) throw new NullPointerException();
		} catch (Exception x) {
			t = new Throwable(
					className + ": " + (msg == null ? "" : msg), cause);
		}
		t.setStackTrace(st);
		setData(t);
	}
}
