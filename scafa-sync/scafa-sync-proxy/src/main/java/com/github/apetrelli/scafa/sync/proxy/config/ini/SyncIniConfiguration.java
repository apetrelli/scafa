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
package com.github.apetrelli.scafa.sync.proxy.config.ini;

import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proxy.config.ServerConfiguration;
import com.github.apetrelli.scafa.proxy.config.ini.AbstractIniConfiguration;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;

public class SyncIniConfiguration extends AbstractIniConfiguration<GatewayHttpConnectionFactory<ProxyHttpConnection>> {
    
    private final SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory;
    private final DataSenderFactory dataSenderFactory;
    private final ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory;
    private final RunnableStarter runnableStarter;
    private final HttpStateMachine<HttpHandler, Void> stateMachine;

	public static SyncIniConfiguration create(String profile, SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			RunnableStarter runnableStarter, HttpStateMachine<HttpHandler, Void> stateMachine) throws IOException {
        Ini ini = AbstractIniConfiguration.loadIni(profile);
		SyncIniConfiguration configuration = new SyncIniConfiguration(ini, socketFactory, dataSenderFactory,
				clientProcessorFactory, runnableStarter, stateMachine);
        configuration.initizializeServerConfigurations();
		return configuration;
    }

	protected SyncIniConfiguration(Ini ini, SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			RunnableStarter runnableStarter, HttpStateMachine<HttpHandler, Void> stateMachine) {
        super(ini);
        this.socketFactory = socketFactory;
        this.dataSenderFactory = dataSenderFactory;
        this.clientProcessorFactory = clientProcessorFactory;
        this.runnableStarter = runnableStarter;
        this.stateMachine = stateMachine;
    }

	@Override
	protected ServerConfiguration<GatewayHttpConnectionFactory<ProxyHttpConnection>> createServerConfiguration(Section section) {
		return new SyncIniServerConfiguration(section, socketFactory, dataSenderFactory,
				clientProcessorFactory, runnableStarter, stateMachine);
	}
}
