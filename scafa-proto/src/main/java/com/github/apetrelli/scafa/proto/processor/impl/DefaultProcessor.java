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
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerResult;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.processor.InputProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Processor;

public class DefaultProcessor<P extends Input, H extends Handler> implements Processor<H> {

    private static final Logger LOG = Logger.getLogger(DefaultProcessor.class.getName());

    private AsyncSocket client;

    private InputProcessorFactory<H, P> inputProcessorFactory;

    private ProcessingContextFactory<P> processingContextFactory;

	public DefaultProcessor(AsyncSocket client, InputProcessorFactory<H, P> inputProcessorFactory,
			ProcessingContextFactory<P> processingContextFactory) {
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
            read(context, handler, processor).handle((x, exc) -> {
                if (exc instanceof AsynchronousCloseException || exc instanceof ClosedChannelException) {
                    LOG.log(Level.INFO, "Channel closed", exc);
                } else if (exc instanceof IOException) {
                    LOG.log(Level.INFO, "I/O exception, closing", exc);
                    disconnect(handler);
                } else {
                    LOG.log(Level.SEVERE, "Unrecognized exception, don''t know what to do...", exc);
                }
            	return CompletionHandlerFuture.completeEmpty();
            });
        } catch (IOException e) {
            LOG.log(Level.INFO, "Error when establishing a connection", e);
        }
    }

	private CompletableFuture<CompletionHandlerResult<Void, Void>> read(P context, H handler, InputProcessor<P> processor) {
		return client.read(context.getBuffer(), context).thenCompose(x -> {
		    if (x.getResult() >= 0) {
		        ByteBuffer buffer = x.getAttachment().getBuffer();
		        buffer.flip();
				return processor.process(x.getAttachment())
						.thenCompose(y -> read(x.getAttachment(), handler, processor));
		    } else {
		        disconnect(handler);
                return CompletionHandlerFuture.complete(null, null);
		    }
		});
	}
    
    private void disconnect(H handler) {
    	CompletableFuture<Void> disconnect = client.disconnect();
		disconnect.handle((x, y) -> {
            LOG.log(Level.SEVERE, "Error when disposing client", y);
            return x;
		}).thenRun(handler::onDisconnect);
    }

}
