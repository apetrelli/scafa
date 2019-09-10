package com.github.apetrelli.scafa.http.output;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.http.HeaderHolder;
import com.github.apetrelli.scafa.proto.output.DataSender;

public interface DataSenderFactory {

	DataSender create(HeaderHolder holder, AsynchronousSocketChannel channel);
}
