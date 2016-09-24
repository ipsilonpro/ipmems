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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.Externalizable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * IPMEMS default data type map.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsDataTypes {
	/**
	 * Get the IPMEMS data type by object value.
	 * @param v Object value.
	 * @return IPMEMS data type.
	 */
	public static Class<? extends IpmemsData<?>> getType(Object v) {
		if (v == null) return IpmemsVoidData.class;
		else if (v instanceof String) return IpmemsStringData.class;
		else if (v instanceof Integer) return IpmemsIntData.class;
		else if (v instanceof Double) return IpmemsDoubleData.class;
		else if (v instanceof Long) return IpmemsLongData.class;
		else if (v instanceof Boolean) return IpmemsBoolData.class;
		else if (v instanceof Short) return IpmemsShortData.class;
		else if (v instanceof Byte) return IpmemsByteData.class;
		else if (v instanceof Character) return IpmemsCharData.class;
		else if (v instanceof Float) return IpmemsFloatData.class;
		else if (v instanceof Calendar) return IpmemsCalendarData.class;
		else if (v instanceof Timestamp) return IpmemsSqlTimestampData.class;
		else if (v instanceof java.sql.Date) return IpmemsSqlDateData.class;
		else if (v instanceof Time) return IpmemsSqlTimeData.class;
		else if (v instanceof Date) return IpmemsDateData.class;
		else if (v instanceof BigInteger) return IpmemsBigintData.class;
		else if (v instanceof BigDecimal) return IpmemsBigDecimalData.class;
		else if (v instanceof Collection) return IpmemsCollectionData.class;
		else if (v instanceof Properties) return IpmemsPropData.class;
		else if (v instanceof Map) return IpmemsMapData.class;
		else if (v instanceof byte[]) return IpmemsBytesData.class;
		else if (v instanceof char[]) return IpmemsCharsData.class;
		else if (v.getClass().isArray()) return IpmemsArrayData.class;
		else if (v instanceof Locale) return IpmemsLocaleData.class;
		else if (v instanceof Externalizable) return IpmemsExtData.class;
		else if (v instanceof Class) return IpmemsClassData.class;
		else return IpmemsMiscData.class;
	}
	
	/**
	 * Wraps the value into IPMEMS data object.
	 * @param value Java value.
	 * @return Wrapped value.
	 */
	@SuppressWarnings("unchecked")
	public static IpmemsData<?> wrap(Object value) {
		try {
			IpmemsData v = getType(value).newInstance();
			v.setData(value);
			return v;
		} catch (Exception x) {
			throw new IllegalArgumentException(x);
		}
	}
}
