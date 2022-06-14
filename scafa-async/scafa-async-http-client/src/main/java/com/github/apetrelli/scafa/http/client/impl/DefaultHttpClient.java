package com.github.apetrelli.scafa.http.client.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.async.file.BufferContext;
import com.github.apetrelli.scafa.async.file.BufferContextReader;
import com.github.apetrelli.scafa.async.file.PathBufferContextReader;
import com.github.apetrelli.scafa.async.file.PathBufferContextReaderFactory;
import com.github.apetrelli.scafa.async.file.PathIOException;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.impl.AsyncHttpSink;
import com.github.apetrelli.scafa.async.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.async.proto.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.async.proto.util.CompletionHandlerFuture;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClient;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.util.AsciiString;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultHttpClient implements HttpClient {

	private final MappedHttpConnectionFactory connectionFactory;
	
	private final PathBufferContextReaderFactory pathBufferContextReaderFactory;

	public static ProcessorFactory<HttpHandler, AsyncSocket> buildDefaultProcessorFactory() {
		HttpProcessingContextFactory contextFactory = new HttpProcessingContextFactory();
		StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(
				new HttpStateMachine<>(new AsyncHttpSink()));
		return new DefaultProcessorFactory<>(inputProcessorFactory, contextFactory);
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
		try {
			PathBufferContextReader payloadReader = pathBufferContextReaderFactory.create(payload);
			request(request, payloadReader, payloadReader.size(), handler);
		} catch (PathIOException e) {
			handler.onRequestError(request, e);
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
