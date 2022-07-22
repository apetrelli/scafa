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

import com.github.apetrelli.scafa.proto.io.OutputFlow;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HttpResponse extends BaseHttpConversation implements Http1Conversation {
    private final AsciiString httpVersion;

    private final AsciiString code;

    private final AsciiString message;

    public HttpResponse(AsciiString httpVersion, AsciiString code, AsciiString message) {
    	super(new HeaderHolder());
        this.httpVersion = httpVersion;
        this.code = code;
        this.message = message;
    }

    public HttpResponse(HttpResponse toCopy) {
    	super(new HeaderHolder(toCopy.headers()));
        httpVersion = toCopy.httpVersion;
        code = toCopy.code;
        message = toCopy.message;
    }

    public AsciiString getHttpVersion() {
        return httpVersion;
    }

    public AsciiString getCode() {
        return code;
    }

    public AsciiString getMessage() {
        return message;
    }

    @Override
	public void fill(OutputFlow out) {
		out.write(httpVersion.getArray(), httpVersion.getFrom(), httpVersion.length()).write(HttpChars.SPACE).write(code.getArray(),
				code.getFrom(), code.length());
        if (message != null) {
        	out.write(HttpChars.SPACE).write(message.getArray(), message.getFrom(), message.length());
        }
        out.write(HttpChars.CR).write(HttpChars.LF);
        Http1Conversation.super.fill(out);
	}

}
