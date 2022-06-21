package com.github.apetrelli.scafa.sync.proto.loom;

import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.RunnableStarterFactory;

public class VirtualThreadRunnableStarterFactory implements RunnableStarterFactory {

	@Override
	public RunnableStarter create() {
		return new VirtualThreadRunnableStarter();
	}

}
