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
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnection;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.util.HttpUtils;

public abstract class AbstractHttpConnection implements HttpConnection {

    private class WriteCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

        private CompletionHandler<Integer, ByteBuffer> readHandler;

        private WriteCompletionHandler(CompletionHandler<Integer, ByteBuffer> readHandler) {
            this.readHandler = readHandler;
        }

        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            attachment.clear();
            channel.read(attachment, attachment, readHandler);
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            readHandler.failed(exc, attachment);
        }
    }

    private static final Logger LOG = Logger.getLogger(AbstractHttpConnection.class.getName());

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    protected MappedHttpConnectionFactory factory;

    protected AsynchronousSocketChannel channel, sourceChannel;

    protected HostPort socketAddress;

    protected ByteBuffer readBuffer = ByteBuffer.allocate(16384);

    private String interfaceName;

    private boolean forceIpV4;

    public AbstractHttpConnection(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
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

                prepareChannel(factory, sourceChannel, socketAddress);
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

    protected void prepareChannel(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress) {
        channel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result >= 0) {
                    attachment.flip();
                    sourceChannel.write(attachment, attachment, new WriteCompletionHandler(this));
                } else {
                    if (sourceChannel.isOpen()) {
                        try {
                            sourceChannel.shutdownOutput();
                        } catch (IOException e) {
                            LOG.log(Level.INFO, "Error when shutting down the source channel", e);
                        }
                    }
                    disconnect();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                if (exc instanceof AsynchronousCloseException || exc instanceof ClosedChannelException) {
                    LOG.log(Level.INFO, "Channel closed", exc);
                } else if (exc instanceof IOException){
                    LOG.log(Level.INFO, "I/O exception, closing", exc);
                    disconnect();
                } else {
                    LOG.log(Level.SEVERE, "Unrecognized exception, don't know what to do...", exc);
                }
            }

            private void disconnect() {
                factory.dispose(socketAddress);
                try {
                    if (channel.isOpen()) {
                        channel.close();
                    }
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Error when closing channel", e);
                }
            }
        });
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
