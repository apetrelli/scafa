package com.github.apetrelli.scafa.server.config.ini;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.AnonymousProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.BasicAuthProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.DirectHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.ntlm.NtlmProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.server.config.ServerConfiguration;

public class IniServerConfiguration implements ServerConfiguration {
    private static final Logger LOG = Logger.getLogger(IniServerConfiguration.class.getName());

    private ProxyHttpConnectionFactory connectionFactory;

    private List<String> excludes;

    public IniServerConfiguration(Section section, SocketFactory<AsyncSocket> socketFactory,
            DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
            HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine) {
        String type = section.get("type");
        switch (type) {
        case "ntlm":
            connectionFactory = new NtlmProxyHttpConnectionFactory(socketFactory, dataSenderFactory,
                    clientProcessorFactory, createProxySocketAddress(section), section.get("interface"),
                    section.get("forceIPV4", boolean.class, false), section.get("domain"), section.get("username"),
                    section.get("password"), createManipulator(section), stateMachine);
            break;
        case "anon":
            connectionFactory = new AnonymousProxyHttpConnectionFactory(socketFactory, dataSenderFactory,
                    clientProcessorFactory, createProxySocketAddress(section), section.get("interface"),
                    section.get("forceIPV4", boolean.class, false), createManipulator(section));
            break;
        case "basic":
            connectionFactory = new BasicAuthProxyHttpConnectionFactory(socketFactory, dataSenderFactory,
                    clientProcessorFactory, createProxySocketAddress(section), section.get("interface"),
                    section.get("forceIPV4", boolean.class, false), section.get("username"), section.get("password"),
                    createManipulator(section));
            break;
        default:
			connectionFactory = new DirectHttpConnectionFactory(socketFactory, dataSenderFactory,
					clientProcessorFactory);
            break;
        }
        List<String> exclude = section.getAll("exclude");
        if (exclude == null) {
            excludes = Collections.emptyList();
        } else {
            excludes = exclude.stream().map(u -> createRegexpFromWildcard(u)).collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getExcludes() {
        return excludes;
    }

    @Override
    public ProxyHttpConnectionFactory getProxyHttpConnectionFactory() {
        return connectionFactory;
    }

    private String createRegexpFromWildcard(String subject) {
        Pattern regex = Pattern.compile("[^*]+|(\\*)");
        Matcher m = regex.matcher(subject);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            if (m.group(1) != null)
                m.appendReplacement(b, ".*");
            else
                m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
        }
        m.appendTail(b);
        return b.toString();
    }

    private static HttpRequestManipulator createManipulator(Section section) {
        String className = section.get("manipulator");
        HttpRequestManipulator manipulator = null;
        if (className != null) {
            try {
                Class<? extends HttpRequestManipulator> clazz = Class.forName(className)
                        .asSubclass(HttpRequestManipulator.class);
                manipulator = clazz.getConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOG.log(Level.SEVERE, "Cannot instantiate manipulator: " + className, e);
            }
        }
        return manipulator;
    }

    private static HostPort createProxySocketAddress(Section section) {
        String host = section.get("host");
        int port = section.get("port", int.class);
        HostPort proxySocketAddress = new HostPort(host, port);
        return proxySocketAddress;
    }
}
