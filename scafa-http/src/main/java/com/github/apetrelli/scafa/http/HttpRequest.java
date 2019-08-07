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
package com.github.apetrelli.scafa.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRequest extends HeaderHolder {

	private static final Logger LOG = Logger.getLogger(HttpRequest.class.getName());

    private static final Map<String, Integer> protocol2port = new HashMap<String, Integer>();

    private String method;

    private String resource;

    private String httpVersion;

    static {
        protocol2port.put("http", 80);
        protocol2port.put("https", 443);
        protocol2port.put("ftp", 80); // This works only with a proxy.
    }

    public HttpRequest(String method, String resource, String httpVersion) {
        this.method = method;
        this.resource = resource;
        this.httpVersion = httpVersion;
        byteSize = method.length() + 1 + resource.length() + 1 + httpVersion.length() + 4;
    }

    public HttpRequest(HttpRequest toCopy) {
        method = toCopy.method;
        resource = toCopy.resource;
        httpVersion = toCopy.httpVersion;
        copyBase(toCopy);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        if (this.method != null) {
            byteSize -= this.method.length();
        }
        this.method = method;
        byteSize += this.method.length();
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        if (this.resource != null) {
            byteSize -= this.resource.length();
        }
        this.resource = resource;
        byteSize += this.resource.length();
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        if (this.httpVersion != null) {
            byteSize -= this.httpVersion.length();
        }
        this.httpVersion = httpVersion;
        byteSize += this.httpVersion.length();
    }

    public HostPort getHostPort() throws IOException {
        HostPort retValue;
        String hostString = getHeader("HOST");
        String url = getResource();
        if (hostString != null) {
            String[] hostStringSplit = hostString.split(":");
            Integer port = null;
            if (hostStringSplit.length == 1) {
                try {
                    URL realUrl = new URL(url);
                    port = protocol2port.get(realUrl.getProtocol());
                } catch (MalformedURLException e) {
                    // Rare, only in HTTP 1.0
                    LOG.log(Level.FINE, "Host header not present and connect executed!", e);
                    hostStringSplit = url.split(":");
                    if (hostStringSplit.length != 2) {
                        throw new IOException("Malformed Host url: " + url);
                    }
                }
            } else if (hostStringSplit.length != 2) {
                throw new IOException("Malformed Host header: " + hostString);
            }
            if (port == null) {
                try {
                    port = Integer.decode(hostStringSplit[1]);
                } catch (NumberFormatException e) {
                    throw new IOException("Malformed port: " + hostStringSplit[1], e);
                }
            }
            if (port == null || port < 0) {
                throw new IOException("Invalid port " + port + " for connection to " + url);
            }
            retValue = new HostPort(hostStringSplit[0], port);
        } else {
            URL realUrl = new URL(url);
            retValue = new HostPort(realUrl.getHost(), realUrl.getPort());
        }
        return retValue;
    }

    @Override
    public ByteBuffer toHeapByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(byteSize);
        Charset charset = StandardCharsets.US_ASCII;
        buffer.put(method.getBytes(charset)).put(SPACE).put(resource.getBytes(charset)).put(SPACE)
                .put(httpVersion.getBytes(charset)).put(CR).put(LF);
        loadInBuffer(buffer);
        return buffer;
    }

}
