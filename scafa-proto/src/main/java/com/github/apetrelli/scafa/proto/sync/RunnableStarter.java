package com.github.apetrelli.scafa.proto.sync;

public interface RunnableStarter {

	void start(Runnable runnable);
	
	void shutdown();
}
