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
package com.github.apetrelli.scafa.server.config.ini.async;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.http.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.gateway.GatewayHttpConnectionFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.proto.aio.AsyncSocket;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.processor.DataHandler;
import com.github.apetrelli.scafa.proto.processor.ProcessorFactory;
import com.github.apetrelli.scafa.server.config.ServerConfiguration;
import com.github.apetrelli.scafa.server.config.ini.AbstractIniConfiguration;

public class AsyncIniConfiguration extends AbstractIniConfiguration<GatewayHttpConnectionFactory<ProxyHttpConnection>> {
    
    private SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory;
    private DataSenderFactory dataSenderFactory;
    private ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory;
    private HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine;

    public static AsyncIniConfiguration create(String profile, SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
            DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
            HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine)
            throws IOException {
        Ini ini = AbstractIniConfiguration.loadIni(profile);
        AsyncIniConfiguration configuration = new AsyncIniConfiguration(ini, socketFactory, dataSenderFactory, clientProcessorFactory, stateMachine);
        configuration.initizializeServerConfigurations();
		return configuration;
    }

	protected AsyncIniConfiguration(Ini ini, SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory,
			DataSenderFactory dataSenderFactory, ProcessorFactory<DataHandler, AsyncSocket> clientProcessorFactory,
			HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine) {
        super(ini);
        this.socketFactory = socketFactory;
        this.dataSenderFactory = dataSenderFactory;
        this.clientProcessorFactory = clientProcessorFactory;
        this.stateMachine = stateMachine;
    }

	@Override
	protected ServerConfiguration<GatewayHttpConnectionFactory<ProxyHttpConnection>> createServerConfiguration(Section section) {
		return new AsyncIniServerConfiguration(section,
				this.socketFactory, this.dataSenderFactory, this.clientProcessorFactory, this.stateMachine);
	}
}
