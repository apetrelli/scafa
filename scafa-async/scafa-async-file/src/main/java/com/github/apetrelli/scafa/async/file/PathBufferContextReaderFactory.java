package com.github.apetrelli.scafa.async.file;

import java.nio.file.Path;

public interface PathBufferContextReaderFactory {

	PathBufferContextReader create(Path payload);
}
