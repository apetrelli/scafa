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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.proto.io.OutputFlow;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public abstract class HeaderHolder {
	
	private static final class StringBuilderOutputFlow implements OutputFlow {

		private StringBuilder builder = new StringBuilder();
		
		@Override
		public OutputFlow write(byte currentByte) {
			builder.append((char) currentByte);
			return this;
		}

		@Override
		public OutputFlow write(byte[] bytes, int from, int length) {
			for (int i = 0; i < length; i++) {
				builder.append((char) bytes[from + i]);
			}
			return this;
		}

		@Override
		public OutputFlow flush() {
			// Does nothing
			return this;
		}
		
		@Override
		public String toString() {
			return builder.toString();
		}
		
	}

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    private static final byte COLON = 58;

    private Map<HeaderName, List<AsciiString>> headers = new LinkedHashMap<>();

    protected int byteSize;

    public void addHeader(HeaderName header, AsciiString value) {
    	// Usually there is one header value per header name, so it makes sense.
    	List<AsciiString> values = headers.computeIfAbsent(header, x -> new ArrayList<>(1));
        values.add(value);
        byteSize += header.length() + 2 + value.length() + 2;
    }

    public void setHeader(HeaderName header, AsciiString value) {
        removeHeader(header);
        addHeader(header, value);
    }

    public void removeHeader(HeaderName header) {
        List<AsciiString> values = headers.remove(header);
        if (values != null) {
            values.stream().forEach(t -> {
                byteSize -= header.length() + 2 + t.length() + 2;
            });
        }
    }

    public AsciiString getHeader(HeaderName header) {
        List<AsciiString> values = headers.get(header);
        AsciiString retValue = null;
        if (values != null && !values.isEmpty()) {
            retValue = values.get(0);
        }
        return retValue;
    }

    public List<AsciiString> getHeaders(HeaderName header) {
        List<AsciiString> values = headers.get(header);
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }

    public Collection<HeaderName> getHeaderNames() {
    	return Collections.unmodifiableCollection(headers.keySet());
    }

    public void fill(OutputFlow out) {
        headers.entrySet().stream().forEach(t -> {
			HeaderName key = t.getKey();
			t.getValue().forEach(
					u -> {
						out.write(key.getArray(), key.getFrom(), key.length()).write(COLON).write(SPACE)
								.write(u.getArray(), u.getFrom(), u.length()).write(CR).write(LF);
					});
        });
        out.write(CR).write(LF).flush();
    }
    
    @Override
    public String toString() {
    	StringBuilderOutputFlow flow = new StringBuilderOutputFlow();
    	fill(flow);
    	return flow.toString();
    }

    protected void copyBase(HeaderHolder toCopy) {
        headers.putAll(toCopy.headers);
        byteSize = toCopy.byteSize;
    }
}
