package com.github.apetrelli.scafa.http;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class HttpUtils {
	
	private HttpUtils() {
	}
	
	private static final ZoneId HTTP_TIME_ZONE = ZoneId.of("GMT");
	
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

	public static String toHttpDate(Instant instant) {
		return dateTimeFormatter.format(ZonedDateTime.ofInstant(instant, HTTP_TIME_ZONE));
	}

	public static String getCurrentHttpDate() {
		return dateTimeFormatter.format(ZonedDateTime.now(HTTP_TIME_ZONE));
	}
}
