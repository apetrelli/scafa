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
package com.github.apetrelli.scafa.http.proxy.ntlm;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.impl.AbstractGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.impl.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.async.processor.DataHandler;
import com.github.apetrelli.scafa.proto.async.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class NtlmProxyHttpConnectionFactory extends AbstractGatewayHttpConnectionFactory<ProxyHttpConnection> {

	private DataSenderFactory dataSenderFactory;

	private String domain, username, password;

	private HttpRequestManipulator manipulator;

	private HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine;

	public NtlmProxyHttpConnectionFactory(SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
			HostPort proxySocketAddress, String interfaceName, boolean forceIpV4,
			String domain, String username, String password, HttpRequestManipulator manipulator,
			HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine) {
		super(socketFactory, clientProcessorFactory, proxySocketAddress, interfaceName, forceIpV4);
		this.dataSenderFactory = dataSenderFactory;
		this.domain = domain;
		this.username = username;
		this.password = password;
		this.manipulator = manipulator;
		this.stateMachine = stateMachine;
	}

	@Override
	protected ProxyHttpConnection createConnection(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> factory,
			AsyncSocket sourceChannel, HttpAsyncSocket<HttpRequest> httpSocket, HostPort socketAddress) {
		HttpAsyncSocket<HttpResponse> httpSourceSocket = new DirectHttpAsyncSocket<>(sourceChannel, dataSenderFactory);
		return new NtlmProxyHttpConnection(factory, clientProcessorFactory, httpSourceSocket, httpSocket, socketAddress,
				domain, username, password, stateMachine, manipulator);
	}
}
