package com.github.apetrelli.scafa.sync.http.server.impl;

import static com.github.apetrelli.scafa.http.HttpHeaders.CHUNKED;
import static com.github.apetrelli.scafa.http.HttpHeaders.CONTENT_LENGTH;
import static com.github.apetrelli.scafa.http.HttpHeaders.TRANSFER_ENCODING;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.apetrelli.scafa.http.HttpResponse;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.output.DataSender;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.server.HttpServer;
import com.github.apetrelli.scafa.proto.IORuntimeException;
import com.github.apetrelli.scafa.proto.util.AsciiString;


public class DefaultHttpServer implements HttpServer {

	private DataSenderFactory dataSenderFactory;

	public DefaultHttpServer(DataSenderFactory dataSenderFactory) {
		this.dataSenderFactory = dataSenderFactory;
	}
	
	@Override
	public void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response, ByteBuffer writeBuffer) {
		channel.sendHeader(response, writeBuffer);
	}
	
	@Override
	public void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response,
			InputStream payload, ByteBuffer writeBuffer) {
		HttpResponse realResponse = new HttpResponse(response);
		realResponse.setHeader(TRANSFER_ENCODING, CHUNKED);
		responseWithPayload(channel, realResponse, payload, writeBuffer);
	}

	@Override
	public void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response,
			InputStream payload, long size, ByteBuffer writeBuffer) {
		HttpResponse realResponse = new HttpResponse(response);
		realResponse.setHeader(CONTENT_LENGTH, new AsciiString(Long.toString(size)));
		responseWithPayload(channel, realResponse, payload, writeBuffer);
	}
	
	@Override
	public void response(HttpSyncSocket<HttpResponse> channel, HttpResponse response,
			Path payload, ByteBuffer writeBuffer) {
		try (InputStream payloadIs = Files.newInputStream(payload)) {
			response(channel, response, payloadIs, payload.toFile().length(), writeBuffer);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	private void responseWithPayload(HttpSyncSocket<HttpResponse> channel, HttpResponse realResponse,
			InputStream payload, ByteBuffer writeBuffer) {
		DataSender sender = dataSenderFactory.create(realResponse, channel);
		channel.sendHeader(realResponse, writeBuffer);
		transferPayload(payload, sender, writeBuffer);
	}

	private void transferPayload(InputStream payload, DataSender sender,
			ByteBuffer buffer) {
		int count;
		try {
			while ((count = payload.read(buffer.array(), buffer.position() + buffer.arrayOffset(), buffer.remaining())) >= 0) {
				buffer.position(buffer.position() + count);
				buffer.flip();
				sender.send(buffer);
				buffer.clear();
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
}
