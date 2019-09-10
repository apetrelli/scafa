package com.github.apetrelli.scafa.http.client.impl.internal;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HeaderHolder;

public interface DataSenderFactory {

	DataSender create(HeaderHolder holder, AsynchronousSocketChannel channel);
}
