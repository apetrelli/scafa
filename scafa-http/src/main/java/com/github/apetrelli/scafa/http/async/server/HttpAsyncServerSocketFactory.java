package com.github.apetrelli.scafa.http.async.server;

import java.io.IOException;

import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.async.proto.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.output.DataSenderFactory;

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
