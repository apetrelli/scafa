package com.github.apetrelli.scafa.http.client.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClient;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.impl.AsyncHttpSink;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.BufferContext;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.aio.impl.DirectClientAsyncSocketFactory;
import com.github.apetrelli.scafa.proto.aio.impl.PathBufferContextReader;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class DefaultHttpClient implements HttpClient {

	private MappedHttpConnectionFactory connectionFactory;

	private static ProcessorFactory<HttpHandler, AsyncSocket> buildDefaultProcessorFactory() {
		HttpProcessingContextFactory contextFactory = new HttpProcessingContextFactory();
		StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(
				new HttpStateMachine<>(new AsyncHttpSink()));
		return new DefaultProcessorFactory<>(inputProcessorFactory, contextFactory);
	}
	
	public DefaultHttpClient(MappedHttpConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public DefaultHttpClient() {
		this(new DefaultMappedHttpConnectionFactory(new DefaultDataSenderFactory(),
				new DirectClientAsyncSocketFactory(), buildDefaultProcessorFactory()));
	}

	@Override
	public void request(HttpRequest request, HttpClientHandler handler) {
		ByteBuffer buffer = ByteBuffer.allocate(16384);
		connectionFactory.create(request).thenCompose(result -> {
			result.prepare(request, handler);
			return result.sendHeader(request, buffer).thenAccept(x -> {
                handler.onRequestHeaderSent(request);
                handler.onRequestEnd(request);
			});
		}).handle((x, e) -> {
			if (e != null) {
				handler.onRequestError(request, e);
			}
			return CompletionHandlerFuture.completeEmpty();
		});
	}

	@Override
	public void request(HttpRequest request, BufferContextReader payloadReader, HttpClientHandler handler) {
		HttpRequest realRequest = new HttpRequest(request);
		realRequest.setHeader(TRANSFER_ENCODING, CHUNKED);
		requestWithPayload(realRequest, payloadReader, handler);
	}

	@Override
	public void request(HttpRequest request, BufferContextReader payloadReader, long size, HttpClientHandler handler) {
		HttpRequest realRequest = new HttpRequest(request);
		realRequest.setHeader(CONTENT_LENGTH, new AsciiString(Long.toString(size)));
		requestWithPayload(realRequest, payloadReader, handler);
	}

	@Override
	public void request(HttpRequest request, Path payload, HttpClientHandler handler) {
		AsynchronousFileChannel fileChannel = null;
		try {
			fileChannel = AsynchronousFileChannel.open(payload, StandardOpenOption.READ); // NOSONAR
			PathBufferContextReader payloadReader = new PathBufferContextReader(fileChannel); // NOSONAR
			request(request, payloadReader, fileChannel.size(), handler);
		} catch (IOException e1) {
			IOUtils.closeQuietly(fileChannel);
			handler.onRequestError(request, e1);
		}
	}

	private void requestWithPayload(HttpRequest realRequest, BufferContextReader payloadReader,
			HttpClientHandler handler) {
		ByteBuffer buffer = ByteBuffer.allocate(16384);
		connectionFactory.create(realRequest).thenCompose(connection -> {
        	connection.prepare(realRequest, handler);
            return connection.sendHeader(realRequest, buffer).thenCompose(x -> {
    			handler.onRequestHeaderSent(realRequest);
    			BufferContext fileContext = new BufferContext();
    			fileContext.setBuffer(buffer);
    			return transferPayload(realRequest, payloadReader, handler, connection, fileContext);
            });
		}).handle((x, e) -> {
			if (e != null) {
				handler.onRequestError(realRequest, e);
			}
			return CompletionHandlerFuture.completeEmpty();
		});
	}

	private CompletableFuture<Void> transferPayload(HttpRequest realRequest, BufferContextReader payloadReader,
			HttpClientHandler handler, HttpClientConnection connection, BufferContext fileContext) {
		return payloadReader.read(fileContext).thenCompose(y -> {
			if (y >= 0) {
				return connection.sendData(fileContext.getBuffer()).thenCompose(x -> {
					fileContext.getBuffer().clear();
					return transferPayload(realRequest, payloadReader, handler, connection, fileContext);
				});
			} else {
				return connection.endData().thenAccept(z -> handler.onRequestEnd(realRequest));
			}
		});
	}
}
