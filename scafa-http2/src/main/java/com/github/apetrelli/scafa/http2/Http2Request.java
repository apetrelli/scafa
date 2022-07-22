package com.github.apetrelli.scafa.http2;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.util.AsciiString;

public class Http2Request extends HttpRequest {
	
	private final Stream stream;

    public Http2Request(AsciiString method, AsciiString resource, AsciiString httpVersion, Stream stream) {
    	super(method, resource, httpVersion);
    	this.stream = stream;
    }

    public Http2Request(Http2Request toCopy) {
    	super(toCopy);
    	this.stream = toCopy.stream;
    }

    public Http2Request(Http2Request toCopy, AsciiString resource, Stream stream) {
    	super(toCopy, resource);
    	this.stream = toCopy.stream;
    }
	
}
