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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpRequest extends HeaderHolder {

    private String method;

    private String resource;

    private String httpVersion;

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
