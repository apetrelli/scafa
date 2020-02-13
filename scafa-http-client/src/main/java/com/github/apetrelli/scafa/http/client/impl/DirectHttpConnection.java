package com.github.apetrelli.scafa.http.client.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.client.impl.AbstractClientConnection;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class DirectHttpConnection extends AbstractClientConnection<HttpAsyncSocket> implements HttpClientConnection {

    private ClientPipelineHttpHandler responseHandler;

    private MappedHttpConnectionFactory connectionFactory;

	public DirectHttpConnection(HttpAsyncSocket socket, MappedHttpConnectionFactory connectionFactory) {
		super(socket); // No binding ATM.
		this.connectionFactory = connectionFactory;
		responseHandler = new ClientPipelineHttpHandler(this);
	}
	
	@Override
	public void prepare(HttpRequest request, HttpClientHandler clientHandler) {
        responseHandler.add(request, clientHandler, this);
	}

	@Override
	public void sendHeader(HttpRequest request, CompletionHandler<Void, Void> completionHandler) {
        socket.sendHeader(request, completionHandler);
	}
	
	@Override
	public void sendData(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
	    socket.sendData(buffer, completionHandler);
	}

	@Override
	public void end(CompletionHandler<Void, Void> completionHandler) {
	    socket.endData(completionHandler);
	}
	
	@Override
	public void disconnect(CompletionHandler<Void, Void> handler) {
        connectionFactory.dispose(socket.getAddress());
	    super.disconnect(handler);
	}

	@Override
    protected void prepareChannel() {
		HttpProcessingContextFactory contextFactory = new HttpProcessingContextFactory();
		StatefulInputProcessorFactory<HttpHandler, HttpStatus, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(
				new HttpStateMachine());
		Processor<HttpHandler> processor = new DefaultProcessor<>(socket, inputProcessorFactory, contextFactory);
		processor.process(responseHandler);
	}
}
