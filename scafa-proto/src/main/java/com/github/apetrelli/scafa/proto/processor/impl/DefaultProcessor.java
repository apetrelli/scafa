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

import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.processor.InputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class DefaultProcessor<P extends Input, H extends Handler> implements Processor<H> {

    private class ClientReadCompletionHandler implements CompletionHandler<Integer, P> {
        private final H handler;
        private final InputProcessor<P> processor;

        private ProcessCompletionHandler processCompletionHandler;

		private ClientReadCompletionHandler(H handler, InputProcessor<P> processor) {
            this.handler = handler;
            this.processor = processor;
        }

		public void setProcessCompletionHandler(ProcessCompletionHandler processCompletionHandler) {
			this.processCompletionHandler = processCompletionHandler;
		}

        @Override
        public void completed(Integer result, P attachment) {
            if (result >= 0) {
                ByteBuffer buffer = attachment.getBuffer();
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
            	handler.onDisconnect();
                if (client.isOpen()) {
                    IOUtils.closeQuietly(client);
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
        	ByteBuffer buffer = attachment.getBuffer();
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

    private InputProcessorFactory<H, P> inputProcessorFactory;

    private ProcessingContextFactory<P> processingContextFactory;

    public DefaultProcessor(AsynchronousSocketChannel client,
            InputProcessorFactory<H, P> inputProcessorFactory, ProcessingContextFactory<P> processingContextFactory) {
        this.client = client;
        this.inputProcessorFactory = inputProcessorFactory;
        this.processingContextFactory = processingContextFactory;
    }

    @Override
    public void process(H handler) {
        try {
            handler.onConnect();
            P context = processingContextFactory.create();
            InputProcessor<P> processor = inputProcessorFactory.create(handler);
            ClientReadCompletionHandler clientReadCompletionHandler = new ClientReadCompletionHandler(handler, processor);
            ProcessCompletionHandler processCompletionHandler = new ProcessCompletionHandler(clientReadCompletionHandler);
            clientReadCompletionHandler.setProcessCompletionHandler(processCompletionHandler);
            client.read(context.getBuffer(), context, clientReadCompletionHandler);
        } catch (IOException e) {
            LOG.log(Level.INFO, "Error when establishing a connection", e);
        }
    }

}
