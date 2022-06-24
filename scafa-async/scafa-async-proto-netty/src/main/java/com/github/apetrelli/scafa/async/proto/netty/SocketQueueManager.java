package com.github.apetrelli.scafa.async.proto.netty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;

public class SocketQueueManager {
	
	private final ConcurrentLinkedQueue<Object> socketQueue = new ConcurrentLinkedQueue<>();

	private CompletableFuture<AsyncSocket> currentCompletableFuture = CompletableFuture.completedFuture(null);
	
	public synchronized void add(AsyncSocket socket) {
		if (!currentCompletableFuture.isDone()) {
			currentCompletableFuture.complete(socket);
		} else {
			socketQueue.offer(socket);
		}
	}
	
	public synchronized void receivedException(Throwable exc) {
		if (!currentCompletableFuture.isDone()) {
			currentCompletableFuture.completeExceptionally(exc);
		} else {
			socketQueue.offer(exc);
		}
	}
	
	public synchronized CompletableFuture<AsyncSocket> newCompletableFuture() {
		if (!currentCompletableFuture.isDone()) {
			return CompletableFuture.failedFuture(new IllegalStateException(
					"Trying to return a new CompletableFuture before completing the one before"));
		}
		CompletableFuture<AsyncSocket> completableFuture = new CompletableFuture<>();
		this.currentCompletableFuture = completableFuture;
		if (!socketQueue.isEmpty()) {
			Object result = socketQueue.remove();
			if (result instanceof AsyncSocket) {
				completableFuture.complete((AsyncSocket) result);
			} else {
				completableFuture.completeExceptionally((Throwable) result);
			}
		}
		return completableFuture;
	}
}
