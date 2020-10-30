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
package com.github.apetrelli.scafa.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.sync.connection.DefaultHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.proxy.sync.handler.ProxyHttpHandlerFactory;
import com.github.apetrelli.scafa.http.sync.HttpHandler;
import com.github.apetrelli.scafa.http.sync.SyncHttpSink;
import com.github.apetrelli.scafa.http.sync.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.sync.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.proto.aio.SocketFactory;
import com.github.apetrelli.scafa.proto.processor.Input;
import com.github.apetrelli.scafa.proto.processor.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.proto.sync.ScafaListener;
import com.github.apetrelli.scafa.proto.sync.SyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.sync.SyncSocket;
import com.github.apetrelli.scafa.proto.sync.processor.DataHandler;
import com.github.apetrelli.scafa.proto.sync.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.proto.sync.socket.DirectClientSyncSocketFactory;
import com.github.apetrelli.scafa.proto.sync.socket.DirectSyncServerSocketFactory;
import com.github.apetrelli.scafa.server.config.SyncConfigurationProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.server.config.ini.sync.SyncIniConfiguration;


public class SyncScafaLauncher extends AbstractScafaLauncher {

    private static final Logger LOG = Logger.getLogger(SyncScafaLauncher.class.getName());
    private ScafaListener<HttpHandler, SyncSocket> proxy;


    @Override
    public void launch(String profile) {
        try {
        	HttpStateMachine<HttpHandler, Void> stateMachine = new HttpStateMachine<>(new SyncHttpSink());
            DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();
            SocketFactory<SyncSocket> socketFactory = new DirectClientSyncSocketFactory();
            DefaultProcessorFactory<Input, DataHandler> clientProcessorFactory = new DefaultProcessorFactory<>(
            		new PassthroughInputProcessorFactory(), new SimpleInputFactory());
			SyncIniConfiguration configuration = SyncIniConfiguration.create(profile, socketFactory, dataSenderFactory,
					clientProcessorFactory, stateMachine);
            Integer port = configuration.getPort();
            StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
            DefaultHttpConnectionFactoryFactory connectionFactoryFactory = new DefaultHttpConnectionFactoryFactory(
                    new SyncConfigurationProxyHttpConnectionFactory(configuration, socketFactory, dataSenderFactory, clientProcessorFactory));
            HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
            ProxyHttpHandlerFactory proxyHttpHandlerFactory = new ProxyHttpHandlerFactory(connectionFactoryFactory);
            SyncServerSocketFactory<SyncSocket> syncServerSocketFactory = new DirectSyncServerSocketFactory(port);
            DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                    inputProcessorFactory, processingContextFactory);
            proxy = new ScafaListener<>(syncServerSocketFactory, defaultProcessorFactory, proxyHttpHandlerFactory);
            proxy.listen();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot start proxy", e);
        }
    }

    @Override
    public void stop() {
        if (proxy != null) {
            proxy.stop();
        }
    }
}
