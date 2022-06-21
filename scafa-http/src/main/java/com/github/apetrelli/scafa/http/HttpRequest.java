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

import static com.github.apetrelli.scafa.http.HttpHeaders.HOST;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.util.AsciiString;

import lombok.extern.java.Log;

@Log
public class HttpRequest extends HeaderHolder {

    private static final Map<String, Integer> protocol2port = new HashMap<>();

    private final AsciiString method;

    private final AsciiString resource;

    private final AsciiString httpVersion;

    static {
        protocol2port.put("http", 80);
        protocol2port.put("https", 443);
        protocol2port.put("ftp", 80); // This works only with a proxy.
    }

    public HttpRequest(AsciiString method, AsciiString resource, AsciiString httpVersion) {
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

    public HttpRequest(HttpRequest toCopy, AsciiString resource) {
        method = toCopy.method;
        this.resource = resource;
        httpVersion = toCopy.httpVersion;
        copyBase(toCopy);
    }

    public AsciiString getMethod() {
        return method;
    }

    public AsciiString getResource() {
        return resource;
    }

    public AsciiString getHttpVersion() {
        return httpVersion;
    }

    public HostPort getHostPort() throws IOException {
        HostPort retValue;
        AsciiString hostString = getHeader(HOST);
        String url = getResource().toString();
        if (hostString != null) {
            String[] hostStringSplit = hostString.toString().split(":");
            Integer port = null;
            if (hostStringSplit.length == 1) {
            	if (url.startsWith("http") || url.startsWith("ftp")) {
	                try {
	                    URL realUrl = new URL(url);
	                    port = protocol2port.get(realUrl.getProtocol());
	                } catch (MalformedURLException e) {
	                    // Rare, only in HTTP 1.0
	                    log.log(Level.FINE, "Host header not present and connect executed!", e);
	                    hostStringSplit = url.split(":");
	                    if (hostStringSplit.length != 2) {
	                        throw new IOException("Malformed Host url: " + url);
	                    }
	                }
            	} else {
            		port = 80;
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
	public void fill(ByteBuffer buffer) {
		buffer.put(method.getArray(), method.getFrom(), method.length()).put(SPACE)
				.put(resource.getArray(), resource.getFrom(), resource.length()).put(SPACE)
				.put(httpVersion.getArray(), httpVersion.getFrom(), httpVersion.length()).put(CR).put(LF);
        loadInBuffer(buffer);
	}

    public ParsedResource getParsedResource() {
    	return new ParsedResource(resource);
    }
}
