package com.github.apetrelli.scafa.http.proxy.ntlm;

import java.nio.ByteBuffer;

import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.proto.data.ProcessingContextFactory;
import com.github.apetrelli.scafa.proto.io.InputFlow;

public class NtlmHttpProcessingContextFactory implements ProcessingContextFactory<NtlmHttpProcessingContext> {

	@Override
	public NtlmHttpProcessingContext create(InputFlow in) {
		NtlmHttpProcessingContext context = new NtlmHttpProcessingContext(in, HttpStatus.IDLE);
        ByteBuffer headerBuffer = ByteBuffer.allocate(16384);
        context.setHeaderBuffer(headerBuffer);
		return context;
	}

}
