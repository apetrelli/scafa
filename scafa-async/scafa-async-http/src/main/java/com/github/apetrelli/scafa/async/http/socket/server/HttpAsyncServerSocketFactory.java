package com.github.apetrelli.scafa.async.http.socket.server;

import java.io.IOException;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HeaderHolder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpAsyncServerSocketFactory<H extends HeaderHolder> implements AsyncServerSocketFactory<HttpAsyncSocket<H>> {

	private final AsyncServerSocketFactory<AsyncSocket> factory;
	
	private final DataSenderFactory dataSenderFactory;

	@Override
	public AsyncServerSocket<HttpAsyncSocket<H>> create() throws IOException {
		return new HttpAsyncServerSocket<>(factory.create(), dataSenderFactory);
	}

}
