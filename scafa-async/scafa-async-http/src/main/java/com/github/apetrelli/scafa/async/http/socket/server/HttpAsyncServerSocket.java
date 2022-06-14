package com.github.apetrelli.scafa.async.http.socket.server;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.http.socket.direct.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HeaderHolder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpAsyncServerSocket<H extends HeaderHolder> implements AsyncServerSocket<HttpAsyncSocket<H>> {

	private final AsyncServerSocket<AsyncSocket> serverSocket;

	private final DataSenderFactory dataSenderFactory;

	@Override
	public CompletableFuture<HttpAsyncSocket<H>> accept() {
		return serverSocket.accept().thenApply(x -> new DirectHttpAsyncSocket<>(x, dataSenderFactory));
	}

	@Override
	public void close() throws IOException {
		serverSocket.close();
	}

}
