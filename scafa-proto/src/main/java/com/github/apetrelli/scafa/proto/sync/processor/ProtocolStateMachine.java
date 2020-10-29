package com.github.apetrelli.scafa.proto.sync.processor;

import com.github.apetrelli.scafa.proto.processor.ProcessingContext;

public interface ProtocolStateMachine<H, P extends ProcessingContext<?>> {

	void out(P context, H handler);
}
