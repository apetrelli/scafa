package com.github.apetrelli.scafa.http.proxy.ntlm;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.processor.ProcessingContextFactory;

public class NtlmHttpProcessingContextFactory implements ProcessingContextFactory<NtlmHttpProcessingContext> {

	@Override
	public NtlmHttpProcessingContext create() {
		NtlmHttpProcessingContext context = new NtlmHttpProcessingContext(HttpStatus.IDLE);
        ByteBuffer buffer = ByteBuffer.allocate(16384);
        context.setBuffer(buffer);
		return context;
	}

}
