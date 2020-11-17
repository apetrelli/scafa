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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class HeaderHolder {

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    private static final byte COLON = 58;

    private Map<String, String> capitalized2normalHeader = new LinkedHashMap<String, String>();

    private Map<String, List<String>> headers = new LinkedHashMap<>();

    protected int byteSize;

    public void addHeader(String header, String value) {
        String capitalizedHeader = header.toUpperCase();
        if (!capitalized2normalHeader.containsKey(capitalizedHeader)) {
            capitalized2normalHeader.put(capitalizedHeader, header);
        }
        List<String> values = headers.get(capitalizedHeader);
        if (values == null) {
            values = new ArrayList<>();
            headers.put(capitalizedHeader, values);
        }
        values.add(value);
        byteSize += capitalizedHeader.length() + 2 + value.length() + 2;
    }

    public void setHeader(String header, String value) {
        removeHeader(header);
        addHeader(header, value);
    }

    public void removeHeader(String header) {
        String capitalizedHeader = header.toUpperCase();
        capitalized2normalHeader.remove(capitalizedHeader);
        List<String> values = headers.remove(capitalizedHeader);
        if (values != null) {
            values.stream().forEach(t -> {
                byteSize -= header.length() + 2 + t.length() + 2;
            });
        }
    }

    public String getHeader(String header) {
        List<String> values = headers.get(header.toUpperCase());
        String retValue = null;
        if (values != null && !values.isEmpty()) {
            retValue = values.get(0);
        }
        return retValue;
    }

    public List<String> getHeaders(String header) {
        List<String> values = headers.get(header.toUpperCase());
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }

    public Collection<String> getHeaderNames() {
    	return Collections.unmodifiableCollection(capitalized2normalHeader.values());
    }

    public abstract void fill(ByteBuffer buffer);

    public abstract ByteBuffer toHeapByteBuffer();

    protected void loadInBuffer(ByteBuffer buffer) {
        Charset charset = StandardCharsets.US_ASCII;
        headers.entrySet().stream().forEach(t -> {
            String key = t.getKey();
            byte[] originalKey = capitalized2normalHeader.get(key).getBytes(StandardCharsets.US_ASCII);
			t.getValue().forEach(
					u -> buffer.put(originalKey).put(COLON).put(SPACE).put(u.getBytes(charset)).put(CR).put(LF));
        });
        buffer.put(CR).put(LF);
    }

    protected void copyBase(HeaderHolder toCopy) {
        headers.putAll(toCopy.headers);
        capitalized2normalHeader.putAll(toCopy.capitalized2normalHeader);
        byteSize = toCopy.byteSize;
    }
}
