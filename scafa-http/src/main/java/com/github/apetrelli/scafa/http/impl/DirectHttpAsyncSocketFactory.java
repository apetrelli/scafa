package com.github.apetrelli.scafa.http.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocketFactory;
import com.github.apetrelli.scafa.proto.aio.impl.DirectAsyncSocket;

public class DirectHttpAsyncSocketFactory<H extends HeaderHolder> implements AsyncSocketFactory<HttpAsyncSocket<H>> {

	private DataSenderFactory dataSenderFactory;

	public DirectHttpAsyncSocketFactory(com.github.apetrelli.scafa.http.output.DataSenderFactory dataSenderFactory) {
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public HttpAsyncSocket<H> create(AsynchronousSocketChannel channel) {
		AsyncSocket socket = new DirectAsyncSocket(channel);
		return new DirectHttpAsyncSocket<H>(socket, dataSenderFactory);
	}

}
