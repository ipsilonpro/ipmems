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

package com.ipsilon.ipmems.rcli;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * IPMEMS auto RCLI client.
 * @author Dmitry Ovchinnikov
 */
public class IpmemsAutoRcliClient implements IpmemsRcliClient {
	/**
	 * Default constructor.
	 */
	public IpmemsAutoRcliClient() {
		this(Collections.EMPTY_MAP);
	}
	
	/**
	 * Constructs the auto RCLI client.
	 * @param ps Properties.
	 */
	public IpmemsAutoRcliClient(Map ps) {
		client = GraphicsEnvironment.isHeadless() ? 
				new IpmemsConsoleRcliClient(ps) :
				new IpmemsGuiRcliClient(ps);
	}

	@Override
	public boolean connect() throws IOException {
		return client.connect();
	}

	@Override
	public boolean isConnected() {
		return client.isConnected();
	}

	@Override
	public void printMessage(String msg, Object... args) {
		client.printMessage(msg, args);
	}

	@Override
	public void printError(String msg, Throwable t, Object... args) {
		client.printError(msg, t, args);
	}

	@Override
	public void close() throws IOException {
		client.close();			
	}

	@Override
	public void start() {
		client.start();
	}

	@Override
	public Object get(String key) {
		return client.get(key);
	}

	@Override
	public Object getAt(String key) {
		return client.getAt(key);
	}

	@Override
	public Object get(String key, Object def) {
		return client.get(key, def);
	}

	@Override
	public String substituted(String key, String def) {
		return client.substituted(key, def);
	}

	@Override
	public <T> T get(Class<T> cl, String key, T def) {
		return client.get(cl, key, def);
	}

	@Override
	public <T> T get(Class<T> cl, String key) {
		return client.get(cl, key);
	}

	@Override
	public boolean containsKey(String key) {
		return client.containsKey(key);
	}

	@Override
	public Object put(String key, Object v) {
		return client.put(key, v);
	}

	@Override
	public Object putAt(String key, Object value) {
		return client.putAt(key, value);
	}

	@Override
	public boolean isCase(Object obj) {
		return client.isCase(obj);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return client.getPropertyKeys();
	}

	@Override
	public Object removeKey(String key) {
		return client.removeKey(key);
	}

	@Override
	public <T> T removeKey(Class<T> c, String key) {
		return client.removeKey(c, key);
	}

	@Override
	public Object removeKey(String key, Object def) {
		return client.removeKey(key, def);
	}

	@Override
	public <T> T removeKey(Class<T> c, String key, T def) {
		return client.removeKey(c, key, def);
	}

	@Override
	public void clearKeys() {
		client.clearKeys();
	}

	@Override
	public Collection<Object> getPropertyValues() {
		return client.getPropertyValues();
	}

	@Override
	public Map<String, Object> getMap() {
		return client.getMap();
	}

	@Override
	public String getKey() {
		return "auto";
	}

	@Override
	public Object eval(String key) throws Exception {
		return client.eval(key);
	}

	@Override
	public Object eval(String key, Object def) throws Exception {
		return client.eval(key, def);
	}

	@Override
	public <T> T eval(Class<T> c, String key, T def) throws Exception {
		return client.eval(c, key, def);
	}

	@Override
	public Socket getSocket() {
		return client.getSocket();
	}

	@Override
	public void disconnect() throws IOException {
		client.disconnect();
	}

	@Override
	public void init(Object... args) {
		client.init(args);
	}
	
	private final IpmemsRcliClient client;
}
