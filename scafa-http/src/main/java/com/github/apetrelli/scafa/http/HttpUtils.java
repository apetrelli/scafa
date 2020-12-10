package com.github.apetrelli.scafa.http;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HttpUtils {
	
	private static final byte SPACE = 32;
	
	private static final byte COMMA = 44;
	
	private static final byte COLON = 58;
	
	private static final byte ZERO = 48;
	
	private static final AsciiString GMT = new AsciiString("GMT");
	
	private static final AsciiString[] DAYS_OF_WEEK = new AsciiString[] { new AsciiString("Mon"), new AsciiString("Tue"),
			new AsciiString("Wed"), new AsciiString("Thu"), new AsciiString("Fri"), new AsciiString("Sat"),
			new AsciiString("Sun") };
	
	private static final AsciiString[] MONTHS = new AsciiString[] { new AsciiString("Jan"), new AsciiString("Feb"),
			new AsciiString("Mar"), new AsciiString("Apr"), new AsciiString("May"), new AsciiString("Jun"),
			new AsciiString("Jul"), new AsciiString("Aug"), new AsciiString("Sep"), new AsciiString("Oct"),
			new AsciiString("Nov"), new AsciiString("Dec") };
	
	private HttpUtils() {
	}
	
	private static final ZoneId HTTP_TIME_ZONE = ZoneId.of("GMT");
	
	public static AsciiString toHttpDate(ZonedDateTime instant) {
		byte[] bytes = new byte[29];
		AsciiString dayOfWeek = DAYS_OF_WEEK[instant.get(ChronoField.DAY_OF_WEEK) - 1];
		System.arraycopy(dayOfWeek.getArray(), 0, bytes, 0, 3);
		bytes[3] = COMMA;
		bytes[4] = SPACE;
		putNumber(instant.get(ChronoField.DAY_OF_MONTH), bytes, 6, 2);
		bytes[7] = SPACE;
		AsciiString month = MONTHS[instant.get(ChronoField.MONTH_OF_YEAR) - 1];
		System.arraycopy(month.getArray(), 0, bytes, 8, 3);
		bytes[11] = SPACE;
		putNumber(instant.get(ChronoField.YEAR), bytes, 15, 4);
		bytes[16] = SPACE;
		putNumber(instant.get(ChronoField.HOUR_OF_DAY), bytes, 18, 2);
		bytes[19] = COLON;
		putNumber(instant.get(ChronoField.MINUTE_OF_HOUR), bytes, 21, 2);
		bytes[22] = COLON;
		putNumber(instant.get(ChronoField.SECOND_OF_MINUTE), bytes, 24, 2);
		bytes[25] = SPACE;
		System.arraycopy(GMT.getArray(), 0, bytes, 26, 3);
		return new AsciiString(bytes);
	}

	public static AsciiString getCurrentHttpDate() {
		return toHttpDate(ZonedDateTime.now(HTTP_TIME_ZONE));
	}

	private static void putNumber(int value, byte[] bytes, int startPosition, int length) {
		for (int i = length; i > 0; i--, startPosition--) {
			byte toPut = (byte) (value % 10 + ZERO);
			bytes[startPosition] = toPut;
			value /= 10;
		}
	}
}
