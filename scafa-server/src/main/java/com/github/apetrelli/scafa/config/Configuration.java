/**
 * Scafa - A universal non-caching proxy for the road warrior
 * Copyright (C) 2015  Antonio Petrelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.apetrelli.scafa.config;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.ntlm.NtlmProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.HttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.AnonymousProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.BasicAuthProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.DirectHttpConnectionFactory;

public class Configuration {
    private Ini ini;

    private Map<String, HttpConnectionFactory> sectionName2factory;

    private HttpConnectionFactory directHttpConnectionFactory = new DirectHttpConnectionFactory();

    public static Configuration create(String profile) throws InvalidFileFormatException, IOException {
        if (profile == null) {
            profile = "direct";
        }
        Ini ini = new Ini();
        ini.load(new File(System.getProperty("user.home") + "/.scafa/" + profile + ".ini"));
        return new Configuration(ini);
    }

    private Configuration(Ini ini) {
        this.ini = ini;
        sectionName2factory = new LinkedHashMap<>();
        ini.keySet().stream().filter(t -> !"main".equals(t)).map(t -> ini.get(t)).forEach(t -> {
            switch (t.get("type")) {
            case "ntlm":
                sectionName2factory.put(t.getName(), new NtlmProxyHttpConnectionFactory(t));
                break;
            case "anon":
                sectionName2factory.put(t.getName(), new AnonymousProxyHttpConnectionFactory(t));
                break;
            case "basic":
                sectionName2factory.put(t.getName(), new BasicAuthProxyHttpConnectionFactory(t));
                break;
            default:
                sectionName2factory.put(t.getName(), new DirectHttpConnectionFactory(t));            }
        });
    }

    public Section getMainConfiguration() {
        return ini.get("main");
    }

    private Section getConfigurationByHost(String host) {
        boolean found = false;
        Iterator<String> keyIt = ini.keySet().iterator();
        Section section = null;
        while (!found && keyIt.hasNext()) {
            section = ini.get(keyIt.next());
            if (!"main".equals(section.getName())) {
                List<String> excludeRegexp = section.getAll("excludeRegexp");
                if (excludeRegexp == null || excludeRegexp.isEmpty()) {
                    List<String> exclude = section.getAll("exclude");
                    if (exclude == null || exclude.isEmpty()) {
                        // Just to avoid useless re-execution
                        section.put("excludeRegexp", "doesnotmatchathing");
                        excludeRegexp = section.getAll("excludeRegexp");
                    } else {
                        excludeRegexp = exclude.stream().map(t -> createRegexpFromWildcard(t))
                                .collect(Collectors.toList());
                        section.putAll("excludeRegexp", excludeRegexp);
                    }
                }
                boolean excluded = excludeRegexp.stream().anyMatch(t -> host.matches(t));
                found = !excluded;
            }
        }
        return found ? section : null;
    }

    public HttpConnectionFactory getHttpConnectionFactoryByHost(String host) {
        HttpConnectionFactory factory = null;
        Section section = getConfigurationByHost(host);
        if (section != null) {
            factory = sectionName2factory.get(section.getName());
        }
        return factory != null ? factory : directHttpConnectionFactory;
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
