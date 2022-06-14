package com.github.apetrelli.scafa.async.proxy.config;

import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.async.http.proxy.connection.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proxy.config.Configuration;
import com.github.apetrelli.scafa.proxy.config.ServerConfiguration;

public class ConfigurationProxyHttpConnectionFactory implements GatewayHttpConnectionFactory<ProxyHttpConnection> {

    private final Configuration<GatewayHttpConnectionFactory<ProxyHttpConnection>> configuration;

    private final GatewayHttpConnectionFactory<ProxyHttpConnection> directHttpConnectionFactory;

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
