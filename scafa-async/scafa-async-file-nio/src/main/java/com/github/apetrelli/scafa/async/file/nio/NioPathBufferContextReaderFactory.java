package com.github.apetrelli.scafa.async.file.nio;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.apetrelli.scafa.async.file.PathBufferContextReader;
import com.github.apetrelli.scafa.async.file.PathBufferContextReaderFactory;
import com.github.apetrelli.scafa.async.file.PathIOException;

public class NioPathBufferContextReaderFactory implements PathBufferContextReaderFactory {

	@Override
	public PathBufferContextReader create(Path payload) {
		FileChannel fileChannel = null;
		try {
			fileChannel = FileChannel.open(payload, StandardOpenOption.READ);
			return new NioPathBufferContextReader(fileChannel);
		} catch (IOException e) {
			IOUtils.closeQuietly(fileChannel);
			throw new PathIOException(e);
		}
	}

}
