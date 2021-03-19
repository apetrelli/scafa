package com.github.apetrelli.scafa.server.config;

import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.impl.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;

public class ConfigurationProxyHttpConnectionFactory implements GatewayHttpConnectionFactory<ProxyHttpConnection> {

    private Configuration<GatewayHttpConnectionFactory<ProxyHttpConnection>> configuration;

    private GatewayHttpConnectionFactory<ProxyHttpConnection> directHttpConnectionFactory;

    public ConfigurationProxyHttpConnectionFactory(Configuration<GatewayHttpConnectionFactory<ProxyHttpConnection>> configuration,
    		SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory, DataSenderFactory dataSenderFactory,
    		ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory) {
        this.configuration = configuration;
        directHttpConnectionFactory = new DirectHttpConnectionFactory(socketFactory, dataSenderFactory, clientProcessorFactory);
    }

    @Override
    public ProxyHttpConnection create(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> factory, AsyncSocket sourceChannel,
            HostPort socketAddress) {
        return getHttpConnectionFactoryByHost(socketAddress.getHost()).create(factory, sourceChannel, socketAddress);
    }

    private GatewayHttpConnectionFactory<ProxyHttpConnection> getHttpConnectionFactoryByHost(String host) {
        ServerConfiguration<GatewayHttpConnectionFactory<ProxyHttpConnection>> config = configuration.getServerConfigurationByHost(host);
        return config != null ? config.getProxyHttpConnectionFactory() : directHttpConnectionFactory;
    }

}
