package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClient;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.BufferContext;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;
import com.github.apetrelli.scafa.proto.aio.CompletionHandlerFuture;
import com.github.apetrelli.scafa.proto.aio.impl.PathBufferContextReader;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class DefaultHttpClient implements HttpClient {

	private MappedHttpConnectionFactory connectionFactory;

	private DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();

	public DefaultHttpClient() {
		connectionFactory = new DefaultMappedHttpConnectionFactory(dataSenderFactory);
	}

	@Override
	public void request(HttpRequest request, HttpClientHandler handler) {
		connectionFactory.create(request).thenCompose(result -> {
			result.prepare(request, handler);
			return result.sendHeader(request).thenAccept(x -> {
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
		realRequest.setHeader("Transfer-Encoding", "chunked");
		requestWithPayload(realRequest, payloadReader, handler);
	}

	@Override
	public void request(HttpRequest request, BufferContextReader payloadReader, long size, HttpClientHandler handler) {
		HttpRequest realRequest = new HttpRequest(request);
		realRequest.setHeader("Content-Length", Long.toString(size));
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
		connectionFactory.create(realRequest).thenCompose(connection -> {
        	connection.prepare(realRequest, handler);
            return connection.sendHeader(realRequest).thenCompose(x -> {
    			handler.onRequestHeaderSent(realRequest);
    			BufferContext fileContext = new BufferContext();
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
			if (y.getResult() >= 0) {
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
