package com.github.apetrelli.scafa.http.async.server;

import java.io.IOException;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.async.socket.AsyncServerSocket;
import com.github.apetrelli.scafa.proto.async.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.async.socket.AsyncSocket;

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
