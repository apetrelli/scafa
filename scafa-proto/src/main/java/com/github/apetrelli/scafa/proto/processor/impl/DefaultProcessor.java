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
import com.github.apetrelli.scafa.proto.processor.ByteSink;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.processor.InputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessingContext;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Processor;

public class DefaultProcessor<I extends Input, S extends ByteSink<I>, ST, P extends ProcessingContext<I, ST>, H> implements Processor<H> {

    private class ClientReadCompletionHandler implements CompletionHandler<Integer, P> {
        private final S sink;
        private final InputProcessor<I, ST, P> processor;

        private ProcessCompletionHandler processCompletionHandler;

		private ClientReadCompletionHandler(S sink, InputProcessor<I, ST, P> processor) {
            this.sink = sink;
            this.processor = processor;
        }

		public void setProcessCompletionHandler(ProcessCompletionHandler processCompletionHandler) {
			this.processCompletionHandler = processCompletionHandler;
		}

        @Override
        public void completed(Integer result, P attachment) {
            if (result >= 0) {
                ByteBuffer buffer = attachment.getInput().getBuffer();
                buffer.flip();
                processor.process(attachment, processCompletionHandler);
            } else {
                disconnect();
            }
        }

        @Override
        public void failed(Throwable exc, P attachment) {
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

    private class ProcessCompletionHandler implements CompletionHandler<P, P> {
        private ClientReadCompletionHandler clientReadCompletionHandler;

        private ProcessCompletionHandler(ClientReadCompletionHandler clientReadCompletionHandler) {
            this.clientReadCompletionHandler = clientReadCompletionHandler;
        }

        @Override
        public void completed(P result, P attachment) {
        	ByteBuffer buffer = attachment.getInput().getBuffer();
            if (client.isOpen()) {
                buffer.clear();
                client.read(buffer, attachment, clientReadCompletionHandler);
            }
        }

        @Override
        public void failed(Throwable exc, P attachment) {
            LOG.log(Level.INFO, "Error when processing buffer, disconnecting", exc);
            clientReadCompletionHandler.disconnect();
        }
    }

    private static final Logger LOG = Logger.getLogger(DefaultProcessor.class.getName());

    private AsynchronousSocketChannel client;

    private ByteSinkFactory<I, S, H> factory;

    private InputProcessorFactory<I, S, H, ST, P> inputProcessorFactory;

    private ProcessingContextFactory<I, ST, P> processingContextFactory;

    private ST initialStatus;

    public DefaultProcessor(AsynchronousSocketChannel client, ByteSinkFactory<I, S, H> factory,
            InputProcessorFactory<I, S, H, ST, P> inputProcessorFactory, ProcessingContextFactory<I, ST, P> processingContextFactory, ST initialStatus) {
        this.client = client;
        this.factory = factory;
        this.inputProcessorFactory = inputProcessorFactory;
        this.processingContextFactory = processingContextFactory;
        this.initialStatus = initialStatus;
    }

    @Override
    public void process(H handler) {
        S sink = factory.create(client, handler);
        try {
            sink.connect();
            I input = sink.createInput();
            P context = processingContextFactory.create(input, initialStatus);
            InputProcessor<I, ST, P> processor = inputProcessorFactory.create(handler);
            ClientReadCompletionHandler clientReadCompletionHandler = new ClientReadCompletionHandler(sink, processor);
            ProcessCompletionHandler processCompletionHandler = new ProcessCompletionHandler(clientReadCompletionHandler);
            clientReadCompletionHandler.setProcessCompletionHandler(processCompletionHandler);
            client.read(input.getBuffer(), context, clientReadCompletionHandler);
        } catch (IOException e) {
            LOG.log(Level.INFO, "Error when establishing a connection", e);
        }
    }

}
