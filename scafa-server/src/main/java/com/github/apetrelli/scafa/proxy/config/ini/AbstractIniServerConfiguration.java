package com.github.apetrelli.scafa.proxy.config.ini;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.proxy.HttpRequestManipulator;
import com.github.apetrelli.scafa.proto.client.HostPort;
import com.github.apetrelli.scafa.proxy.config.ServerConfiguration;

public class AbstractIniServerConfiguration<T> implements ServerConfiguration<T> {

	private static final Logger LOG = Logger.getLogger(AbstractIniServerConfiguration.class.getName());

    protected static HttpRequestManipulator createManipulator(Section section) {
        String className = section.get("manipulator");
        HttpRequestManipulator manipulator = null;
        if (className != null) {
            try {
                Class<? extends HttpRequestManipulator> clazz = Class.forName(className)
                        .asSubclass(HttpRequestManipulator.class);
                manipulator = clazz.getConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOG.log(Level.SEVERE, e, () -> "Cannot instantiate manipulator: " + className);
            }
        }
        return manipulator;
    }

    protected static HostPort createProxySocketAddress(Section section) {
        String host = section.get("host");
        int port = section.get("port", int.class);
        return new HostPort(host, port);
    }

    protected T connectionFactory;

    private List<String> excludes;

    public AbstractIniServerConfiguration(Section section, T connectionFactory) {
    	this.connectionFactory = connectionFactory;
        buildExcludes(section);
    }

	private void buildExcludes(Section section) {
		List<String> exclude = section.getAll("exclude");
        if (exclude == null) {
            excludes = Collections.emptyList();
        } else {
            excludes = exclude.stream().map(this::createRegexpFromWildcard).collect(Collectors.toList());
        }
	}

    @Override
    public List<String> getExcludes() {
        return excludes;
    }

    @Override
    public T getProxyHttpConnectionFactory() {
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
}
