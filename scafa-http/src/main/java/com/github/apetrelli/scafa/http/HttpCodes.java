package com.github.apetrelli.scafa.http;

import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HttpCodes {

	private HttpCodes() {}
	
	public static final AsciiString OK = new AsciiString("200");
	
	public static final AsciiString FOUND = new AsciiString("302");
	
	public static final AsciiString BAD_REQUEST = new AsciiString("400");
	
	public static final AsciiString NOT_FOUND = new AsciiString("404");
	
	public static final AsciiString METHOD_NOT_ALLOWED = new AsciiString("405");
	
	public static final AsciiString PROXY_AUTHENTICATION_REQUIRED = new AsciiString("407");
	
	public static final AsciiString INTERNAL_SERVER_ERROR = new AsciiString("500");
}
