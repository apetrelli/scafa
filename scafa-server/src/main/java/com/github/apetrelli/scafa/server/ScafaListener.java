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
package com.github.apetrelli.scafa.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.aio.HandlerFactory;
import com.github.apetrelli.scafa.aio.ProcessorFactory;
import com.github.apetrelli.scafa.processor.Processor;

public class ScafaListener<H> {

    private static final Logger LOG = Logger.getLogger(ScafaListener.class.getName());
    
    private ProcessorFactory<H> processorFactory;
    
    private HandlerFactory<H> handlerFactory;

    private int portNumber;

    private AsynchronousServerSocketChannel server;
    
    public ScafaListener(ProcessorFactory<H> processorFactory, HandlerFactory<H> handlerFactory, int portNumber) {
        this.processorFactory = processorFactory;
        this.handlerFactory = handlerFactory;
        this.portNumber = portNumber;
    }

    public void listen() throws IOException {
        server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(portNumber));
        server.accept((Void) null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            @Override
            public void completed(AsynchronousSocketChannel client,
                    Void attachment) {
                Processor<H> processor = processorFactory.create(client);
                H handler = handlerFactory.create(client);
                processor.process(handler);
                server.accept(null, this);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                LOG.log(Level.SEVERE, "Error when accepting connections", exc);
            }
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
