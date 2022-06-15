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
package com.github.apetrelli.scafa.sync.http.proxy.ntlm;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.gateway.connection.AbstractGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.sync.http.socket.direct.DirectHttpSyncSocket;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

public class NtlmProxyHttpConnectionFactory extends AbstractGatewayHttpConnectionFactory<ProxyHttpConnection> {
	
	private final DataSenderFactory dataSenderFactory;

	private final String domain, username, password;

	private final HttpRequestManipulator manipulator;

	private final HttpStateMachine<HttpHandler, Void> stateMachine;

	public NtlmProxyHttpConnectionFactory(SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory, DataSenderFactory dataSenderFactory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, RunnableStarter runnableStarter,
			HostPort proxySocketAddress, String interfaceName, boolean forceIpV4, String domain, String username,
			String password, HttpRequestManipulator manipulator, HttpStateMachine<HttpHandler, Void> stateMachine) {
		super(socketFactory, clientProcessorFactory, runnableStarter, proxySocketAddress, interfaceName, forceIpV4);
		this.dataSenderFactory = dataSenderFactory;
		this.domain = domain;
		this.username = username;
		this.password = password;
		this.manipulator = manipulator;
		this.stateMachine = stateMachine;
	}

	@Override
	protected ProxyHttpConnection createConnection(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> factory,
			SyncSocket sourceChannel, HttpSyncSocket<HttpRequest> httpSocket, HostPort socketAddress) {
		HttpSyncSocket<HttpResponse> httpSourceSocket = new DirectHttpSyncSocket<>(sourceChannel, dataSenderFactory);
		return new NtlmProxyHttpConnection(factory, clientProcessorFactory, runnableStarter, httpSourceSocket,
				httpSocket, socketAddress, domain, username, password, stateMachine, manipulator);
	}
}
