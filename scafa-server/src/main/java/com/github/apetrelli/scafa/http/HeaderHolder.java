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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class HeaderHolder {

    protected static final byte CR = 13;

    protected static final byte LF = 10;

    protected static final byte SPACE = 32;

    private static final byte COLON = 58;

    private static final byte A_UPPER = 65;

    private static final byte Z_UPPER = 90;

    private static final byte A_LOWER = 97;

    private static final byte Z_LOWER = 122;

    private static final byte CAPITALIZE_CONST = A_LOWER - A_UPPER;

    private Map<String, List<String>> headers = new LinkedHashMap<>();

    protected int byteSize;

    public void addHeader(String header, String value) {
        List<String> values = headers.get(header);
        if (values == null) {
            values = new ArrayList<>();
            headers.put(header, values);
        }
        values.add(value);
        byteSize += header.length() + 2 + value.length() + 2;
    }

    public void setHeader(String header, String value) {
        removeHeader(header);
        addHeader(header, value);
    }

    public void removeHeader(String header) {
        List<String> values = headers.remove(header);
        if (values != null) {
            values.stream().forEach(t -> {
                byteSize -= header.length() + 2 + t.length() + 2;
            });
        }
    }

    public String getHeader(String header) {
        List<String> values = headers.get(header);
        String retValue = null;
        if (values != null && !values.isEmpty()) {
            retValue = values.get(0);
        }
        return retValue;
    }

    public List<String> getHeaders(String header) {
        List<String> values = headers.get(header);
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }

    public abstract ByteBuffer toHeapByteBuffer();
    
    protected void loadInBuffer(ByteBuffer buffer) {
        Charset charset = StandardCharsets.US_ASCII;
        headers.entrySet().stream().forEach(t -> {
            String key = t.getKey();
            byte[] convertedKey = putCapitalized(key);
            t.getValue().forEach(u -> {
                buffer.put(convertedKey).put(COLON).put(SPACE).put(u.getBytes(charset)).put(CR).put(LF);
            });
        });
        buffer.put(CR).put(LF);
    }

    protected void copyBase(HeaderHolder toCopy) {
        headers.putAll(toCopy.headers);
        byteSize = toCopy.byteSize;
    }

    private static byte[] putCapitalized(String string) {
        byte[] array = string.getBytes(StandardCharsets.US_ASCII);
        byte[] converted = new byte[array.length];
        boolean capitalize = true;
        for (int i = 0; i < array.length; i++) {
            byte currentByte = array[i];
            if (capitalize) {
                if (currentByte >= A_LOWER && currentByte <= Z_LOWER) {
                    currentByte -= CAPITALIZE_CONST;
                    capitalize = false;
                } else if (currentByte >= A_UPPER && currentByte <= Z_UPPER) {
                    capitalize = false;
                }
            } else {
                if (currentByte >= A_UPPER && currentByte <= Z_UPPER) {
                    currentByte += CAPITALIZE_CONST;
                } else if (currentByte < A_LOWER || currentByte > Z_LOWER) {
                    capitalize = true;
                }
            }
            converted[i] = currentByte;
        }
        return converted;
    }
}
