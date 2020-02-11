package com.github.apetrelli.scafa.proto.aio;

import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IgnoringCompletionHandler<V, A> implements CompletionHandler<V, A> {
	
	public static final IgnoringCompletionHandler<Void, Void> INSTANCE = new IgnoringCompletionHandler<>();
	
	private static final Logger LOG = Logger.getLogger(IgnoringCompletionHandler.class.getName());
	
	@Override
	public void completed(V result, A attachment) {
		LOG.finest("Completed ignored outcome of operation");
	}

	@Override
	public void failed(Throwable exc, A attachment) {
		LOG.log(Level.WARNING, "Failed ignored outcome of operation", exc);
	}

}
