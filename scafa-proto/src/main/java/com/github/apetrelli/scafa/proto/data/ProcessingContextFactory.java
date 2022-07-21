package com.github.apetrelli.scafa.proto.data;

import com.github.apetrelli.scafa.proto.io.InputFlow;

public interface ProcessingContextFactory<P extends Input> {

	P create(InputFlow in);
}
