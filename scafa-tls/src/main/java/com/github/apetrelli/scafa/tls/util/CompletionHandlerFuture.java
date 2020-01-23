package com.github.apetrelli.scafa.tls.util;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletionHandlerFuture<V, A> implements CompletionHandler<V, A> {
	
	private class LatchedFuture implements Future<V> {

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return latch.getCount() == 0L;
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			latch.await();
			return getResult();
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			latch.await(timeout, unit);
			return getResult();
		}

		private V getResult() throws ExecutionException {
			if (exc == null) {
				return result;
			} else {
				if (exc instanceof RuntimeException) {
					throw (RuntimeException) exc;
				} else {
					throw new ExecutionException(exc);
				}
			}
		}
		
	}

	private V result;
	
	private Throwable exc;
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	private Future<V> future = new LatchedFuture();
	
	@Override
	public void completed(V result, A attachment) {
		this.result = result;
		latch.countDown();
	}

	@Override
	public void failed(Throwable exc, A attachment) {
		this.exc = exc;
		latch.countDown();
	}
	
	public Future<V> getFuture() {
		return future;
	}
}
