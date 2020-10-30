package com.github.apetrelli.scafa.server.config;

import com.github.apetrelli.scafa.http.proxy.sync.MappedProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.connection.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.http.sync.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;

public class SyncConfigurationProxyHttpConnectionFactory implements ProxyHttpConnectionFactory {

    private Configuration<ProxyHttpConnectionFactory> configuration;

    private ProxyHttpConnectionFactory directHttpConnectionFactory;

    public SyncConfigurationProxyHttpConnectionFactory(Configuration<ProxyHttpConnectionFactory> configuration,
    		SocketFactory<SyncSocket> socketFactory, DataSenderFactory dataSenderFactory,
    		ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory) {
        this.configuration = configuration;
        directHttpConnectionFactory = new DirectHttpConnectionFactory(socketFactory, dataSenderFactory, clientProcessorFactory);
    }

    @Override
    public ProxyHttpConnection create(MappedProxyHttpConnectionFactory factory, SyncSocket sourceChannel,
            HostPort socketAddress) {
        return getHttpConnectionFactoryByHost(socketAddress.getHost()).create(factory, sourceChannel, socketAddress);
    }

    private ProxyHttpConnectionFactory getHttpConnectionFactoryByHost(String host) {
        ServerConfiguration<ProxyHttpConnectionFactory> config = configuration.getServerConfigurationByHost(host);
        return config != null ? config.getProxyHttpConnectionFactory() : directHttpConnectionFactory;
    }

}
