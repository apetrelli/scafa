package com.github.apetrelli.scafa.sync.proto;

import java.util.concurrent.Future;

public interface RunnableStarter extends AutoCloseable {

	Future<?> start(Runnable runnable);
}
