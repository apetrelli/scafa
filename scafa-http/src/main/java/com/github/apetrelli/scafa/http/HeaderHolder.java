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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.proto.util.AsciiString;

public abstract class HeaderHolder {

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    private static final byte COLON = 58;

    private Map<HeaderName, List<AsciiString>> headers = new LinkedHashMap<>();

    protected int byteSize;

    public void addHeader(HeaderName header, AsciiString value) {
    	List<AsciiString> values = headers.computeIfAbsent(header, x -> new ArrayList<>());
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

    public abstract void fill(ByteBuffer buffer);

    public abstract ByteBuffer toHeapByteBuffer();

    protected void loadInBuffer(ByteBuffer buffer) {
        headers.entrySet().stream().forEach(t -> {
			t.getValue().forEach(
					u -> buffer.put(t.getKey().getArray()).put(COLON).put(SPACE).put(u.getArray()).put(CR).put(LF));
        });
        buffer.put(CR).put(LF);
    }

    protected void copyBase(HeaderHolder toCopy) {
        headers.putAll(toCopy.headers);
        byteSize = toCopy.byteSize;
    }
}
