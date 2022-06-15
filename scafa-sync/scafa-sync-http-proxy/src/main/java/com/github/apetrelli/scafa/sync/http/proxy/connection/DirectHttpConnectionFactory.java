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
package com.github.apetrelli.scafa.sync.http.proxy.connection;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.sync.http.socket.direct.DirectHttpSyncSocket;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectHttpConnectionFactory implements GatewayHttpConnectionFactory<ProxyHttpConnection> {

	private final SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory;

	private final DataSenderFactory dataSenderFactory;
	
	private final ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory;
	
	private final RunnableStarter runnableStarter;

	private final String interfaceName;

	private final boolean forceIpV4;

	public DirectHttpConnectionFactory(SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			RunnableStarter runnableStarter) {
		this(socketFactory, dataSenderFactory, clientProcessorFactory, runnableStarter, null, false);
	}

	@Override
	public ProxyHttpConnection create(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> factory,
			SyncSocket sourceChannel, HostPort socketAddress) {
		HttpSyncSocket<HttpRequest> httpSocket = socketFactory.create(socketAddress, interfaceName, forceIpV4);
		return new DirectProxyHttpConnection(factory, clientProcessorFactory, runnableStarter,
				new DirectHttpSyncSocket<>(sourceChannel, dataSenderFactory), httpSocket);
	}

}
