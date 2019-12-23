package com.github.apetrelli.scafa.impl;

import java.nio.channels.AsynchronousSocketChannel;

import com.github.apetrelli.scafa.config.ServerConfiguration;
import com.github.apetrelli.scafa.config.ini.IniConfiguration;
import com.github.apetrelli.scafa.http.proxy.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;

public class ConfigurationProxyHttpConnectionFactory implements ProxyHttpConnectionFactory {

    private IniConfiguration configuration;

    private ProxyHttpConnectionFactory directHttpConnectionFactory = new DirectHttpConnectionFactory();

    public ConfigurationProxyHttpConnectionFactory(IniConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ProxyHttpConnection create(MappedProxyHttpConnectionFactory factory, AsynchronousSocketChannel sourceChannel,
            HostPort socketAddress) {
        return getHttpConnectionFactoryByHost(socketAddress.getHost()).create(factory, sourceChannel, socketAddress);
    }

    private ProxyHttpConnectionFactory getHttpConnectionFactoryByHost(String host) {
        ServerConfiguration config = configuration.getServerConfigurationByHost(host);
        return config != null ? config.getProxyHttpConnectionFactory() : directHttpConnectionFactory;
    }

}
