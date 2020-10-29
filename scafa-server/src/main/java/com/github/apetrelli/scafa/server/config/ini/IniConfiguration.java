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
package com.github.apetrelli.scafa.server.config.ini;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.ini4j.Ini;

import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.server.ConfigurationUtils;
import com.github.apetrelli.scafa.server.config.Configuration;
import com.github.apetrelli.scafa.server.config.ServerConfiguration;

public class IniConfiguration implements Configuration {

    private Ini ini;

    private List<ServerConfiguration> serverConfigurations;

    public static IniConfiguration create(String profile, SocketFactory<AsyncSocket> socketFactory,
            DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
            HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine)
            throws IOException {
        if (profile == null) {
            profile = "direct";
        }
        Ini ini = ConfigurationUtils.loadIni(profile);
        return new IniConfiguration(ini, socketFactory, dataSenderFactory, clientProcessorFactory, stateMachine);
    }

	private IniConfiguration(Ini ini, SocketFactory<AsyncSocket> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
			HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine) {
        this.ini = ini;
		serverConfigurations = ini
				.keySet().stream().filter(t -> !"main".equals(t)).map(t -> new IniServerConfiguration(ini.get(t),
						socketFactory, dataSenderFactory, clientProcessorFactory, stateMachine))
				.collect(Collectors.toList());
    }

    @Override
    public int getPort() {
        return ini.get("main").get("port", int.class);
    }

    @Override
    public ServerConfiguration getServerConfigurationByHost(String host) {
        boolean found = false;
        Iterator<ServerConfiguration> configIt = serverConfigurations.iterator();
        ServerConfiguration config = null;
        while (!found && configIt.hasNext()) {
            config = configIt.next();
            List<String> excludeRegexp = config.getExcludes();
            boolean excluded = excludeRegexp.stream().anyMatch(host::matches);
            found = !excluded;
        }
        return found ? config : null;
    }

    @Override
    public List<ServerConfiguration> getServerConfigurations() {
        return serverConfigurations;
    }
}
