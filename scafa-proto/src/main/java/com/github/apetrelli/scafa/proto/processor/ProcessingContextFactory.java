package com.github.apetrelli.scafa.proto.processor;

import java.io.InputStream;

public interface ProcessingContextFactory<P extends Input> {

	P create(InputStream stream);
}
