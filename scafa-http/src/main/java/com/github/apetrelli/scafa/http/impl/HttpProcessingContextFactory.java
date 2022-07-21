package com.github.apetrelli.scafa.http.impl;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.data.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.io.InputFlow;

public class HttpProcessingContextFactory implements ProcessingContextFactory<HttpProcessingContext> {

	@Override
	public HttpProcessingContext create(InputFlow in) {
		HttpProcessingContext context = new HttpProcessingContext(in, HttpStatus.IDLE);
        ByteBuffer headerBuffer = ByteBuffer.allocate(16384);
        context.setHeaderBuffer(headerBuffer);
		return context;
	}

}
