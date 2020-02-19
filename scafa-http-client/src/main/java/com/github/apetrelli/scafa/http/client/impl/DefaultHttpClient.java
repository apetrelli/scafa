package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClient;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.BufferContext;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;
import com.github.apetrelli.scafa.proto.aio.impl.PathBufferContextReader;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class DefaultHttpClient implements HttpClient {

	private class RequestWithPayloadCompletionHandler implements CompletionHandler<Void, Void> {
		private final HttpRequest realRequest;
		private final BufferContextReader payloadReader;
		private final HttpClientHandler handler;
		private final HttpClientConnection connection;

		private RequestWithPayloadCompletionHandler(HttpRequest realRequest,
				BufferContextReader payloadReader, HttpClientHandler handler, HttpClientConnection connection) {
			this.realRequest = realRequest;
			this.payloadReader = payloadReader;
			this.handler = handler;
			this.connection = connection;
		}

		@Override
		public void completed(Void result, Void attachment) {
			handler.onRequestHeaderSent(realRequest);
			BufferContext fileContext = new BufferContext();
			RequestBodySentCompletionHandler requestBodySentCompletionHandler = new RequestBodySentCompletionHandler(
					realRequest, payloadReader, handler, fileContext);
            ReadFileHandler readFileHandler = new ReadFileHandler(realRequest, requestBodySentCompletionHandler,
                    handler, connection);
			requestBodySentCompletionHandler.setReadFileHandler(readFileHandler);
			payloadReader.read(fileContext, readFileHandler);
		}

		@Override
		public void failed(Throwable exc, Void attachment) {
			handler.onRequestError(realRequest, exc);
		}
	}

	private static class ReadFileHandler implements CompletionHandler<Integer, BufferContext> {
		private class AfterPayloadSentCompletionHandler implements CompletionHandler<Void, Void> {
			@Override
			public void completed(Void result, Void attachment) {
				handler.onRequestEnd(request);
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				handler.onRequestError(request, exc);
			}
		}

		private final HttpRequest request;
		private final CompletionHandler<Void, Void> requestBodySentCompletionHandler;
		private final HttpClientHandler handler;
		private final HttpClientConnection connection;

		private ReadFileHandler(HttpRequest request, CompletionHandler<Void, Void> requestBodySentCompletionHandler,
				HttpClientHandler handler, HttpClientConnection connection) {
			this.request = request;
			this.requestBodySentCompletionHandler = requestBodySentCompletionHandler;
			this.handler = handler;
			this.connection = connection;
		}

		@Override
		public void completed(Integer result, BufferContext fileContext) {
			if (result >= 0) {
				connection.sendData(fileContext.getBuffer(), requestBodySentCompletionHandler);
			} else {
				connection.endData(new AfterPayloadSentCompletionHandler());
			}
		}

		@Override
		public void failed(Throwable exc, BufferContext attachment) {
			handler.onRequestError(request, exc);
		}
	}

	private static class RequestBodySentCompletionHandler implements CompletionHandler<Void, Void> {
		private final HttpRequest realRequest;
		private final BufferContextReader reader;
		private final HttpClientHandler handler;
		private final BufferContext fileContext;

		private ReadFileHandler readFileHandler;

		private RequestBodySentCompletionHandler(HttpRequest realRequest, BufferContextReader reader,
				HttpClientHandler handler, BufferContext fileContext) {
			this.realRequest = realRequest;
			this.reader = reader;
			this.handler = handler;
			this.fileContext = fileContext;
		}

		public void setReadFileHandler(ReadFileHandler readFileHandler) {
			this.readFileHandler = readFileHandler;
		}

		@Override
		public void completed(Void result, Void attachment) {
			fileContext.getBuffer().clear();
			reader.read(fileContext, readFileHandler);
		}

		@Override
		public void failed(Throwable exc, Void attachment) {
			IOUtils.closeQuietly(reader);
			handler.onRequestError(realRequest, exc);
		}
	}

	private MappedHttpConnectionFactory connectionFactory;

	private DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();

	public DefaultHttpClient() {
		connectionFactory = new DefaultMappedHttpConnectionFactory(dataSenderFactory);
	}

	@Override
	public void request(HttpRequest request, HttpClientHandler handler) {
		connectionFactory.create(request, new CompletionHandler<HttpClientConnection, Void>() {

            @Override
            public void completed(HttpClientConnection result, Void attachment) {
            	result.prepare(request, handler);
                result.sendHeader(request, new CompletionHandler<Void, Void>() {

                    @Override
                    public void completed(Void result, Void attachment) {
                        handler.onRequestHeaderSent(request);
                        handler.onRequestEnd(request);
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        handler.onRequestError(request, exc);
                    }
                });
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                handler.onRequestError(request, exc);
            }
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
			fileChannel = AsynchronousFileChannel.open(payload, StandardOpenOption.READ);
			request(request, new PathBufferContextReader(fileChannel), fileChannel.size(), handler);
		} catch (IOException e1) {
			IOUtils.closeQuietly(fileChannel);
			handler.onRequestError(request, e1);
		}
	}

	private void requestWithPayload(HttpRequest realRequest, BufferContextReader payloadReader,
			HttpClientHandler handler) {
		connectionFactory.create(realRequest, new CompletionHandler<HttpClientConnection, Void>() {

            @Override
            public void completed(HttpClientConnection connection, Void attachment) {
            	connection.prepare(realRequest, handler);
                connection.sendHeader(realRequest, new RequestWithPayloadCompletionHandler(realRequest, payloadReader, handler, connection));
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                handler.onRequestError(realRequest, exc);
            }
		});
	}
}
