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
package com.github.apetrelli.scafa.async.http.proxy.connection;

import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.gateway.connection.AbstractGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class BasicAuthProxyHttpConnectionFactory extends AbstractGatewayHttpConnectionFactory<ProxyHttpConnection> {

	private String username, password;

	private HttpRequestManipulator manipulator;

	public BasicAuthProxyHttpConnectionFactory(SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory, HostPort proxySocketAddress,
			String interfaceName, boolean forceIpV4, String username, String password,
			HttpRequestManipulator manipulator) {
		super(socketFactory, clientProcessorFactory, proxySocketAddress, interfaceName, forceIpV4);
		this.username = username;
		this.password = password;
		this.manipulator = manipulator;
	}

	@Override
	protected ProxyHttpConnection createConnection(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> factory,
			AsyncSocket sourceChannel, HttpAsyncSocket<HttpRequest> httpSocket, HostPort socketAddress) {
		return new BasicAuthProxyHttpConnection(factory, clientProcessorFactory, sourceChannel, httpSocket,
				socketAddress, manipulator, username, password);
	}
}
