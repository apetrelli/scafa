package com.github.apetrelli.scafa.proto.sync;

public interface RunnableStarter extends AutoCloseable {

	void start(Runnable runnable);
}
