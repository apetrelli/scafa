package com.github.apetrelli.scafa.http.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;

public class HttpProcessingContextFactory implements ProcessingContextFactory<HttpProcessingContext> {

	@Override
	public HttpProcessingContext create() {
		HttpProcessingContext context = new HttpProcessingContext(HttpStatus.IDLE);
        ByteBuffer buffer = ByteBuffer.allocate(16384);
        context.setBuffer(buffer);
        ByteBuffer headerBuffer = ByteBuffer.allocate(16384);
        context.setHeaderBuffer(headerBuffer);
		return context;
	}

}
