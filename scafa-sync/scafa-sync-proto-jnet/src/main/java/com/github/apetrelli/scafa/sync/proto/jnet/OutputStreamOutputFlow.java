package com.github.apetrelli.scafa.sync.proto.jnet;

import java.io.IOException;
import java.io.OutputStream;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.io.OutputFlow;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OutputStreamOutputFlow implements OutputFlow {

	private final OutputStream os;

	@Override
	public OutputFlow write(byte currentByte) {
		try {
			os.write(currentByte);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return this;
	}

	@Override
	public OutputFlow write(byte[] bytes, int from, int length) {
		try {
			os.write(bytes, from, length);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return this;
	}

	@Override
	public OutputFlow flush() {
		try {
			os.flush();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		return this;
	}
	
	
}
