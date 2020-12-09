package com.github.apetrelli.scafa.http;

import com.github.apetrelli.scafa.proto.util.AsciiString;

public class HttpHeaders {

	private HttpHeaders() {
	}
	
	public static final HeaderName CONTENT_LENGTH = new HeaderName("Content-Length");

	public static final HeaderName TRANSFER_ENCODING = new HeaderName("Transfer-Encoding");

	public static final HeaderName CONNECTION = new HeaderName("Connection");
    
    public static final HeaderName HOST = new HeaderName("Host");

	public static final HeaderName PROXY_CONNECTION = new HeaderName("Proxy-Connection");

	public static final HeaderName PROXY_AUTHENTICATE = new HeaderName("Proxy-Authenticate");

    public static final HeaderName PROXY_AUTHORIZATION = new HeaderName("Proxy-Authorization");

    public static final HeaderName CONTENT_TYPE = new HeaderName("Content-Type");
	
    public static final HeaderName SERVER = new HeaderName("Server");

	public static final HeaderName LOCATION = new HeaderName("Location");

	public static final HeaderName DATE = new HeaderName("Date");

	public static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

	public static final AsciiString CONTENT_LENGTH_0 = new AsciiString("0");

	public static final AsciiString PROXY_AUTHENTICATE_NTLM = new AsciiString("NTLM");

    public static final AsciiString CLOSE_CONNECTION = new AsciiString("close");

	public static final AsciiString CHUNKED = new AsciiString("chunked");

	public static final AsciiString TEXT_PLAIN = new AsciiString("text/plain");
	
	public static final AsciiString SCAFA = new AsciiString("Scafa");

	public static final AsciiString GET = new AsciiString("GET");
	
	public static final AsciiString CONNECT = new AsciiString("CONNECT");

	public static final AsciiString OK = new AsciiString("OK");

	public static final AsciiString FOUND = new AsciiString("Found");

	public static final AsciiString HTTP_1_1 = new AsciiString("HTTP/1.1");
}
