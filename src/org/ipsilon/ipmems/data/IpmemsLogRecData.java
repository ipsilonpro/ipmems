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
import org.ipsilon.ipmems.logging.IpmemsLogRec;

/**
 * IPMEMS log record data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLogRecData extends IpmemsAbstractData<IpmemsLogRec> {
	@Override
	public void writeExternal(ObjectOutput o) throws IOException {
		o.writeLong(getData().getTimestamp());
		o.writeInt(getData().getLevel());
		if (getData().getMessage() != null) {
			o.writeBoolean(true);
			o.writeUTF(getData().getMessage());
		} else o.writeBoolean(false);
		if (getData().getThrown() != null) {
			o.writeBoolean(true);
			IpmemsThrowableData d = new IpmemsThrowableData();
			d.setData(getData().getThrown());
			o.writeObject(d);
		} else o.writeBoolean(false);
		if (getData().getParams() != null) {
			o.writeBoolean(true);
			o.writeInt(getData().getParams().length);
			for (Object p: getData().getParams())
				o.writeObject(IpmemsDataTypes.wrap(p));
		} else o.writeBoolean(false);
	}

	@Override
	public void readExternal(ObjectInput in) throws 
			IOException, ClassNotFoundException {
		long ts = in.readLong();
		int l = in.readInt();
		String msg = in.readBoolean() ? in.readUTF() : null;
		Throwable t = in.readBoolean() ? 
				((IpmemsThrowableData)in.readObject()).getData() : null;
		Object[] ps = null;
		if (in.readBoolean()) {
			ps = new Object[in.readInt()];
			for (int i = 0; i < ps.length; i++) 
				ps[i] = ((IpmemsData)in.readObject()).getData();
		}
		setData(new IpmemsLogRec(ts, l, msg, t, ps));
	}
}
