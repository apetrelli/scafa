package com.github.apetrelli.scafa.http.impl;

import java.io.IOException;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.async.AsyncServerSocket;
import com.github.apetrelli.scafa.proto.async.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.async.AsyncSocket;

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
