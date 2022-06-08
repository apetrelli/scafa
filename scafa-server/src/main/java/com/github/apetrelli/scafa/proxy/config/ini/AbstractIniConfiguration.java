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
package com.github.apetrelli.scafa.proxy.config.ini;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.proxy.ConfigurationUtils;
import com.github.apetrelli.scafa.proxy.config.Configuration;
import com.github.apetrelli.scafa.proxy.config.ServerConfiguration;

public abstract class AbstractIniConfiguration<T> implements Configuration<T> {

    private Ini ini;

    private List<ServerConfiguration<T>> serverConfigurations;

	public static Ini loadIni(String profile) throws IOException {
		if (profile == null) {
            profile = "direct";
        }
        return ConfigurationUtils.loadIni(profile);
	}

	protected AbstractIniConfiguration(Ini ini) {
        this.ini = ini;
		initizializeServerConfigurations();
    }

    @Override
    public int getPort() {
        return ini.get("main").get("port", int.class);
    }

    @Override
    public ServerConfiguration<T> getServerConfigurationByHost(String host) {
        boolean found = false;
        Iterator<ServerConfiguration<T>> configIt = serverConfigurations.iterator();
        ServerConfiguration<T> config = null;
        while (!found && configIt.hasNext()) {
            config = configIt.next();
            List<String> excludeRegexp = config.getExcludes();
            boolean excluded = excludeRegexp.stream().anyMatch(host::matches);
            found = !excluded;
        }
        return found ? config : null;
    }

    @Override
    public List<ServerConfiguration<T>> getServerConfigurations() {
        return serverConfigurations;
    }

	protected void initizializeServerConfigurations() {
		serverConfigurations = ini
				.keySet().stream().filter(t -> !"main".equals(t)).map(ini::get).map(this::createServerConfiguration)
				.collect(Collectors.toList());
	}

	protected abstract ServerConfiguration<T> createServerConfiguration(Section section);
}
