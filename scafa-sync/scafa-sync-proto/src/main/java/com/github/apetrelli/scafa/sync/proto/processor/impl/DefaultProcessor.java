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
package com.github.apetrelli.scafa.sync.proto.processor.impl;

import java.nio.ByteBuffer;
import java.util.logging.Level;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.processor.Handler;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.sync.proto.SocketRuntimeException;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.InputProcessor;
import com.github.apetrelli.scafa.sync.proto.processor.InputProcessorFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RequiredArgsConstructor
@Log
public class DefaultProcessor<P extends Input, H extends Handler> implements Processor<H> {
    
    private final long id;

    private final SyncSocket client;

    private final InputProcessorFactory<H, P> inputProcessorFactory;

    private final ProcessingContextFactory<P> processingContextFactory;
    
    private final DefaultProcessorFactory<P, H> processorFactory;

    @Override
    public void process(H handler) {
        handler.onConnect();
        P context = processingContextFactory.create();
        InputProcessor<P> processor = inputProcessorFactory.create(handler);
        try {
        	read(context, handler, processor);
        } catch (SocketRuntimeException exc) {
            log.log(Level.INFO, "Channel closed", exc);
        } catch (IORuntimeException exc) {
            log.log(Level.INFO, "I/O exception, closing", exc);
            disconnect(handler);
        } catch (RuntimeException exc) {
            log.log(Level.SEVERE, "Unrecognized exception, use the handler", exc);
            handler.onError(exc);
        }
    }
    
    public void disconnectFinally() {
    	try {
    		client.disconnect();
    	} catch (RuntimeException y) {
			log.log(Level.SEVERE, "Error when disposing client", y);
    	}
    }

	private void read(P context, H handler, InputProcessor<P> processor) {
		try {
		    while (client.read(context.getBuffer()) >= 0) {
		        ByteBuffer buffer = context.getBuffer();
		        buffer.flip();
		        processor.process(context);
		        buffer.clear();
		    }
		} finally {
			disconnect(handler);
		}
	}
    
    private void disconnect(H handler) {
    	disconnectFinally();
    	processorFactory.release(id);
    	handler.onDisconnect();
    }

}
