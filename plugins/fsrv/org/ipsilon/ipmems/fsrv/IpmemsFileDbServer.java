package org.ipsilon.ipmems.fsrv;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.ipsilon.ipmems.db.IpmemsDbAbstractServer;

/**
 * IPMEMS file database server.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsFileDbServer extends IpmemsDbAbstractServer {

	@Override
	public boolean isRunning() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void start() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void stop() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isActive() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isClosed() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getProtocol() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getDriverName() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getPort() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Collection<String> getDatabaseNames() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Connection getConnection(String db) {
		return null;
	}

	@Override
	public Set<String> getObjectNames(String db, String type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Driver getDriver() {
		return null;
	}
	
	private LinkedHashSet<String> databases = new LinkedHashSet<String>();
}
