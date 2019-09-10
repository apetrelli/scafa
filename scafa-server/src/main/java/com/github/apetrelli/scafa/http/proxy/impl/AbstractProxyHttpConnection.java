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
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Enumeration;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.AbstractHttpConnection;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.util.HttpUtils;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.SimpleInputFactory;

public abstract class AbstractProxyHttpConnection extends AbstractHttpConnection implements ProxyHttpConnection {

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    protected MappedProxyHttpConnectionFactory factory;

    protected AsynchronousSocketChannel sourceChannel;

    private String interfaceName;

    private boolean forceIpV4;

    private SimpleInputFactory inputFactory = new SimpleInputFactory();

    public AbstractProxyHttpConnection(MappedProxyHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress, String interfaceName, boolean forceIpV4) {
        super(socketAddress);
        this.factory = factory;
        this.sourceChannel = sourceChannel;
        this.interfaceName = interfaceName;
        this.forceIpV4 = forceIpV4;
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
    public void end() {
        // Does nothing.
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

    @Override
    protected void bindChannel() throws IOException {
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
