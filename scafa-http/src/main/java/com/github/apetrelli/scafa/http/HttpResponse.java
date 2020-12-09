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

import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HttpResponse extends HeaderHolder {
    private AsciiString httpVersion;

    private int code;

    private AsciiString message;

    public HttpResponse(AsciiString httpVersion, int code, AsciiString message) {
        this.httpVersion = httpVersion;
        this.code = code;
        this.message = message;
        byteSize = httpVersion.length() + 1 + 3 + (message != null ? 1 + message.length() : 0) + 4;
    }

    public HttpResponse(HttpResponse toCopy) {
        httpVersion = toCopy.httpVersion;
        code = toCopy.code;
        message = toCopy.message;
        copyBase(toCopy);
    }

    public AsciiString getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(AsciiString httpVersion) {
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

    public AsciiString getMessage() {
        return message;
    }

    public void setMessage(AsciiString message) {
        if (this.message != null) {
            byteSize -= this.message.length();
        }
        this.message = message;
        byteSize += this.message.length();
    }

    @Override
    public ByteBuffer toHeapByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(byteSize);
        fill(buffer);
        return buffer;
    }

    @Override
	public void fill(ByteBuffer buffer) {
		Charset charset = StandardCharsets.US_ASCII;
        buffer.put(httpVersion.getArray()).put(SPACE).put(Integer.toString(code).getBytes(charset));
        if (message != null) {
        	buffer.put(SPACE).put(message.getArray());
        }
        buffer.put(CR).put(LF);
        loadInBuffer(buffer);
	}

}
