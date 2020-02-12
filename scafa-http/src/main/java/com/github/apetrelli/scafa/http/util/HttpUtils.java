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
package com.github.apetrelli.scafa.http.util;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.util.AsyncUtils;

public class HttpUtils {

	private static final String END_OF_CHUNKED_TRANSFER_SIZE_STRING = "0";

	private static final Logger LOG = Logger.getLogger(HttpUtils.class.getName());

	private static final byte CR = 13;

	private static final byte LF = 10;

	private HttpUtils() {
	}

	public static void sendHeader(HeaderHolder holder, AsyncSocket channelToSend,
			CompletionHandler<Void, Void> completionHandler) {
		ByteBuffer buffer = holder.toHeapByteBuffer();
		if (LOG.isLoggable(Level.FINEST)) {
			String request = new String(buffer.array(), 0, buffer.limit());
			LOG.finest("-- Raw request/response header");
			LOG.finest(request);
			LOG.finest("-- End of header --");
		}
		AsyncUtils.flipAndFlushBuffer(buffer, channelToSend, completionHandler);
	}

	public static void sendChunkSize(long size, AsyncSocket channelToSend,
			CompletionHandler<Void, Void> completionHandler) {
		String sizeString = Long.toString(size, 16);
		ByteBuffer buffer = ByteBuffer.allocate(sizeString.length() + 2);
		buffer.put(sizeString.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF);
		AsyncUtils.flipAndFlushBuffer(buffer, channelToSend, completionHandler);
	}

	public static void sendNewline(AsyncSocket channelToSend,
			CompletionHandler<Void, Void> completionHandler) {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.put(CR).put(LF);
		AsyncUtils.flipAndFlushBuffer(buffer, channelToSend, completionHandler);
	}

	public static void sendAsChunk(ByteBuffer buffer, AsyncSocket channel,
			CompletionHandler<Void, Void> completionHandler) {
		HttpUtils.sendChunkSize(buffer.remaining(), channel, new CompletionHandler<Void, Void>() {

			@Override
			public void completed(Void result, Void attachment) {
				AsyncUtils.flushBuffer(buffer, channel, new CompletionHandler<Void, Void>() {

					@Override
					public void completed(Void result, Void attachment) {
						HttpUtils.sendNewline(channel, completionHandler);
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						completionHandler.failed(exc, attachment);
					}
				});
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				completionHandler.failed(exc, attachment);
			}
		});
	}

	public static void sendEndOfChunkedTransfer(AsyncSocket channelToSend,
			CompletionHandler<Void, Void> completionHandler) {
		ByteBuffer buffer = ByteBuffer.allocate(END_OF_CHUNKED_TRANSFER_SIZE_STRING.length() + 4);
		buffer.put(END_OF_CHUNKED_TRANSFER_SIZE_STRING.getBytes(StandardCharsets.US_ASCII)).put(CR).put(LF).put(CR)
				.put(LF);
		AsyncUtils.flipAndFlushBuffer(buffer, channelToSend, completionHandler);
	}

	public static String getCurrentDateString() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}
}
