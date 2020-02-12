package com.github.apetrelli.scafa.http.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsyncSocketFactory;
import com.github.apetrelli.scafa.proto.aio.impl.DirectAsyncSocket;

public class DirectHttpAsyncSocketFactory implements AsyncSocketFactory<HttpAsyncSocket> {

	private DataSenderFactory dataSenderFactory;

	public DirectHttpAsyncSocketFactory(com.github.apetrelli.scafa.http.output.DataSenderFactory dataSenderFactory) {
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public HttpAsyncSocket create(AsynchronousSocketChannel channel) {
		AsyncSocket socket = new DirectAsyncSocket(channel);
		return new DirectHttpAsyncSocket(socket, dataSenderFactory);
	}

}
