package com.github.apetrelli.scafa.sync.proto.thread;

import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.RunnableStarterFactory;

public class ThreadRunnableStarterFactory implements RunnableStarterFactory {

	@Override
	public RunnableStarter create() {
		return new ThreadRunnableStarter();
	}

}
