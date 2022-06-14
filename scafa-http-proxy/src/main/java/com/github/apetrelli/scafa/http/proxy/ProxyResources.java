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
package com.github.apetrelli.scafa.http.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import lombok.extern.java.Log;

@Log
public class ProxyResources {

    private static final ProxyResources INSTANCE = new ProxyResources();

    private static final String HTTP_502 = "HTTP/1.1 502 Bad gateway \r\n"
            + "Connection: close\r\n"
            + "Content-Length: %1$d\r\n\r\n";

    private byte[] generic502Page;

    private ProxyResources() {
        try {
            generic502Page = loadResource("/errorpages/generic-502.html");
        } catch (IOException e) {
            throw new IllegalStateException("Error page not found", e);
        }
    }

    public static ProxyResources getInstance() {
        return INSTANCE;
    }

    public void sendGenericErrorPage(AsynchronousSocketChannel client) {
        sendErrorPage(client, HTTP_502, generic502Page);
    }

    private byte[] loadResource(String resourcePath) throws IOException {
        byte[] retValue;
        try (InputStream stream = getClass().getResourceAsStream(resourcePath);
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int amount;
            while ((amount = stream.read(buf)) >= 0) {
                os.write(buf, 0, amount);
            }
            retValue = os.toByteArray();
        }
        return retValue;
    }

    private void sendErrorPage(AsynchronousSocketChannel client, String header,
            byte[] page) {
        String realHeader = String.format(header, page.length);
        try {
            getFuture(client.write(ByteBuffer.wrap(realHeader.getBytes(StandardCharsets.US_ASCII))));
            getFuture(client.write(ByteBuffer.wrap(page)));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error when sending error page", e);
        }
    }

    public static <T> T getFuture(Future<T> future) throws IOException {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new IOException("Future problem", e);
            }
        }
    }
}
