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
package com.github.apetrelli.scafa.http.proxy.impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.util.HttpUtils;

public abstract class AbstractProxyHttpConnection implements ProxyHttpConnection {

    private static final Logger LOG = Logger.getLogger(AbstractProxyHttpConnection.class.getName());

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    protected MappedProxyHttpConnectionFactory factory;

    protected AsynchronousSocketChannel channel, sourceChannel;

    protected HostPort socketAddress;

    private String interfaceName;

    private boolean forceIpV4;

    private SimpleInputFactory inputFactory = new SimpleInputFactory();

    public AbstractProxyHttpConnection(MappedProxyHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress, String interfaceName, boolean forceIpV4) {
        this.factory = factory;
        this.sourceChannel = sourceChannel;
        this.socketAddress = socketAddress;
        this.interfaceName = interfaceName;
        this.forceIpV4 = forceIpV4;
    }

    @Override
    public void ensureConnected(CompletionHandler<Void, Void> handler) {

        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to address {1}",
                    new Object[] { Thread.currentThread().getName(), socketAddress.toString() });
        }
        try {
            channel = AsynchronousSocketChannel.open();
            bindChannel();
        } catch (IOException e1) {
            handler.failed(e1, null);
        }
        establishConnection(new DelegateFailureCompletionHandler<Void, Void>(handler) {

            @Override
            public void completed(Void result, Void attachment) {
                if (LOG.isLoggable(Level.INFO)) {
                    try {
                        LOG.log(Level.INFO, "Connected thread {0} to port {1}",
                                new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString() });
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, "Cannot obtain local address", e);
                    }
                }

                prepareChannel();
                handler.completed(result, attachment);
            }
        });
    }

    @Override
    public void sendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        HttpRequest modifiedRequest;
        try {
            modifiedRequest = createForwardedRequest(request);
            doSendHeader(modifiedRequest, completionHandler);
        } catch (IOException e) {
            completionHandler.failed(e, null);
        }
    }

    @Override
    public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        HttpUtils.flushBuffer(buffer, channel, completionHandler);
    }

    @Override
    public void end() {
        // Does nothing.
    }

    @Override
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.shutdownInput();
        }
    }

    protected abstract void establishConnection(CompletionHandler<Void, Void> handler);

    protected abstract HttpRequest createForwardedRequest(HttpRequest request) throws IOException;

    protected void doSendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        HttpUtils.sendHeader(request, channel, completionHandler);
    }

    protected void prepareChannel() {
        Processor<Handler> processor = new DefaultProcessor<Input, Handler>(channel, new PassthroughInputProcessorFactory(sourceChannel), inputFactory);
        processor.process(new ChannelDisconnectorHandler(factory, sourceChannel, socketAddress));
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
            channel.bind(new InetSocketAddress(address, 0));
        }
    }
}
