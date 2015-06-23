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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.processor.BufferProcessor;
import com.github.apetrelli.scafa.processor.ByteSink;
import com.github.apetrelli.scafa.processor.Input;
import com.github.apetrelli.scafa.server.Status;

public class DefaultBufferProcessor<I extends Input, S extends ByteSink<I>> implements BufferProcessor<I, S> {

    private static final Logger LOG = Logger.getLogger(DefaultBufferProcessor.class.getName());

    private S sink;

    public DefaultBufferProcessor(S sink) {
        this.sink = sink;
    }

    @Override
    public Status<I, S> process(I input, Status<I, S> status) {
        ByteBuffer buffer = input.getBuffer();
        while (buffer.position() < buffer.limit()) {
            status = status.next(input);
            try {
                status.out(input, sink);
            } catch (IOException e) {
                String message = "Generic I/O error";
                manageError(input, e, message);
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Generic runtime error", e);
                input.setCaughtError(true);
            }
        }
        return status;
    }

    private void manageError(I input, IOException e, String message) {
        LOG.log(Level.INFO, message, e);
        if (input.isHttpConnected()) {
            try {
                sink.disconnect();
            } catch (IOException e1) {
                LOG.log(Level.INFO, "Generic runtime error", e);
            }
        } else {
            input.setCaughtError(true);
        }
    }

}