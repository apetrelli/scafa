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
package com.github.apetrelli.scafa.proto.processor.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.aio.ByteSinkFactory;
import com.github.apetrelli.scafa.proto.processor.BufferProcessor;
import com.github.apetrelli.scafa.proto.processor.BufferProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.ByteSink;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.Status;
import com.github.apetrelli.scafa.proto.util.ObjectHolder;

public class DefaultProcessor<I extends Input, S extends ByteSink<I>, H> implements Processor<H> {

    private class ClientReadCompletionHandler implements CompletionHandler<Integer, I> {
        private final ObjectHolder<Status<I, S>> statusHolder;
        private final S sink;
        private final BufferProcessor<I, S> processor;

        private ClientReadCompletionHandler(ObjectHolder<Status<I, S>> statusHolder, S sink,
                BufferProcessor<I, S> processor) {
            this.statusHolder = statusHolder;
            this.sink = sink;
            this.processor = processor;
        }

        @Override
        public void completed(Integer result, I attachment) {
            if (result >= 0) {
                ByteBuffer buffer = attachment.getBuffer();
                buffer.flip();
                processor.process(attachment, statusHolder.getObj(), new ProcessCompletionHandler(statusHolder, buffer, this));
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
    }

    private class ProcessCompletionHandler implements CompletionHandler<Status<I, S>, I> {
        private final ObjectHolder<Status<I, S>> statusHolder;
        private final ByteBuffer buffer;
        private ClientReadCompletionHandler clientReadCompletionHandler;

        private ProcessCompletionHandler(ObjectHolder<Status<I, S>> statusHolder, ByteBuffer buffer, ClientReadCompletionHandler clientReadCompletionHandler) {
            this.statusHolder = statusHolder;
            this.buffer = buffer;
            this.clientReadCompletionHandler = clientReadCompletionHandler;
        }

        @Override
        public void completed(Status<I, S> result, I attachment) {
            statusHolder.setObj(result);
            if (client.isOpen()) {
                buffer.clear();
                client.read(buffer, attachment, clientReadCompletionHandler);
            }
        }

        @Override
        public void failed(Throwable exc, I attachment) {
            LOG.log(Level.INFO, "Error when processing buffer, disconnecting", exc);
            clientReadCompletionHandler.disconnect();
        }
    }

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
            CompletionHandler<Integer, I> clientCompletionHandler = new ClientReadCompletionHandler(statusHolder, sink, processor);
            client.read(input.getBuffer(), input, clientCompletionHandler);
        } catch (IOException e) {
            LOG.log(Level.INFO, "Error when establishing a connection", e);
        }
    }

}
