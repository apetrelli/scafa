package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HostPort;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.util.HttpUtils;
import com.github.apetrelli.scafa.proto.aio.DelegateFailureCompletionHandler;
import com.github.apetrelli.scafa.proto.processor.Processor;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessor;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class DirectHttpConnection implements HttpClientConnection {

	private static final Logger LOG = Logger.getLogger(DirectHttpConnection.class.getName());

	private HostPort socketAddress;

    private ClientPipelineHttpHandler responseHandler;

    private MappedHttpConnectionFactory connectionFactory;

    private AsynchronousSocketChannel channel;

	public DirectHttpConnection(HostPort socketAddress, MappedHttpConnectionFactory connectionFactory) {
		this.socketAddress = socketAddress;
		this.connectionFactory = connectionFactory;
		responseHandler = new ClientPipelineHttpHandler(this);
	}

	@Override
	public void ensureConnected(CompletionHandler<Void, Void> handler) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Connected thread {0} to address {1}",
                    new Object[] { Thread.currentThread().getName(), socketAddress.toString() });
        }
        try {
            channel = AsynchronousSocketChannel.open();
        } catch (IOException e1) {
            handler.failed(e1, null);
        }
        establishConnection(new DelegateFailureCompletionHandler<Void, Void>(handler) {

            @Override
            public void completed(Void result, Void attachment) {
                if (LOG.isLoggable(Level.INFO)) {
                    try {
                        LOG.log(Level.INFO, "Connected thread {0} to port {1}",
                                new Object[] { Thread.currentThread().getName(), channel.getLocalAddress().toString() });
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, "Cannot obtain local address", e);
                    }
                }

                prepareChannel();
                handler.completed(result, attachment);
            }
        });
	}

	@Override
	public void sendHeader(HttpRequest request, HttpClientHandler clientHandler, CompletionHandler<Void, Void> completionHandler) {
		responseHandler.add(request, clientHandler, this);
        HttpUtils.sendHeader(request, channel, completionHandler);
	}

	@Override
	public void send(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
        HttpUtils.flushBuffer(buffer, channel, completionHandler);
	}

	@Override
	public void sendAsChunk(ByteBuffer buffer, CompletionHandler<Void, Void> completionHandler) {
		HttpUtils.sendChunkSize(buffer.remaining(), channel, new CompletionHandler<Void, Void>() {

			@Override
			public void completed(Void result, Void attachment) {
				HttpUtils.flushBuffer(buffer, channel, new CompletionHandler<Void, Void>() {

					@Override
					public void completed(Void result, Void attachment) {
						HttpUtils.sendNewline(channel, completionHandler);
					}

					@Override
					public void failed(Throwable exc, Void attachment) {
						completionHandler.failed(exc, attachment);
					}
				});
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				completionHandler.failed(exc, attachment);
			}
		});
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
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
	}

    private void establishConnection(CompletionHandler<Void, Void> handler) {
        channel.connect(new InetSocketAddress(socketAddress.getHost(), socketAddress.getPort()), null, handler);
    }

    private void prepareChannel() {
		HttpProcessingContextFactory contextFactory = new HttpProcessingContextFactory();
		StatefulInputProcessorFactory<HttpHandler, HttpStatus, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(
				new HttpStateMachine());
		Processor<HttpHandler> processor = new DefaultProcessor<>(channel, inputProcessorFactory, contextFactory);
		processor.process(responseHandler);
	}
}
