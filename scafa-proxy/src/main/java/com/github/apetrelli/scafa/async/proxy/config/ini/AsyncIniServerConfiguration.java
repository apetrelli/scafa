package com.github.apetrelli.scafa.async.proxy.config.ini;

import java.util.concurrent.CompletableFuture;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.async.proto.processor.DataHandler;
import com.github.apetrelli.scafa.async.proto.socket.AsyncSocket;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.async.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.async.http.HttpHandler;
import com.github.apetrelli.scafa.async.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.async.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.async.http.proxy.connection.AnonymousProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.proxy.connection.BasicAuthProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.proxy.connection.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.async.http.proxy.ntlm.NtlmProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proxy.config.ini.AbstractIniServerConfiguration;

public class AsyncIniServerConfiguration extends AbstractIniServerConfiguration<GatewayHttpConnectionFactory<ProxyHttpConnection>> {
    private static final String FORCE_IPV4 = "forceIPV4";

	private static final String INTERFACE = "interface";

	private static GatewayHttpConnectionFactory<ProxyHttpConnection> buildConnectionFactory(Section section, SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
			HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine) {
		GatewayHttpConnectionFactory<ProxyHttpConnection> connectionFactory;
		String type = section.get("type");
        switch (type) {
        case "ntlm":
            connectionFactory = new NtlmProxyHttpConnectionFactory(socketFactory, dataSenderFactory,
                    clientProcessorFactory, createProxySocketAddress(section), section.get(INTERFACE),
                    section.get(FORCE_IPV4, boolean.class, false), section.get("domain"), section.get("username"),
                    section.get("password"), createManipulator(section), stateMachine);
            break;
        case "anon":
			connectionFactory = new AnonymousProxyHttpConnectionFactory(socketFactory, clientProcessorFactory,
					createProxySocketAddress(section), section.get(INTERFACE),
					section.get(FORCE_IPV4, boolean.class, false), createManipulator(section));
            break;
        case "basic":
			connectionFactory = new BasicAuthProxyHttpConnectionFactory(socketFactory, clientProcessorFactory,
					createProxySocketAddress(section), section.get(INTERFACE),
					section.get(FORCE_IPV4, boolean.class, false), section.get("username"), section.get("password"),
					createManipulator(section));
            break;
        default:
			connectionFactory = new DirectHttpConnectionFactory(socketFactory, dataSenderFactory,
					clientProcessorFactory);
            break;
        }
		return connectionFactory;
	}

    public AsyncIniServerConfiguration(Section section, SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
            DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
            HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine) {
    	super(section, buildConnectionFactory(section, socketFactory, dataSenderFactory, clientProcessorFactory, stateMachine));
    }
}
