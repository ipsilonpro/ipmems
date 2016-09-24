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
import java.util.Locale;

/**
 * IPMEMS locale data.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsLocaleData extends IpmemsAbstractData<Locale> {
	@Override
	public void writeExternal(ObjectOutput o) throws IOException {
		o.writeUTF(getData().getLanguage());
		o.writeBoolean(getData().getCountry() != null);
		if (getData().getCountry() != null) o.writeUTF(getData().getCountry());
		o.writeBoolean(getData().getVariant() != null);
		if (getData().getVariant() != null) o.writeUTF(getData().getVariant());
	}

	@Override
	public void readExternal(ObjectInput in) 
			throws IOException, ClassNotFoundException {
		String language = in.readUTF();
		String country = in.readBoolean() ? in.readUTF() : null;
		String variant = in.readBoolean() ? in.readUTF() : null;
		if (country != null && variant != null)
			setData(new Locale(language, country, variant));
		else if (country != null) setData(new Locale(language, country));
		else setData(new Locale(language));
	}
}
