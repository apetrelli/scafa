package com.github.apetrelli.scafa.proto.aio;

import java.nio.channels.CompletionHandler;

public class DelegateCompletionHandler<V, A> implements CompletionHandler<V, A> {

	protected CompletionHandler<?, A> parent;

	public DelegateCompletionHandler(CompletionHandler<?, A> parent) {
		this.parent = parent;
	}

	@Override
	public void completed(V result, A attachment) {
		parent.completed(null, attachment);
	}

	@Override
	public void failed(Throwable exc, A attachment) {
		parent.failed(exc, attachment);
	}

}
