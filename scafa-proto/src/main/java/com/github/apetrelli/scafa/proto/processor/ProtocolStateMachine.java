package com.github.apetrelli.scafa.proto.processor;

import com.github.apetrelli.scafa.proto.data.impl.ProcessingContext;

public interface ProtocolStateMachine<H, P extends ProcessingContext<?>> {

	void out(P context, H handler);
}
