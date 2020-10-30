package com.github.apetrelli.scafa.server.config.ini.sync;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.connection.AnonymousProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.connection.BasicAuthProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.connection.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.sync.ntlm.NtlmProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;
import com.github.apetrelli.scafa.server.config.ini.AbstractIniServerConfiguration;


public class SyncIniServerConfiguration extends AbstractIniServerConfiguration<ProxyHttpConnectionFactory> {
    private static final String FORCE_IPV4 = "forceIPV4";

	private static final String INTERFACE = "interface";

	private static ProxyHttpConnectionFactory buildConnectionFactory(Section section, SocketFactory<SyncSocket> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			HttpStateMachine<HttpHandler, Void> stateMachine) {
		ProxyHttpConnectionFactory connectionFactory;
		String type = section.get("type");
        switch (type) {
        case "ntlm":
            connectionFactory = new NtlmProxyHttpConnectionFactory(socketFactory, dataSenderFactory,
                    clientProcessorFactory, createProxySocketAddress(section), section.get(INTERFACE),
                    section.get(FORCE_IPV4, boolean.class, false), section.get("domain"), section.get("username"),
                    section.get("password"), createManipulator(section), stateMachine);
            break;
        case "anon":
            connectionFactory = new AnonymousProxyHttpConnectionFactory(socketFactory, dataSenderFactory,
                    clientProcessorFactory, createProxySocketAddress(section), section.get(INTERFACE),
                    section.get(FORCE_IPV4, boolean.class, false), createManipulator(section));
            break;
        case "basic":
            connectionFactory = new BasicAuthProxyHttpConnectionFactory(socketFactory, dataSenderFactory,
                    clientProcessorFactory, createProxySocketAddress(section), section.get(INTERFACE),
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

    public SyncIniServerConfiguration(Section section, SocketFactory<SyncSocket> socketFactory,
            DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
            HttpStateMachine<HttpHandler, Void> stateMachine) {
    	super(section, buildConnectionFactory(section, socketFactory, dataSenderFactory, clientProcessorFactory, stateMachine));
    }
}
