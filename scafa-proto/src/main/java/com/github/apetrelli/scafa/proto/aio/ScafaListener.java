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
package com.github.apetrelli.scafa.proto.aio;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.async.AsyncServerSocket;
import com.github.apetrelli.scafa.proto.async.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.async.AsyncSocket;
import com.github.apetrelli.scafa.proto.async.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.processor.HandlerFactory;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class ScafaListener<H, S extends AsyncSocket> {

    private static final Logger LOG = Logger.getLogger(ScafaListener.class.getName());
    
    private AsyncServerSocketFactory<S> asyncServerSocketFactory;
    
    private ProcessorFactory<H, AsyncSocket> processorFactory;

    private HandlerFactory<H, S> handlerFactory;

    private AsyncServerSocket<S> server;

	public ScafaListener(AsyncServerSocketFactory<S> asyncServerSocketFactory, ProcessorFactory<H, AsyncSocket> processorFactory,
			HandlerFactory<H, S> handlerFactory) {
		this.asyncServerSocketFactory = asyncServerSocketFactory;
        this.processorFactory = processorFactory;
        this.handlerFactory = handlerFactory;
    }

    public void listen() throws IOException {
    	this.server = asyncServerSocketFactory.create();
        accept();
    }

	private void accept() {
		this.server.accept().thenCompose(socket -> {
			Processor<H> processor = processorFactory.create(socket);
            H handler = handlerFactory.create(socket);
            processor.process(handler);
            accept();
            return CompletionHandlerFuture.completeEmpty();
        });
	}

    public void stop() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Error when closing server channel", e);
            }
        }
    }
}
