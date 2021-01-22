package com.github.apetrelli.scafa.server.config.ini.async;

import java.util.concurrent.CompletableFuture;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.impl.AnonymousProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.BasicAuthProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ntlm.NtlmProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.async.AsyncSocket;
import com.github.apetrelli.scafa.proto.async.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.server.config.ini.AbstractIniServerConfiguration;

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
