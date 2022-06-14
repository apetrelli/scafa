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
package com.github.apetrelli.scafa.async.proto.processor.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

import com.github.apetrelli.scafa.async.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.async.proto.processor.InputProcessorFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RequiredArgsConstructor
@Log
public class DefaultProcessor<P extends Input, H extends Handler> implements Processor<H> {

    private final AsyncSocket client;

    private final InputProcessorFactory<H, P> inputProcessorFactory;

    private final ProcessingContextFactory<P> processingContextFactory;

    @Override
    public void process(H handler) {
        handler.onConnect();
        P context = processingContextFactory.create();
        InputProcessor<P> processor = inputProcessorFactory.create(handler);
        read(context, handler, processor).handle((x, exc) -> {
        	if (exc != null) {
        		if (exc instanceof CompletionException) {
        			exc = ((CompletionException) exc).getCause();
        		}
                if (exc instanceof AsynchronousCloseException || exc instanceof ClosedChannelException) {
                    log.log(Level.INFO, "Channel closed", exc);
                } else if (exc instanceof IOException) {
                	log.log(Level.INFO, "I/O exception, closing", exc);
                    disconnect(handler);
                } else {
                	log.log(Level.SEVERE, "Unrecognized exception, use the handler", exc);
                    handler.onError(exc);
                }
        	}
        	return x;
        });
    }

	private CompletableFuture<Void> read(P context, H handler, InputProcessor<P> processor) {
		return client.read(context.getBuffer()).thenCompose(x -> {
		    if (x >= 0) {
		        ByteBuffer buffer = context.getBuffer();
		        buffer.flip();
				return processor.process(context)
						.thenCompose(y -> {
							context.getBuffer().clear();
							return read(context, handler, processor);
						});
		    } else {
		        return disconnect(handler);
		    }
		});
	}
    
    private CompletableFuture<Void> disconnect(H handler) {
    	return client.disconnect().handle((x, y) -> {
    		if (y != null) {
    			log.log(Level.SEVERE, "Error when disposing client", y);
    		}
            return x;
		}).thenRun(handler::onDisconnect);
    }

}
