package com.github.apetrelli.scafa.proto.sync;

import java.util.concurrent.Future;

public interface RunnableStarter extends AutoCloseable {

	Future<?> start(Runnable runnable);
}
