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

import java.io.IOException;
import java.util.logging.Level;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.HttpConnectRequest;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.gateway.connection.AbstractGatewayHttpConnection;
import com.github.apetrelli.scafa.sync.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

import lombok.extern.java.Log;

@Log
public abstract class AbstractUpstreamProxyHttpConnection extends AbstractGatewayHttpConnection<Socket> implements ProxyHttpConnection {

    protected final HttpRequestManipulator manipulator;

	public AbstractUpstreamProxyHttpConnection(MappedGatewayHttpConnectionFactory<?> factory,
			ProcessorFactory<DataHandler, Socket> clientProcessorFactory, 
			RunnableStarter runnableStarter, Socket sourceChannel,
			HttpSyncSocket<HttpRequest> socket, HostPort destinationSocketAddress,
			HttpRequestManipulator manipulator) {
        super(factory, clientProcessorFactory, runnableStarter, sourceChannel, socket, destinationSocketAddress);
        this.manipulator = manipulator;
    }
    
    @Override
    public void connect(HttpConnectRequest request) {
        if (log.isLoggable(Level.INFO)) {
            try {
            	log.log(Level.INFO, "Connected thread {0} to address {1} and host {2}", new Object[] {
				        Thread.currentThread().getName(), socket.getAddress(), request.getHostPort()});
			} catch (IOException e) {
				log.log(Level.WARNING, "Error when parsing connect request", e);
			}
        }
        doConnect(request);
    }

    protected void doConnect(HttpConnectRequest request) {
        socket.sendHeader(request);
    }

    @Override
    protected HttpRequest createForwardedRequest(HttpRequest request) {
        if (manipulator != null) {
            request = new HttpRequest(request);
            manipulator.manipulate(request);
        }
        if (log.isLoggable(Level.INFO)) {
        	log.log(Level.INFO, "Connected thread {0} to port {1} and URL {2}",
                    new Object[] { Thread.currentThread().getName(), socket.getAddress(), request.getResource() });
        }
        return request;
    }

}
