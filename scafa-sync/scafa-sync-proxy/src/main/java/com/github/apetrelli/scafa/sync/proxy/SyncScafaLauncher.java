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
package com.github.apetrelli.scafa.sync.proxy;

import java.io.IOException;
import java.util.logging.Level;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.proxy.AbstractScafaLauncher;
import com.github.apetrelli.scafa.sync.http.HttpHandler;
import com.github.apetrelli.scafa.sync.http.HttpSyncSocket;
import com.github.apetrelli.scafa.sync.http.gateway.direct.DefaultGatewayHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.sync.http.impl.SyncHttpSink;
import com.github.apetrelli.scafa.sync.http.output.DataSenderFactory;
import com.github.apetrelli.scafa.sync.http.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.sync.http.proxy.ProxyHttpConnection;
import com.github.apetrelli.scafa.sync.http.proxy.handler.ProxyHttpHandlerFactory;
import com.github.apetrelli.scafa.sync.http.socket.direct.DirectHttpSyncSocketFactory;
import com.github.apetrelli.scafa.sync.proto.RunnableStarter;
import com.github.apetrelli.scafa.sync.proto.RunnableStarterFactory;
import com.github.apetrelli.scafa.sync.proto.ScafaListener;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactory;
import com.github.apetrelli.scafa.sync.proto.SyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.sync.proto.SyncSocket;
import com.github.apetrelli.scafa.sync.proto.processor.DataHandler;
import com.github.apetrelli.scafa.sync.proto.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.sync.proto.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.sync.proxy.config.SyncConfigurationProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.sync.proxy.config.ini.SyncIniConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class SyncScafaLauncher extends AbstractScafaLauncher {
	
	private final SocketFactory<SyncSocket> clientSocketFactory;
	
	private final SyncServerSocketFactoryFactory serverSocketFactoryFactory;
    
	private final RunnableStarterFactory runnableStarterFactory;
	
    private ScafaListener<HttpHandler, SyncSocket> proxy;

    @Override
    public void launch(String profile) {
    	HttpStateMachine<HttpHandler, Void> stateMachine = new HttpStateMachine<>(new SyncHttpSink());
    	try (RunnableStarter runnableStarter = runnableStarterFactory.create();
        		DefaultProcessorFactory<Input, DataHandler> clientProcessorFactory = new DefaultProcessorFactory<>(
                		new PassthroughInputProcessorFactory(), new SimpleInputFactory());
        		DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                        new StatefulInputProcessorFactory<>(stateMachine), new HttpProcessingContextFactory())) {
            DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();
			SocketFactory<HttpSyncSocket<HttpRequest>> socketFactory = new DirectHttpSyncSocketFactory(
					clientSocketFactory, dataSenderFactory);
			SyncIniConfiguration configuration = SyncIniConfiguration.create(profile, socketFactory, dataSenderFactory,
					clientProcessorFactory, runnableStarter, stateMachine);
            Integer port = configuration.getPort();
            DefaultGatewayHttpConnectionFactoryFactory<ProxyHttpConnection> connectionFactoryFactory = new DefaultGatewayHttpConnectionFactoryFactory<>(
					new SyncConfigurationProxyHttpConnectionFactory(configuration, socketFactory, dataSenderFactory,
							clientProcessorFactory, runnableStarter));
            ProxyHttpHandlerFactory proxyHttpHandlerFactory = new ProxyHttpHandlerFactory(connectionFactoryFactory);
            SyncServerSocketFactory<SyncSocket> syncServerSocketFactory = serverSocketFactoryFactory.create(port);
            proxy = new ScafaListener<>(syncServerSocketFactory, defaultProcessorFactory, proxyHttpHandlerFactory, runnableStarter);
            proxy.listen();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot start proxy", e);
        }
    }

    @Override
    public void stop() {
        if (proxy != null) {
            proxy.stop();
        }
    }
}
