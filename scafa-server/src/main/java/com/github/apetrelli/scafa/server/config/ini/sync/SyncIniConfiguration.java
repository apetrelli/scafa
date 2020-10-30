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
package com.github.apetrelli.scafa.server.config.ini.sync;

import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.sync.ProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.output.DataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;
import com.github.apetrelli.scafa.server.config.ServerConfiguration;
import com.github.apetrelli.scafa.server.config.ini.AbstractIniConfiguration;

public class SyncIniConfiguration extends AbstractIniConfiguration<ProxyHttpConnectionFactory> {
    
    private SocketFactory<SyncSocket> socketFactory;
    private DataSenderFactory dataSenderFactory;
    private ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory;
    private HttpStateMachine<HttpHandler, Void> stateMachine;

    public static SyncIniConfiguration create(String profile, SocketFactory<SyncSocket> socketFactory,
            DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
            HttpStateMachine<HttpHandler, Void> stateMachine)
            throws IOException {
        Ini ini = AbstractIniConfiguration.loadIni(profile);
        SyncIniConfiguration configuration = new SyncIniConfiguration(ini, socketFactory, dataSenderFactory, clientProcessorFactory, stateMachine);
        configuration.initizializeServerConfigurations();
		return configuration;
    }

	protected SyncIniConfiguration(Ini ini, SocketFactory<SyncSocket> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, SyncSocket> clientProcessorFactory,
			HttpStateMachine<HttpHandler, Void> stateMachine) {
        super(ini);
        this.socketFactory = socketFactory;
        this.dataSenderFactory = dataSenderFactory;
        this.clientProcessorFactory = clientProcessorFactory;
        this.stateMachine = stateMachine;
    }

	@Override
	protected ServerConfiguration<ProxyHttpConnectionFactory> createServerConfiguration(Section section) {
		return new SyncIniServerConfiguration(section,
				this.socketFactory, this.dataSenderFactory, this.clientProcessorFactory, this.stateMachine);
	}
}