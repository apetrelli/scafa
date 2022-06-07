package com.github.apetrelli.scafa.server.config.ini.sync;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnection;
import com.github.apetrelli.scafa.http.proxy.sync.connection.AnonymousProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.connection.BasicAuthProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.connection.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.ntlm.NtlmProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.server.config.ini.AbstractIniServerConfiguration;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;


public class SyncIniServerConfiguration extends AbstractIniServerConfiguration<GatewayHttpConnectionFactory<ProxyHttpConnection>> {
    private static final String FORCE_IPV4 = "forceIPV4";

	private static final String INTERFACE = "interface";

	private static GatewayHttpConnectionFactory<ProxyHttpConnection> buildConnectionFactory(Section section,
			SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory, DataSenderFactory dataSenderFactory,
			ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory, RunnableStarter runnableStarter,
			HttpStateMachine<HttpHandler, Void> stateMachine) {
		GatewayHttpConnectionFactory<ProxyHttpConnection> connectionFactory;
		String type = section.get("type");
        switch (type) {
        case "ntlm":
			connectionFactory = new NtlmProxyHttpConnectionFactory(socketFactory, dataSenderFactory,
					clientProcessorFactory, runnableStarter, createProxySocketAddress(section), section.get(INTERFACE),
					section.get(FORCE_IPV4, boolean.class, false), section.get("domain"), section.get("username"),
					section.get("password"), createManipulator(section), stateMachine);
            break;
        case "anon":
			connectionFactory = new AnonymousProxyHttpConnectionFactory(socketFactory,
					clientProcessorFactory, runnableStarter, createProxySocketAddress(section), section.get(INTERFACE),
					section.get(FORCE_IPV4, boolean.class, false), createManipulator(section));
            break;
        case "basic":
			connectionFactory = new BasicAuthProxyHttpConnectionFactory(socketFactory,
					clientProcessorFactory, runnableStarter, createProxySocketAddress(section), section.get(INTERFACE),
					section.get(FORCE_IPV4, boolean.class, false), section.get("username"), section.get("password"),
					createManipulator(section));
            break;
        default:
			connectionFactory = new DirectHttpConnectionFactory(socketFactory, dataSenderFactory,
					clientProcessorFactory, runnableStarter);
            break;
        }
		return connectionFactory;
	}

	public SyncIniServerConfiguration(Section section, SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			RunnableStarter runnableStarter, HttpStateMachine<HttpHandler, Void> stateMachine) {
		super(section, buildConnectionFactory(section, socketFactory, dataSenderFactory, clientProcessorFactory,
				runnableStarter, stateMachine));
    }
}
