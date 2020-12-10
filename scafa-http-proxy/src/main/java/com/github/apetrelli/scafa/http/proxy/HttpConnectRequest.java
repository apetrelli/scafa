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

import java.io.IOException;

import com.github.apetrelli.scafa.http.HttpRequest;

public class HttpConnectRequest extends HttpRequest {

    private String host;

    private int port;

    public HttpConnectRequest(HttpRequest toCopy) throws IOException {
        super(toCopy);
        String url = toCopy.getResource().toString();
        String[] strings = url.split(":");
        if (strings.length != 2) {
            throw new IOException("Invalid host:port " + url);
        }
        host = strings[0];
        try {
            port = Integer.parseInt(strings[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid port in host:port " + url, e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
