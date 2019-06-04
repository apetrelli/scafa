/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.http.proxy.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnection;

public class NullHttpConnection implements HttpConnection {

	private AsynchronousSocketChannel sourceChannel;

	public NullHttpConnection(AsynchronousSocketChannel sourceChannel) {
		this.sourceChannel = sourceChannel;
	}

	@Override
	public void sendHeader(HttpRequest request) throws IOException {
		// Does nothing
	}

	@Override
	public void connect(HttpConnectRequest request) throws IOException {
		// Does nothing
	}

	@Override
	public void send(ByteBuffer buffer) throws IOException {
		buffer.clear();
	}

	@Override
	public void end() throws IOException {
		ProxyResources.getInstance().sendGenericErrorPage(sourceChannel);
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() throws IOException {
		// Does nothing
	}

}
