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
package com.github.apetrelli.scafa.processor.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.aio.ByteSinkFactory;
import com.github.apetrelli.scafa.processor.BufferProcessor;
import com.github.apetrelli.scafa.processor.BufferProcessorFactory;
import com.github.apetrelli.scafa.processor.ByteSink;
import com.github.apetrelli.scafa.processor.Input;
import com.github.apetrelli.scafa.processor.Processor;
import com.github.apetrelli.scafa.server.Status;
import com.github.apetrelli.scafa.util.ObjectHolder;

public class DefaultProcessor<I extends Input, S extends ByteSink<I>, H> implements Processor<H> {

    private static final Logger LOG = Logger.getLogger(DefaultProcessor.class.getName());

    private AsynchronousSocketChannel client;

    private ByteSinkFactory<I, S, H> factory;

    private BufferProcessorFactory<I, S> bufferProcessorFactory;

    private Status<I, S> initialStatus;

    public DefaultProcessor(AsynchronousSocketChannel client, ByteSinkFactory<I, S, H> factory,
            BufferProcessorFactory<I, S> bufferProcessorFactory, Status<I, S> initialStatus) {
        this.client = client;
        this.factory = factory;
        this.bufferProcessorFactory = bufferProcessorFactory;
        this.initialStatus = initialStatus;
    }

    @Override
    public void process(H handler) {
        S sink = factory.create(client, handler);
        try {
            sink.connect();
            ObjectHolder<Status<I, S>> statusHolder = new ObjectHolder<>();
            statusHolder.setObj(initialStatus);
            I input = sink.createInput();
            BufferProcessor<I, S> processor = bufferProcessorFactory.create(sink);
            client.read(input.getBuffer(), input, new CompletionHandler<Integer, I>() {
                @Override
                public void completed(Integer result, I attachment) {
                    if (result >= 0) {
                        ByteBuffer buffer = attachment.getBuffer();
                        buffer.flip();
                        try {
                            statusHolder.setObj(processor.process(attachment, statusHolder.getObj()));
                            if (client.isOpen()) {
                                buffer.clear();
                                client.read(buffer, attachment, this);
                            }
                        } catch (IOException e) {
                            LOG.log(Level.INFO, "Error when processing buffer, disconnecting", e);
                            disconnect();
                        }
                    } else {
                        disconnect();
                    }
                }

                @Override
                public void failed(Throwable exc, I attachment) {
                    if (exc instanceof AsynchronousCloseException || exc instanceof ClosedChannelException) {
                        LOG.log(Level.INFO, "Channel closed", exc);
                    } else if (exc instanceof IOException) {
                        LOG.log(Level.INFO, "I/O exception, closing", exc);
                        disconnect();
                    } else {
                        LOG.log(Level.SEVERE, "Unrecognized exception, don't know what to do...", exc);
                    }
                }

                private void disconnect() {
                    try {
                        sink.disconnect();
                        if (client.isOpen()) {
                            client.close();
                        }
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, "Error when disposing client", e);
                    }
                }
            });
        } catch (IOException e) {
            LOG.log(Level.INFO, "Error when establishing a connection", e);
        }
    }

}
