package com.github.apetrelli.scafa.http.gateway.direct;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.impl.DirectHttpAsyncSocket;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class DirectGatewayHttpConnectionFactory implements GatewayHttpConnectionFactory<HttpAsyncSocket<HttpRequest>> {

	private SocketFactory<AsyncSocket> socketFactory;
	
	private DataSenderFactory dataSenderFactory;
	
	private ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory;
	
	private HostPort destinationSocketAddress;

	public DirectGatewayHttpConnectionFactory(SocketFactory<AsyncSocket> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
			HostPort destinationSocketAddress) {
		this.socketFactory = socketFactory;
		this.dataSenderFactory = dataSenderFactory;
		this.clientProcessorFactory = clientProcessorFactory;
		this.destinationSocketAddress = destinationSocketAddress;
	}

	@Override
	public HttpAsyncSocket<HttpRequest> create(MappedGatewayHttpConnectionFactory<HttpAsyncSocket<HttpRequest>> factory,
			AsyncSocket sourceChannel, HostPort socketAddress) {
		AsyncSocket socket = socketFactory.create(destinationSocketAddress, null, false);
		HttpAsyncSocket<HttpRequest> httpSocket = new DirectHttpAsyncSocket<>(socket, dataSenderFactory);
		return new DirectGatewayHttpConnection(factory, clientProcessorFactory, sourceChannel, httpSocket, destinationSocketAddress);
	}

}
