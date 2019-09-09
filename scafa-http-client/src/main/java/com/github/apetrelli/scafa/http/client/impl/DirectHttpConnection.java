package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.impl.AbstractHttpConnection;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.util.HttpUtils;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class DirectHttpConnection extends AbstractHttpConnection implements HttpClientConnection {

    private ClientPipelineHttpHandler responseHandler;

    private MappedHttpConnectionFactory connectionFactory;

    private AsynchronousSocketChannel channel;

	public DirectHttpConnection(HostPort socketAddress, MappedHttpConnectionFactory connectionFactory) {
		super(socketAddress);
		this.connectionFactory = connectionFactory;
		responseHandler = new ClientPipelineHttpHandler(this);
	}

	@Override
	public void sendHeader(HttpRequest request, HttpClientHandler clientHandler, CompletionHandler<Void, Void> completionHandler) {
		responseHandler.add(request, clientHandler, this);
        HttpUtils.sendHeader(request, channel, completionHandler);
	}

    @Override
    public void sendAsChunk(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        HttpUtils.sendAsChunk(buffer, channel, completionHandler);
    }

    @Override
    public void endChunkedTransfer(CompletionHandler<Void, Void> completionHandler) {
        HttpUtils.sendEndOfChunkedTransfer(channel, completionHandler);
    }

	@Override
	public void end() {
        // Does nothing.
	}

	@Override
	public void close() throws IOException {
		connectionFactory.dispose(socketAddress);
		super.close();
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
	}

	@Override
    protected void establishConnection(CompletionHandler<Void, Void> handler) {
        channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort()), null, handler);
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
