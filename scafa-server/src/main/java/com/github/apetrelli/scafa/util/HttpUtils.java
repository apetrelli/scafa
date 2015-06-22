package com.github.apetrelli.scafa.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HttpUtils {

    private static final byte CR = 13;

    private static final byte LF = 10;

    protected static final byte SPACE = 32;

    private static final byte COLON = 58;

    private static final byte A_UPPER = 65;

    private static final byte Z_UPPER = 90;

    private static final byte A_LOWER = 97;

    private static final byte Z_LOWER = 122;

    private static final byte CAPITALIZE_CONST = A_LOWER - A_UPPER;

    private HttpUtils() {
    }

    public static Integer sendHeader(String requestLine, Map<String, List<String>> headers, ByteBuffer buffer,
            AsynchronousSocketChannel channelToSend) throws IOException {
        Charset charset = StandardCharsets.US_ASCII;
        buffer.put(requestLine.getBytes(charset)).put(CR).put(LF);
        headers.entrySet().stream().forEach(t -> {
            String key = t.getKey();
            byte[] convertedKey = putCapitalized(key);
            t.getValue().forEach(u -> {
                buffer.put(convertedKey).put(COLON).put(SPACE).put(u.getBytes(charset)).put(CR).put(LF);
            });
        });
        buffer.put(CR).put(LF);
        return flushBuffer(buffer, channelToSend);
    }

    public static Integer flushBuffer(ByteBuffer buffer, AsynchronousSocketChannel channelToSend) throws IOException {
        buffer.flip();
        Integer result = getFuture(channelToSend.write(buffer));
        buffer.clear();
        return result;
    }

    public static Integer sendChunkSize(long size, ByteBuffer buffer, AsynchronousSocketChannel channelToSend)
            throws IOException {
        buffer.clear();
        buffer.put(Long.toString(size, 16).getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
        return flushBuffer(buffer, channelToSend);
    }

    public static Integer sendNewline(ByteBuffer buffer, AsynchronousSocketChannel channelToSend)
            throws IOException {
        buffer.clear();
        buffer.put(CR).put(LF);
        return flushBuffer(buffer, channelToSend);
    }

    public static <T> T getFuture(Future<T> future) throws IOException {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Future problem", e);
        }
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
