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
package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpConnection;
import com.github.apetrelli.scafa.http.HttpConnectionFactory;
import com.github.apetrelli.scafa.util.HttpUtils;

public abstract class AbstractHttpConnection implements HttpConnection {

    private static final Logger LOG = Logger.getLogger(AbstractHttpConnection.class.getName());

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;
    
    protected HttpConnectionFactory factory;

    protected AsynchronousSocketChannel channel, sourceChannel;

    protected ByteBuffer buffer = ByteBuffer.allocate(16384);

    protected ByteBuffer readBuffer = ByteBuffer.allocate(16384);

    public AbstractHttpConnection(HttpConnectionFactory factory,
            AsynchronousSocketChannel sourceChannel)
            throws IOException {
        this.factory = factory;
        this.sourceChannel = sourceChannel;
        channel = AsynchronousSocketChannel.open();
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
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        if (channel.isOpen()) {
            channel.close();
        }
    }

    protected void prepareChannel(HttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress) throws IOException {
        channel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result >= 0) {
                    attachment.flip();
                    try {
                        sourceChannel.write(attachment).get();
                        attachment.clear();
                        channel.read(attachment, attachment, this);
                    } catch (InterruptedException | ExecutionException e) {
                        failed(e, attachment);
                    }
                } else {
                    try {
                        factory.dispose(socketAddress);
                    } catch (IOException e) {
                        failed(e, attachment);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                if (exc instanceof AsynchronousCloseException) {
                    LOG.log(Level.INFO, "Channel has been closed", exc);
                } else {
                    LOG.log(Level.SEVERE, "Error when writing to source", exc);
                }
                try {
                    channel.close();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Error when closing channel", exc);
                }
            }
        });
    }
}
