package com.github.apetrelli.scafa.sync.proto.loom;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.github.apetrelli.scafa.sync.proto.RunnableStarter;

import lombok.extern.java.Log;

@Log
public class VirtualThreadRunnableStarter implements RunnableStarter {
	
	@SuppressWarnings("preview")
	private final ExecutorService scheduler = Executors.newVirtualThreadPerTaskExecutor();

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
			log.log(Level.INFO, "Error during waiting for termination", e);
			Thread.currentThread().interrupt();
		}
	}
}
