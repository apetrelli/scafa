package com.github.apetrelli.scafa.server.config;

import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.aio.ProcessorFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;

public class ConfigurationProxyHttpConnectionFactory implements ProxyHttpConnectionFactory {

    private Configuration configuration;

    private ProxyHttpConnectionFactory directHttpConnectionFactory;

    public ConfigurationProxyHttpConnectionFactory(Configuration configuration,
    		SocketFactory<AsyncSocket> socketFactory, DataSenderFactory dataSenderFactory,
    		ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory) {
        this.configuration = configuration;
        directHttpConnectionFactory = new DirectHttpConnectionFactory(socketFactory, dataSenderFactory, clientProcessorFactory);
    }

    @Override
    public ProxyHttpConnection create(MappedProxyHttpConnectionFactory factory, AsyncSocket sourceChannel,
            HostPort socketAddress) {
        return getHttpConnectionFactoryByHost(socketAddress.getHost()).create(factory, sourceChannel, socketAddress);
    }

    private ProxyHttpConnectionFactory getHttpConnectionFactoryByHost(String host) {
        ServerConfiguration config = configuration.getServerConfigurationByHost(host);
        return config != null ? config.getProxyHttpConnectionFactory() : directHttpConnectionFactory;
    }

}
