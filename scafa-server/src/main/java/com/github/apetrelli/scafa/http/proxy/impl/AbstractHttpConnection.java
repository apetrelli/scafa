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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.HostPort;
import com.github.apetrelli.scafa.http.proxy.HttpConnection;
import com.github.apetrelli.scafa.http.proxy.MappedHttpConnectionFactory;
import com.github.apetrelli.scafa.util.HttpUtils;

public abstract class AbstractHttpConnection implements HttpConnection {

    private static final Logger LOG = Logger.getLogger(AbstractHttpConnection.class.getName());

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    protected MappedHttpConnectionFactory factory;

    protected AsynchronousSocketChannel channel, sourceChannel;

    protected HostPort socketAddress;

    protected ByteBuffer readBuffer = ByteBuffer.allocate(16384);

	public AbstractHttpConnection(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
			HostPort socketAddress) {
        this.factory = factory;
        this.sourceChannel = sourceChannel;
        this.socketAddress = socketAddress;
    }

    @Override
    public void ensureConnected(CompletionHandler<Void, Void> handler) {

        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to address {1}",
                    new Object[] { Thread.currentThread().getName(), socketAddress.toString() });
        }
        try {
			channel = AsynchronousSocketChannel.open();
		} catch (IOException e1) {
			handler.failed(e1, null);
		}
    	establishConnection(new CompletionHandler<Void, Void>() {

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

			@Override
			public void failed(Throwable exc, Void attachment) {
				handler.failed(exc, attachment);
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
    public void send(ByteBuffer buffer) throws IOException {
        HttpUtils.getFuture(channel.write(buffer));
    }

    @Override
    public void end() throws IOException {
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
        try {
			HttpUtils.sendHeader(request, channel);
	        completionHandler.completed(null, null);
		} catch (IOException e) {
			completionHandler.failed(e, null);
		}
    }

    protected void prepareChannel(MappedHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress) {
        channel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result >= 0) {
                    attachment.flip();
                    try {
                        HttpUtils.getFuture(sourceChannel.write(attachment));
                        attachment.clear();
                        channel.read(attachment, attachment, this);
                    } catch (IOException e) {
                        LOG.log(Level.INFO, "Error when writing buffer, disconnecting", e);
                        disconnect();
                    }
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
}
