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

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.sync.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.sync.connection.AbstractGatewayHttpConnection;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.sync.HttpSyncSocket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.RunnableStarter;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;

public abstract class AbstractUpstreamProxyHttpConnection extends AbstractGatewayHttpConnection<SyncSocket> implements ProxyHttpConnection {

    private static final Logger LOG = Logger.getLogger(AbstractUpstreamProxyHttpConnection.class.getName());

    protected HttpRequestManipulator manipulator;

	public AbstractUpstreamProxyHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, 
			RunnableStarter runnableStarter, SyncSocket sourceChannel,
			HttpSyncSocket<HttpRequest> socket, HostPort destinationSocketAddress,
			HttpRequestManipulator manipulator) {
        super(factory, clientProcessorFactory, runnableStarter, sourceChannel, socket, destinationSocketAddress);
        this.manipulator = manipulator;
    }
    
    @Override
    public void connect(HttpConnectRequest request, ByteBuffer buffer) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1} and host {2}:{3}", new Object[] {
                    Thread.currentThread().getName(), socket.getAddress(), request.getHost(), request.getPort()});
        }
        doConnect(request, buffer);
    }

    protected void doConnect(HttpConnectRequest request, ByteBuffer buffer) {
        socket.sendHeader(request, buffer);
    }

    @Override
    protected HttpRequest createForwardedRequest(HttpRequest request) {
        if (manipulator != null) {
            request = new HttpRequest(request);
            manipulator.manipulate(request);
        }
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to port {1} and URL {2}",
                    new Object[] { Thread.currentThread().getName(), socket.getAddress(), request.getResource() });
        }
        return request;
    }

}
