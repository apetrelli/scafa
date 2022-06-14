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
import com.github.apetrelli.scafa.async.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.async.http.socket.direct.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectHttpConnectionFactory implements GatewayHttpConnectionFactory<ProxyHttpConnection> {

	private final SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory;

	private final DataSenderFactory dataSenderFactory;
	
	private final ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory;

	private final String interfaceName;

	private final boolean forceIpV4;

	public DirectHttpConnectionFactory(SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory) {
		this(socketFactory, dataSenderFactory, clientProcessorFactory, null, false);
	}

	@Override
	public ProxyHttpConnection create(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> factory, AsyncSocket sourceChannel,
			HostPort socketAddress) {
		HttpAsyncSocket<HttpRequest> httpSocket = socketFactory.create(socketAddress, interfaceName, forceIpV4);
		return new DirectProxyHttpConnection(factory, clientProcessorFactory,
				new DirectHttpAsyncSocket<>(sourceChannel, dataSenderFactory), httpSocket);
	}

}
