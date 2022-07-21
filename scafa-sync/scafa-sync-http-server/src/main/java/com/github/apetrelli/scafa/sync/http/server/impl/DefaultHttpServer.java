package com.github.apetrelli.scafa.sync.http.server.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.io.FlowBuffer;
import com.github.apetrelli.scafa.proto.util.AsciiString;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSender;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.server.HttpServer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultHttpServer implements HttpServer {

	private final DataSenderFactory dataSenderFactory;
	
	@Override
	public void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response) {
		channel.sendHeader(response);
	}
	
	@Override
	public void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response,
			InputStream payload, FlowBuffer writeBuffer) {
		HttpResponse realResponse = new HttpResponse(response);
		realResponse.setHeader(TRANSFER_ENCODING, CHUNKED);
		responseWithPayload(channel, realResponse, payload, writeBuffer);
	}

	@Override
	public void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response,
			InputStream payload, long size, FlowBuffer writeBuffer) {
		HttpResponse realResponse = new HttpResponse(response);
		realResponse.setHeader(CONTENT_LENGTH, new AsciiString(Long.toString(size)));
		responseWithPayload(channel, realResponse, payload, writeBuffer);
	}
	
	@Override
	public void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response,
			Path payload, FlowBuffer writeBuffer) {
		try (InputStream payloadIs = Files.newInputStream(payload)) {
			response(channel, response, payloadIs, payload.toFile().length(), writeBuffer);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	private void responseWithPayload(HttpSyncSocket<HttpResponse> channel, HttpResponse realResponse,
			InputStream payload, FlowBuffer writeBuffer) {
		DataSender sender = dataSenderFactory.create(realResponse, channel);
		channel.sendHeader(realResponse);
		transferPayload(payload, sender, writeBuffer);
	}

	private void transferPayload(InputStream payload, DataSender sender,
			FlowBuffer buffer) {
		try {
			int count;
			while ((count = payload.read(buffer.array(), 0, buffer.maxLength())) >= 0) {
				buffer.length(count);
				sender.send(buffer);
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
}
