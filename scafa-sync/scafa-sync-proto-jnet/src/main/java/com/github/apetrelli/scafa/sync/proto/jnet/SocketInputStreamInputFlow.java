package com.github.apetrelli.scafa.sync.proto.jnet;

import java.io.IOException;
import java.io.InputStream;

import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.proto.io.FlowClosedException;
import com.github.apetrelli.scafa.proto.io.InputFlow;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SocketInputStreamInputFlow implements InputFlow {

	private interface IOOperation {

		int execute() throws IOException;
	}

	private final InputStream is;
	
	private final FlowBuffer flow = new FlowBuffer(8192);
	
	private int lastResult = 0;
	
	@Override
	public byte read() {
		int result = execute(() -> is.read());
		return (byte) result;
	}

	@Override
	public FlowBuffer read(long maxLength) {
		int result = execute(() -> is.read(flow.array(), 0, (int) Math.min(flow.maxLength(), maxLength)));
		flow.length(result);
		return flow;
	}

	@Override
	public FlowBuffer readBuffer() {
		int result = execute(() -> is.read(flow.array(), 0, flow.maxLength()));
		flow.length(result);
		return flow;
	}

	@Override
	public boolean hasData() {
		return lastResult >= 0;
	}
	
	private int execute(IOOperation operation) {
		try {
			if (lastResult >= 0) {
				lastResult = operation.execute();
			}
			if (lastResult < 0) {
				throw new FlowClosedException("The flow is closed");
			}
			return lastResult;
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
		
	}
}
