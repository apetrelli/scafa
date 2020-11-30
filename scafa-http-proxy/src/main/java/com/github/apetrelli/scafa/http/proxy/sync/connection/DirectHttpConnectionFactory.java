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
import com.github.apetrelli.scafa.http.proxy.sync.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.http.sync.direct.DirectHttpSyncSocket;
import com.github.apetrelli.scafa.http.sync.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.RunnableStarter;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;

public class DirectHttpConnectionFactory implements ProxyHttpConnectionFactory {

	private SocketFactory<SyncSocket> socketFactory;

	private DataSenderFactory dataSenderFactory;
	
	private ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory;
	
	private RunnableStarter runnableStarter;

	private String interfaceName;

	private boolean forceIpV4;

	public DirectHttpConnectionFactory(SocketFactory<SyncSocket> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			RunnableStarter runnableStarter) {
		this(socketFactory, dataSenderFactory, clientProcessorFactory, runnableStarter, null, false);
	}

	public DirectHttpConnectionFactory(SocketFactory<SyncSocket> socketFactory, DataSenderFactory dataSenderFactory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, RunnableStarter runnableStarter,
			String interfaceName, boolean forceIpV4) {
		this.socketFactory = socketFactory;
		this.dataSenderFactory = dataSenderFactory;
		this.clientProcessorFactory = clientProcessorFactory;
		this.runnableStarter = runnableStarter;
		this.interfaceName = interfaceName;
		this.forceIpV4 = forceIpV4;
	}

	@Override
	public ProxyHttpConnection create(MappedProxyHttpConnectionFactory factory, SyncSocket sourceChannel,
			HostPort socketAddress) {
		SyncSocket socket = socketFactory.create(socketAddress, interfaceName, forceIpV4);
		HttpSyncSocket<HttpRequest> httpSocket = new DirectHttpSyncSocket<>(socket, dataSenderFactory);
		return new DirectProxyHttpConnection(factory, clientProcessorFactory, runnableStarter,
				new DirectHttpSyncSocket<>(sourceChannel, dataSenderFactory), httpSocket);
	}

}
