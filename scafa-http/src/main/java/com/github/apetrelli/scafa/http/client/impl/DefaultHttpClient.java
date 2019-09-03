package com.github.apetrelli.scafa.http.client.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.client.HttpClient;
import com.github.apetrelli.scafa.http.client.HttpClientConnection;
import com.github.apetrelli.scafa.http.client.HttpClientHandler;
import com.github.apetrelli.scafa.proto.aio.ResultHandler;

public class DefaultHttpClient implements HttpClient {

	private static class ReadFileHandler implements CompletionHandler<Integer, FileContext> {
		private final HttpRequest request;
		private final CompletionHandler<Void, Void> requestBodySentCompletionHandler;
		private final HttpClientHandler handler;
		private final AsynchronousFileChannel fileChannel;
		private final HttpClientConnection connection;

		private ReadFileHandler(HttpRequest request, CompletionHandler<Void, Void> requestBodySentCompletionHandler,
				HttpClientHandler handler, AsynchronousFileChannel fileChannel, HttpClientConnection connection) {
			this.request = request;
			this.requestBodySentCompletionHandler = requestBodySentCompletionHandler;
			this.handler = handler;
			this.fileChannel = fileChannel;
			this.connection = connection;
		}

		@Override
		public void completed(Integer result, FileContext currentFileContext) {
			if (result >= 0) {
				currentFileContext.moveForwardBy(result);
				currentFileContext.getBuffer().flip();
				connection.send(currentFileContext.getBuffer(), requestBodySentCompletionHandler);
			} else {
				closeFileChannel(fileChannel);
				handler.onRequestEnd(request);
			}
		}

		@Override
		public void failed(Throwable exc, FileContext attachment) {
			handler.onRequestError(request, exc);
		}
	}

	private static class RequestBodySentCompletionHandler implements CompletionHandler<Void, Void> {
		private final HttpRequest realRequest;
		private final AsynchronousFileChannel fileChannel;
		private final HttpClientHandler handler;
		private final FileContext fileContext;

		private ReadFileHandler readFileHandler;

		private RequestBodySentCompletionHandler(HttpRequest realRequest, AsynchronousFileChannel fileChannel,
				HttpClientHandler handler, FileContext fileContext) {
			this.realRequest = realRequest;
			this.fileChannel = fileChannel;
			this.handler = handler;
			this.fileContext = fileContext;
		}

		public void setReadFileHandler(ReadFileHandler readFileHandler) {
			this.readFileHandler = readFileHandler;
		}

		@Override
		public void completed(Void result, Void attachment) {
			fileContext.getBuffer().clear();
			fileChannel.read(fileContext.getBuffer(), fileContext.getPosition(), fileContext, readFileHandler);
		}

		@Override
		public void failed(Throwable exc, Void attachment) {
			closeFileChannel(fileChannel);
			handler.onRequestError(realRequest, exc);
		}
	}

	private static final Logger LOG = Logger.getLogger(DefaultHttpClient.class.getName());

	private MappedHttpConnectionFactory connectionFactory;

	public DefaultHttpClient() {
		connectionFactory = new DefaultMappedHttpConnectionFactory();
	}

	@Override
	public void request(HttpRequest request, HttpClientHandler handler) {
		connectionFactory.create(request, new ResultHandler<HttpClientConnection>() {

			@Override
			public void handle(HttpClientConnection result) {
				result.sendHeader(request, handler, new CompletionHandler<Void, Void>() {

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
		});
	}

	@Override
	public void request(HttpRequest request, Path payload, HttpClientHandler handler) {
		try {
			AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(payload, StandardOpenOption.READ);
			HttpRequest realRequest = new HttpRequest(request);
			realRequest.setHeader("Content-Length", Long.toString(fileChannel.size()));
			connectionFactory.create(realRequest, new ResultHandler<HttpClientConnection>() {

				@Override
				public void handle(HttpClientConnection connection) {
					connection.sendHeader(realRequest, handler, new CompletionHandler<Void, Void>() {

						@Override
						public void completed(Void result, Void attachment) {
							handler.onRequestHeaderSent(realRequest);
							FileContext fileContext = new FileContext();
							RequestBodySentCompletionHandler requestBodySentCompletionHandler = new RequestBodySentCompletionHandler(realRequest, fileChannel, handler, fileContext);
							ReadFileHandler readFileHandler = new ReadFileHandler(realRequest, requestBodySentCompletionHandler, handler, fileChannel,
									connection);
							requestBodySentCompletionHandler.setReadFileHandler(readFileHandler);
							fileChannel.read(fileContext.getBuffer(), 0, fileContext, readFileHandler);
						}

						@Override
						public void failed(Throwable exc, Void attachment) {
							handler.onRequestError(realRequest, exc);
						}
					});
				}
			});
		} catch (IOException e1) {
			handler.onRequestError(request, e1);
		}
	}

	private static void closeFileChannel(AsynchronousFileChannel fileChannel) {
		if (fileChannel.isOpen()) {
			try {
				fileChannel.close();
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Cannot close file", e);
			}
		}
	}
}
