package com.github.apetrelli.scafa.aio;

import java.nio.channels.CompletionHandler;

public abstract class DelegateFailureCompletionHandler<V, A> implements CompletionHandler<V, A> {

	protected CompletionHandler<?, ?> parent;

	public DelegateFailureCompletionHandler(CompletionHandler<?, ?> parent) {
		this.parent = parent;
	}

	@Override
	public void failed(Throwable exc, A attachment) {
		parent.failed(exc, null);
	}

}
