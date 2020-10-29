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
package com.github.apetrelli.scafa.proto.sync.processor.impl;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.sync.IORuntimeException;
import com.github.apetrelli.scafa.proto.sync.SocketRuntimeException;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.InputProcessor;
import com.github.apetrelli.scafa.proto.sync.processor.InputProcessorFactory;

public class DefaultProcessor<P extends Input, H extends Handler> implements Processor<H> {

    private static final Logger LOG = Logger.getLogger(DefaultProcessor.class.getName());

    private SyncSocket client;

    private InputProcessorFactory<H, P> inputProcessorFactory;

    private ProcessingContextFactory<P> processingContextFactory;

	public DefaultProcessor(SyncSocket client, InputProcessorFactory<H, P> inputProcessorFactory,
			ProcessingContextFactory<P> processingContextFactory) {
        this.client = client;
        this.inputProcessorFactory = inputProcessorFactory;
        this.processingContextFactory = processingContextFactory;
    }

    @Override
    public void process(H handler) {
        handler.onConnect();
        P context = processingContextFactory.create();
        InputProcessor<P> processor = inputProcessorFactory.create(handler);
        try {
        	read(context, handler, processor);
        } catch (SocketRuntimeException exc) {
            LOG.log(Level.INFO, "Channel closed", exc);
        } catch (IORuntimeException exc) {
            LOG.log(Level.INFO, "I/O exception, closing", exc);
            disconnect(handler);
        } catch (RuntimeException exc) {
            LOG.log(Level.SEVERE, "Unrecognized exception, don''t know what to do...", exc);
        }
    }

	private void read(P context, H handler, InputProcessor<P> processor) {
	    while (client.read(context.getBuffer()) >= 0) {
	        ByteBuffer buffer = context.getBuffer();
	        buffer.flip();
	        processor.process(context);
	        buffer.clear();
	    }
        disconnect(handler);
	}
    
    private void disconnect(H handler) {
    	try {
    		client.disconnect();
    	} catch (RuntimeException y) {
			LOG.log(Level.SEVERE, "Error when disposing client", y);
    	}
    	handler.onDisconnect();
    }

}
