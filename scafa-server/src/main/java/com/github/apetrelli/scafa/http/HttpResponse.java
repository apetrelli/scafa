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

public class HttpResponse extends HeaderHolder {
    private String httpVersion;
    
    private int code;
    
    private String message;

    public HttpResponse(String httpVersion, int code, String message) {
        this.httpVersion = httpVersion;
        this.code = code;
        this.message = message;
        byteSize = httpVersion.length() + 1 + 3 + 1 + message.length() + 4;
    }
    
    public HttpResponse(HttpResponse toCopy) {
        httpVersion = toCopy.httpVersion;
        code = toCopy.code;
        message = toCopy.message;
        copyBase(toCopy);
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
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        if (this.message != null) {
            byteSize -= this.message.length();
        }
        this.message = message;
        byteSize += this.message.length();
    }

    @Override
    public ByteBuffer toHeapByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(byteSize);
        Charset charset = StandardCharsets.US_ASCII;
        buffer.put(httpVersion.getBytes(charset)).put(SPACE).put(Integer.toString(code).getBytes(charset)).put(SPACE)
                .put(message.getBytes(charset)).put(CR).put(LF);
        loadInBuffer(buffer);
        return buffer;
    }
    
}
