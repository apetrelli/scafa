/**
 * Scafa - Universal roadwarrior non-caching proxy
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
package com.github.apetrelli.scafa.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.server.processor.ByteSink;
import com.github.apetrelli.scafa.server.processor.ByteSinkFactory;
import com.github.apetrelli.scafa.server.processor.Input;

public class ScafaListener<I extends Input, S extends ByteSink<I>> {

    private static class ObjectHolder<T> {
        T obj;
    }

    private static final Logger LOG = Logger.getLogger(ScafaListener.class.getName());

    private ByteSinkFactory<I, S> factory;

    private Status<I, S> initialStatus;

    public ScafaListener(ByteSinkFactory<I, S> factory,
            Status<I, S> initialStatus) {
        this.factory = factory;
        this.initialStatus = initialStatus;
    }

    public void listen() throws IOException {
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(9000));
        server.accept((Void) null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            @Override
            public void completed(AsynchronousSocketChannel client,
                    Void attachment) {
                S sink = factory.create(client);
                ObjectHolder<Status<I, S>> statusHolder = new ObjectHolder<>();
                statusHolder.obj = initialStatus;
                I input = sink.createInput();
                client.read(input.getBuffer(), input, new CompletionHandler<Integer, I>() {
                    @Override
                    public void completed(Integer result, I attachment) {
                        ByteBuffer buffer = attachment.getBuffer();
                        if (result >= 0) {
                            buffer.flip();
                            while (buffer.position() < buffer.limit()) {
                                statusHolder.obj = statusHolder.obj.next(input);
                                try {
                                    statusHolder.obj.out(input, sink);
                                } catch (IOException e) {
                                    LOG.log(Level.INFO, "Generic I/O error", e);
                                    input.setCaughtError(true);
                                } catch (RuntimeException e) {
                                    LOG.log(Level.INFO, "Generic runtime error", e);
                                    input.setCaughtError(true);
                                }
                            }
                            if (client.isOpen()) {
                                buffer.clear();
                                client.read(buffer, attachment, this);
                            }
                        } else {
                            try {
                                sink.dispose();
                            } catch (IOException e) {
                                LOG.log(Level.SEVERE, "Error when disposing client", e);
                            }
                        }
                    }

                    @Override
                    public void failed(Throwable exc, I attachment) {
                        LOG.log(Level.INFO, "Error when listening to I/O");
                        try {
                            client.close();
                        } catch (IOException e) {
                            LOG.log(Level.FINE, "Error when closing client");
                        }
                    }
                });


                server.accept(null, this);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                LOG.log(Level.SEVERE, "Error when accepting connections", exc);
            }
        });

    }
}
