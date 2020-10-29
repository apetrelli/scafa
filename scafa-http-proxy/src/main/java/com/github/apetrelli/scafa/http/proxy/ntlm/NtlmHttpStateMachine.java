package com.github.apetrelli.scafa.http.proxy.ntlm;

import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.processor.ProtocolStateMachine;

public class NtlmHttpStateMachine implements ProtocolStateMachine<HttpHandler, NtlmHttpProcessingContext> {

	private final HttpStateMachine delegate;

	public NtlmHttpStateMachine(HttpStateMachine delegate) {
		this.delegate = delegate;
	}

	@Override
	public CompletableFuture<Void> out(NtlmHttpProcessingContext context, HttpHandler handler) {
		return delegate.out(context, handler);
	}

}
