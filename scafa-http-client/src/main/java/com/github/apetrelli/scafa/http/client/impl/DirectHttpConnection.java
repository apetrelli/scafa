package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.util.HttpUtils;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.client.impl.AbstractClientConnection;
import com.github.apetrelli.scafa.proto.output.DataSender;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class DirectHttpConnection extends AbstractClientConnection implements HttpClientConnection {

    private ClientPipelineHttpHandler responseHandler;

    private MappedHttpConnectionFactory connectionFactory;

    private DataSenderFactory dataSenderFactory;

	public DirectHttpConnection(HostPort socketAddress, MappedHttpConnectionFactory connectionFactory, DataSenderFactory dataSenderFactory) {
		super(socketAddress, null, false); // No binding ATM.
		this.connectionFactory = connectionFactory;
		this.dataSenderFactory = dataSenderFactory;
		responseHandler = new ClientPipelineHttpHandler(this);
	}

	@Override
	public void sendHeader(HttpRequest request, HttpClientHandler clientHandler, CompletionHandler<Void, Void> completionHandler) {
		responseHandler.add(request, clientHandler, this);
        HttpUtils.sendHeader(request, channel, completionHandler);
	}

	@Override
	public void end() {
        // Does nothing.
	}

	@Override
	public DataSender createDataSender(HttpRequest request) {
	    return dataSenderFactory.create(request, channel);
	}

	@Override
	public void close() throws IOException {
		connectionFactory.dispose(socketAddress);
		super.close();
	}

	@Override
    protected void prepareChannel() {
		HttpProcessingContextFactory contextFactory = new HttpProcessingContextFactory();
		StatefulInputProcessorFactory<HttpHandler, HttpStatus, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(
				new HttpStateMachine());
		Processor<HttpHandler> processor = new DefaultProcessor<>(channel, inputProcessorFactory, contextFactory);
		processor.process(responseHandler);
	}
}
