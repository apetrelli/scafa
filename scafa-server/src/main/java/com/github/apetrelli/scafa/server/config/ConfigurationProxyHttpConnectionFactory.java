package com.github.apetrelli.scafa.server.config;

import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.AsynchronousSocketChannelFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class ConfigurationProxyHttpConnectionFactory implements ProxyHttpConnectionFactory {

    private Configuration configuration;
    
    private ProxyHttpConnectionFactory directHttpConnectionFactory;

    public ConfigurationProxyHttpConnectionFactory(Configuration configuration, AsynchronousSocketChannelFactory channelFactory) {
        this.configuration = configuration;
        directHttpConnectionFactory = new DirectHttpConnectionFactory(channelFactory);
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
