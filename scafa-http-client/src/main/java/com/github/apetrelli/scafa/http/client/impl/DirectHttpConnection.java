package com.github.apetrelli.scafa.http.client.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.client.impl.AbstractClientConnection;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class DirectHttpConnection extends AbstractClientConnection<HttpAsyncSocket<HttpRequest>> implements HttpClientConnection {

    private ClientPipelineHttpHandler responseHandler;

    private MappedHttpConnectionFactory connectionFactory;

	public DirectHttpConnection(HttpAsyncSocket<HttpRequest> socket, MappedHttpConnectionFactory connectionFactory) {
		super(socket); // No binding ATM.
		this.connectionFactory = connectionFactory;
		responseHandler = new ClientPipelineHttpHandler(this);
	}
	
	@Override
	public void prepare(HttpRequest request, HttpClientHandler clientHandler) {
        responseHandler.add(request, clientHandler, this);
	}
	
	@Override
	public CompletableFuture<Void> sendHeader(HttpRequest request) {
        return socket.sendHeader(request);
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
		HttpProcessingContextFactory contextFactory = new HttpProcessingContextFactory();
		StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(
				new HttpStateMachine());
		Processor<HttpHandler> processor = new DefaultProcessor<>(socket, inputProcessorFactory, contextFactory);
		processor.process(responseHandler);
	}
}
