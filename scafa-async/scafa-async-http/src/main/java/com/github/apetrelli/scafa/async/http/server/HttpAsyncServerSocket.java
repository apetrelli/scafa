package com.github.apetrelli.scafa.async.http.server;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.direct.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HeaderHolder;

public class HttpAsyncServerSocket<H extends HeaderHolder> implements AsyncServerSocket<HttpAsyncSocket<H>> {

	private final AsyncServerSocket<AsyncSocket> serverSocket;

	private final DataSenderFactory dataSenderFactory;

	public HttpAsyncServerSocket(AsyncServerSocket<AsyncSocket> serverSocket, DataSenderFactory dataSenderFactory) {
		this.serverSocket = serverSocket;
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public CompletableFuture<HttpAsyncSocket<H>> accept() {
		return serverSocket.accept().thenApply(x -> new DirectHttpAsyncSocket<>(x, dataSenderFactory));
	}

	@Override
	public void close() throws IOException {
		serverSocket.close();
	}

}
