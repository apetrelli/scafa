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
package com.github.apetrelli.scafa.proto.aio;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.processor.Processor;

public class ScafaListener<H, S extends AsyncSocket> {

    private static final Logger LOG = Logger.getLogger(ScafaListener.class.getName());

    private AsyncSocketFactory<S> asyncSocketFactory;
    
    private ProcessorFactory<H> processorFactory;

    private HandlerFactory<H, S> handlerFactory;

    private int portNumber;

    private String interfaceName;

    private boolean forceIpV4;

    private AsynchronousServerSocketChannel server;

	public ScafaListener(AsyncSocketFactory<S> asyncSocketFactory, ProcessorFactory<H> processorFactory,
			HandlerFactory<H, S> handlerFactory, int portNumber, String interfaceName, boolean forceIpV4) {
		this.asyncSocketFactory = asyncSocketFactory;
        this.processorFactory = processorFactory;
        this.handlerFactory = handlerFactory;
        this.portNumber = portNumber;
        this.interfaceName = interfaceName;
        this.forceIpV4 = forceIpV4;
    }

	public ScafaListener(AsyncSocketFactory<S> asyncSocketFactory, ProcessorFactory<H> processorFactory,
			HandlerFactory<H, S> handlerFactory, int portNumber) {
    	this(asyncSocketFactory, processorFactory, handlerFactory, portNumber, null, false);
    }

    public void listen() throws IOException {
        server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(portNumber));
        bindChannel();
        server.accept((Void) null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            @Override
            public void completed(AsynchronousSocketChannel client,
                    Void attachment) {
                S socket = asyncSocketFactory.create(client);
				Processor<H> processor = processorFactory.create(socket);
                H handler = handlerFactory.create(socket);
                processor.process(handler);
                server.accept(null, this);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                LOG.log(Level.SEVERE, "Error when accepting connections", exc);
            }
        });

    }

    public void stop() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Error when closing server channel", e);
            }
        }
    }

    private void bindChannel() throws IOException {
        if (interfaceName != null) {
            NetworkInterface intf = NetworkInterface.getByName(interfaceName);
            if (!intf.isUp()) {
                throw new SocketException("The interface " + interfaceName + " is not connected");
            }
            Enumeration<InetAddress> addresses = intf.getInetAddresses();
            if (!addresses.hasMoreElements()) {
                throw new SocketException("The interface " + interfaceName + " has no addresses");
            }
            InetAddress address = null;
            while (addresses.hasMoreElements() && address == null) {
                InetAddress currentAddress = addresses.nextElement();
                if (!forceIpV4 || currentAddress instanceof Inet4Address) {
                    address = currentAddress;
                }
            }
            if (address == null) {
                throw new SocketException("Not able to find a feasible address for interface " + interfaceName);
            }
            server.bind(new InetSocketAddress(address, 0));
        }
    }
}
