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
package com.github.apetrelli.scafa.http.proxy.sync.connection;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.sync.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.sync.connection.AbstractGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.RunnableStarter;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;

public class AnonymousProxyHttpConnectionFactory extends AbstractGatewayHttpConnectionFactory<ProxyHttpConnection> {

	private HttpRequestManipulator manipulator;

	public AnonymousProxyHttpConnectionFactory(SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, RunnableStarter runnableStarter,
			HostPort proxySocketAddress, String interfaceName, boolean forceIpV4, HttpRequestManipulator manipulator) {
		super(socketFactory, clientProcessorFactory, runnableStarter, proxySocketAddress, interfaceName, forceIpV4);
		this.manipulator = manipulator;
	}

	@Override
	protected ProxyHttpConnection createConnection(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> factory,
			SyncSocket sourceChannel, HttpSyncSocket<HttpRequest> httpSocket, HostPort socketAddress) {
		return new AnonymousProxyHttpConnection(factory, clientProcessorFactory, runnableStarter, sourceChannel,
				httpSocket, socketAddress, manipulator);
	}

}
