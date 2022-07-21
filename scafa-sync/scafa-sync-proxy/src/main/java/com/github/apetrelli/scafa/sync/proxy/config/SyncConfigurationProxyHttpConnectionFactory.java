package com.github.apetrelli.scafa.sync.proxy.config;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.proto.Socket;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proxy.config.Configuration;
import com.github.apetrelli.scafa.proxy.config.ServerConfiguration;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.gateway.MappedGatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.sync.http.proxy.connection.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

public class SyncConfigurationProxyHttpConnectionFactory implements GatewayHttpConnectionFactory<ProxyHttpConnection> {

    private final Configuration<GatewayHttpConnectionFactory<ProxyHttpConnection>> configuration;

    private final GatewayHttpConnectionFactory<ProxyHttpConnection> directHttpConnectionFactory;

    public SyncConfigurationProxyHttpConnectionFactory(Configuration<GatewayHttpConnectionFactory<ProxyHttpConnection>> configuration,
    		SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory, DataSenderFactory dataSenderFactory,
    		ProcessorFactory<DataHandler, Socket> clientProcessorFactory,
    		RunnableStarter runnableStarter) {
        this.configuration = configuration;
        directHttpConnectionFactory = new DirectHttpConnectionFactory(socketFactory, dataSenderFactory, clientProcessorFactory, runnableStarter);
    }

    @Override
    public ProxyHttpConnection create(MappedGatewayHttpConnectionFactory<ProxyHttpConnection> factory, Socket sourceChannel,
            HostPort socketAddress) {
        return getHttpConnectionFactoryByHost(socketAddress.getHost()).create(factory, sourceChannel, socketAddress);
    }

    private GatewayHttpConnectionFactory<ProxyHttpConnection> getHttpConnectionFactoryByHost(String host) {
        ServerConfiguration<GatewayHttpConnectionFactory<ProxyHttpConnection>> config = configuration.getServerConfigurationByHost(host);
        return config != null ? config.getProxyHttpConnectionFactory() : directHttpConnectionFactory;
    }

}
