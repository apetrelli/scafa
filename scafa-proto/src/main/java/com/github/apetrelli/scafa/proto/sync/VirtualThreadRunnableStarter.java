package com.github.apetrelli.scafa.proto.sync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VirtualThreadRunnableStarter implements RunnableStarter {

	private static final Logger LOG = Logger.getLogger(VirtualThreadRunnableStarter.class.getName());
	
	private ExecutorService scheduler = Executors.newVirtualThreadExecutor();

	@Override
	public Future<?> start(Runnable runnable) {
		return scheduler.submit(runnable);
	}

	@Override
	public void close() {
		try {
			scheduler.shutdown();
			scheduler.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.log(Level.INFO, "Error during waiting for termination", e);
			Thread.currentThread().interrupt();
		}
	}
}
