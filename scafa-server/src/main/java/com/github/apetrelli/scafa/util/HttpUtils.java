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
package com.github.apetrelli.scafa.util;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.HttpRequestManipulator;

public class HttpUtils {

    private static final Logger LOG = Logger.getLogger(HttpUtils.class.getName());

    private static final byte CR = 13;

    private static final byte LF = 10;

    private HttpUtils() {
    }

    public static void sendHeader(HeaderHolder holder, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        ByteBuffer buffer = holder.toHeapByteBuffer();
        if (LOG.isLoggable(Level.FINEST)) {
            String request = new String(buffer.array(), 0, buffer.limit());
            LOG.finest("-- Raw request/response header");
            LOG.finest(request);
            LOG.finest("-- End of header --");
        }
        flushBuffer(buffer, channelToSend, completionHandler);
    }

    public static void flushBuffer(ByteBuffer buffer, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        buffer.flip();
        channelToSend.write(buffer, null, new CompletionHandler<Integer, Void>() {

            @Override
            public void completed(Integer result, Void attachment) {
                buffer.clear();
                completionHandler.completed(null, attachment);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                buffer.clear();
                completionHandler.failed(exc, attachment);
            }
        });
    }

    public static void sendChunkSize(long size, AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        String sizeString = Long.toString(size, 16);
        ByteBuffer buffer = ByteBuffer.allocate(sizeString.length() + 2);
        buffer.put(sizeString.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
        flushBuffer(buffer, channelToSend, completionHandler);
    }

    public static void sendNewline(AsynchronousSocketChannel channelToSend, CompletionHandler<Void, Void> completionHandler) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(CR).put(LF);
        flushBuffer(buffer, channelToSend, completionHandler);
    }

    public static HttpRequestManipulator createManipulator(Section section) {
        String className = section.get("manipulator");
        HttpRequestManipulator manipulator = null;
        if (className != null) {
            try {
                Class<? extends HttpRequestManipulator> clazz = Class.forName(className).asSubclass(HttpRequestManipulator.class);
                manipulator = clazz.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOG.log(Level.SEVERE, "Cannot instantiate manipulator: " + className, e);
            }
        }
        return manipulator;
    }

}
