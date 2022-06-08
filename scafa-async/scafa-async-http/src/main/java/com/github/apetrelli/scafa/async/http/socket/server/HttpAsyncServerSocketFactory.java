package com.github.apetrelli.scafa.async.http.socket.server;

import java.io.IOException;

import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HeaderHolder;

public class HttpAsyncServerSocketFactory<H extends HeaderHolder> implements AsyncServerSocketFactory<HttpAsyncSocket<H>> {

	private final AsyncServerSocketFactory<AsyncSocket> factory;
	
	private final DataSenderFactory dataSenderFactory;
	
	public HttpAsyncServerSocketFactory(AsyncServerSocketFactory<AsyncSocket> factory,
			DataSenderFactory dataSenderFactory) {
		this.factory = factory;
		this.dataSenderFactory = dataSenderFactory;
	}



	@Override
	public AsyncServerSocket<HttpAsyncSocket<H>> create() throws IOException {
		return new HttpAsyncServerSocket<>(factory.create(), dataSenderFactory);
	}

}
