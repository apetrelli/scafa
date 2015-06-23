/**
 * Scafa - Universal roadwarrior non-caching proxy
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
package com.github.apetrelli.scafa.http.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.ProxyHttpHandler;

public class ProxyHttpByteSink extends DefaultHttpByteSink<ProxyHttpHandler> {

    private static final Logger LOG = Logger.getLogger(ProxyHttpByteSink.class.getName());

    private static final String HTTP_502 = "HTTP/1.1 502 Bad gateway \r\n"
            + "Connection: close\r\n"
            + "Content-Length: %1$d\r\n\r\n";

    private AsynchronousSocketChannel sourceChannel;

    private byte[] generic502Page;
    
    public ProxyHttpByteSink(AsynchronousSocketChannel sourceChannel, ProxyHttpHandler handler) {
        super(handler);
        this.sourceChannel = sourceChannel;
        try {
            generic502Page = loadResource("/errorpages/generic-502.html");
        } catch (IOException e) {
            throw new IllegalStateException("Error page not found", e);
        }
    }
    
    @Override
    public void send(HttpInput input) throws IOException {
        if (input.isHttpConnected()) {
            handler.onDataToPassAlong(input.getBuffer());
        } else {
            super.send(input);
        }
    }
    
    @Override
    protected void manageError() {
        sendErrorPage(sourceChannel, HTTP_502, generic502Page);
    }

    @Override
    protected void manageRequestheader(ProxyHttpHandler handler, HttpInput input, String method, String url,
            String httpVersion, Map<String, List<String>> headers) throws IOException {
        if ("CONNECT".equalsIgnoreCase(method)) {
            String[] strings = url.split(":");
            if (strings.length != 2) {
                throw new IOException("Invalid host:port " + url);
            }
            try {
                int port = Integer.parseInt(strings[1]);
                handler.onConnectMethod(strings[0], port, httpVersion, headers);
                input.setHttpConnected(true);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid port in host:port " + url, e);
            }
        } else {
            super.manageRequestheader(handler, input, method, url, httpVersion, headers);
        }
    }

    private void sendErrorPage(AsynchronousSocketChannel client, String header,
            byte[] page) {
        String realHeader = String.format(header, page.length);
        try {
            client.write(ByteBuffer.wrap(realHeader.getBytes(StandardCharsets.US_ASCII))).get();
            client.write(ByteBuffer.wrap(page)).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.log(Level.SEVERE, "Error when sending error page", e);
        }
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
}
