package com.github.apetrelli.scafa.async.file.aio;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.apetrelli.scafa.async.file.PathBufferContextReader;
import com.github.apetrelli.scafa.async.file.PathBufferContextReaderFactory;
import com.github.apetrelli.scafa.async.file.PathIOException;

public class AioPathBufferContextReaderFactory implements PathBufferContextReaderFactory {

	@Override
	public PathBufferContextReader create(Path payload) {
		AsynchronousFileChannel fileChannel = null;
		try {
			fileChannel = AsynchronousFileChannel.open(payload, StandardOpenOption.READ);
			return new AioPathBufferContextReader(fileChannel);
		} catch (IOException e) {
			IOUtils.closeQuietly(fileChannel);
			throw new PathIOException(e);
		}
	}

}
