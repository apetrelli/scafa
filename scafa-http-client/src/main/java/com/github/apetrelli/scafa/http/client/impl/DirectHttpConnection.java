package com.github.apetrelli.scafa.http.client.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.proto.client.AbstractClientConnection;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.HttpHandler;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class DirectHttpConnection extends AbstractClientConnection<HttpAsyncSocket<HttpRequest>> implements HttpClientConnection {

    private ClientPipelineHttpHandler responseHandler;

    private MappedHttpConnectionFactory connectionFactory;
    
    private ProcessorFactory<HttpHandler, AsyncSocket> processorFactory;

	public DirectHttpConnection(HttpAsyncSocket<HttpRequest> socket, MappedHttpConnectionFactory connectionFactory,
			ProcessorFactory<HttpHandler, AsyncSocket> processorFactory) {
		super(socket); // No binding ATM.
		this.connectionFactory = connectionFactory;
		this.processorFactory = processorFactory;
		responseHandler = new ClientPipelineHttpHandler(this);
	}
	
	@Override
	public void prepare(HttpRequest request, HttpClientHandler clientHandler) {
        responseHandler.add(request, clientHandler);
	}
	
	@Override
	public CompletableFuture<Void> sendHeader(HttpRequest request, ByteBuffer writeBuffer) {
        return socket.sendHeader(request, writeBuffer);
	}
	
	@Override
	public CompletableFuture<Void> sendData(ByteBuffer buffer) {
	    return socket.sendData(buffer);
	}
	
	@Override
	public CompletableFuture<Void> endData() {
	    return socket.endData();
	}
	
	@Override
	public CompletableFuture<Void> disconnect() {
        connectionFactory.dispose(socket.getAddress());
	    return super.disconnect();
	}

	@Override
    protected void prepareChannel() {
		Processor<HttpHandler> processor = processorFactory.create(socket);
		processor.process(responseHandler);
	}
}
