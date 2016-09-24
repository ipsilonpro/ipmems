package org.ipsilon.ipmems.db;

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

import java.util.Set;

/**
 * IPMEMS DB facilities.
 * @author Dmitry Ovchinnikov
 */
public interface IpmemsDbFacilities {
	/**
	 * Sets the DB facility.
	 * @param name Facility name.
	 * @param c Facility class.
	 */
	public void setFacility(String name, Class<? extends IpmemsDbGate> c);
	
	/**
	 * Get the DB facility.
	 * @param name Facility name.
	 * @return Facility class.
	 */
	public Class<? extends IpmemsDbGate> getFacility(String name);
	
	/**
	 * Map store support status.
	 * @param name Facility name.
	 * @return True if the DB server supports the given facility.
	 */
	public boolean supports(String name);
	
	/**
	 * Get the facility set.
	 * @return Facility set.
	 */
	public Set<String> getFacilitySet();
}
