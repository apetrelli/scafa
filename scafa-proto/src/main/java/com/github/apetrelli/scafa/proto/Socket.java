package com.github.apetrelli.scafa.proto;

import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.io.InputFlow;
import com.github.apetrelli.scafa.proto.io.OutputFlow;

public interface Socket extends AutoCloseable {

	HostPort getAddress();
	
	void connect();
	
	void disconnect();
	
	InputFlow in();
	
	OutputFlow out();

	boolean isOpen();
	
	@Override
	default void close() {
		disconnect();
	}
}
