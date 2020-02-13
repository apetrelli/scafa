package com.github.apetrelli.scafa.http.server.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.http.output.DataSender;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.server.HttpServer;
import com.github.apetrelli.scafa.proto.aio.BufferContext;
import com.github.apetrelli.scafa.proto.aio.BufferContextReader;
import com.github.apetrelli.scafa.proto.aio.impl.PathBufferContextReader;
import com.github.apetrelli.scafa.tls.util.IOUtils;

public class DefaultHttpServer implements HttpServer {

	private class ResponseWithPayloadCompletionHandler implements CompletionHandler<Void, Void> {
		private final DataSender sender;
		private final BufferContextReader payloadReader;
		private final CompletionHandler<Void, Void> completionHandler;

		private ResponseWithPayloadCompletionHandler(DataSender sender, BufferContextReader payloadReader,
				CompletionHandler<Void, Void> completionHandler) {
			this.sender = sender;
			this.payloadReader = payloadReader;
			this.completionHandler = completionHandler;
		}

		@Override
		public void completed(Void result, Void attachment) {
			BufferContext fileContext = new BufferContext();
			RequestBodySentCompletionHandler requestBodySentCompletionHandler = new RequestBodySentCompletionHandler(
					payloadReader, completionHandler, fileContext);
			ReadFileHandler readFileHandler = new ReadFileHandler(requestBodySentCompletionHandler, completionHandler,
					sender);
			requestBodySentCompletionHandler.setReadFileHandler(readFileHandler);
			payloadReader.read(fileContext, readFileHandler);
		}

		@Override
		public void failed(Throwable exc, Void attachment) {
			completionHandler.failed(exc, attachment);
		}
	}

	private static class ReadFileHandler implements CompletionHandler<Integer, BufferContext> {

		private final CompletionHandler<Void, Void> requestBodySentCompletionHandler;
		private final CompletionHandler<Void, Void> completionHandler;
		private final DataSender sender;

		private ReadFileHandler(CompletionHandler<Void, Void> requestBodySentCompletionHandler,
				CompletionHandler<Void, Void> completionHandler, DataSender sender) {
			this.requestBodySentCompletionHandler = requestBodySentCompletionHandler;
			this.completionHandler = completionHandler;
			this.sender = sender;
		}

		@Override
		public void completed(Integer result, BufferContext fileContext) {
			if (result >= 0) {
				sender.send(fileContext.getBuffer(), requestBodySentCompletionHandler);
			} else {
				sender.end(completionHandler);
			}
		}

		@Override
		public void failed(Throwable exc, BufferContext attachment) {
			completionHandler.failed(exc, null);
		}
	}

	private static class RequestBodySentCompletionHandler implements CompletionHandler<Void, Void> {
		private final BufferContextReader reader;
		private final CompletionHandler<Void, Void> completionHandler;
		private final BufferContext fileContext;

		private ReadFileHandler readFileHandler;

		private RequestBodySentCompletionHandler(BufferContextReader reader,
				CompletionHandler<Void, Void> completionHandler, BufferContext fileContext) {
			this.reader = reader;
			this.completionHandler = completionHandler;
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
			completionHandler.failed(exc, attachment);
		}
	}

	private DataSenderFactory dataSenderFactory;

	public DefaultHttpServer(DataSenderFactory dataSenderFactory) {
		this.dataSenderFactory = dataSenderFactory;
	}

	@Override
	public void response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response,
			CompletionHandler<Void, Void> completionHandler) {
		channel.sendHeader(response, completionHandler);
	}

	@Override
	public void response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, BufferContextReader payloadReader,
			CompletionHandler<Void, Void> completionHandler) {
		HttpResponse realResponse = new HttpResponse(response);
		realResponse.setHeader("Transfer-Encoding", "chunked");
		responseWithPayload(channel, realResponse, payloadReader, completionHandler);

	}

	@Override
	public void response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, BufferContextReader payloadReader,
			long size, CompletionHandler<Void, Void> completionHandler) {
		HttpResponse realResponse = new HttpResponse(response);
		realResponse.setHeader("Content-Length", Long.toString(size));
		responseWithPayload(channel, realResponse, payloadReader, completionHandler);
	}

	@Override
	public void response(HttpAsyncSocket<HttpResponse> channel, HttpResponse response, Path payload,
			CompletionHandler<Void, Void> completionHandler) {
		AsynchronousFileChannel fileChannel = null;
		try {
			fileChannel = AsynchronousFileChannel.open(payload, StandardOpenOption.READ);
			response(channel, response, new PathBufferContextReader(fileChannel), fileChannel.size(),
					completionHandler);
		} catch (IOException e1) {
			IOUtils.closeQuietly(fileChannel);
			completionHandler.failed(e1, null);
		}
	}

	private void responseWithPayload(HttpAsyncSocket<HttpResponse> channel, HttpResponse realResponse,
			BufferContextReader payloadReader, CompletionHandler<Void, Void> completionHandler) {
		DataSender sender = dataSenderFactory.create(realResponse, channel);
		channel.sendHeader(realResponse,
				new ResponseWithPayloadCompletionHandler(sender, payloadReader, completionHandler));
	}
}
