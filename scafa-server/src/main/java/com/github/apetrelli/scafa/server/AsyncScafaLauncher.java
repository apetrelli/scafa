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
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.async.proto.aio.DirectAsyncServerSocketFactory;
import com.github.apetrelli.scafa.async.proto.aio.DirectClientAsyncSocketFactory;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpRequest;
import com.github.apetrelli.scafa.http.async.HttpAsyncSocket;
import com.github.apetrelli.scafa.http.async.HttpHandler;
import com.github.apetrelli.scafa.http.async.direct.DirectHttpAsyncSocketFactory;
import com.github.apetrelli.scafa.http.async.impl.AsyncHttpSink;
import com.github.apetrelli.scafa.http.async.output.DataSenderFactory;
import com.github.apetrelli.scafa.http.async.output.impl.DefaultDataSenderFactory;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.impl.DefaultHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.proxy.impl.ProxyHttpHandlerFactory;
import com.github.apetrelli.scafa.proto.SocketFactory;
import com.github.apetrelli.scafa.proto.async.ScafaListener;
import com.github.apetrelli.scafa.proto.async.processor.DataHandler;
import com.github.apetrelli.scafa.proto.async.processor.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.async.processor.impl.PassthroughInputProcessorFactory;
import com.github.apetrelli.scafa.proto.async.processor.impl.StatefulInputProcessorFactory;
import com.github.apetrelli.scafa.proto.async.socket.AsyncServerSocketFactory;
import com.github.apetrelli.scafa.proto.async.socket.AsyncSocket;
import com.github.apetrelli.scafa.proto.data.Input;
import com.github.apetrelli.scafa.proto.data.impl.SimpleInputFactory;
import com.github.apetrelli.scafa.server.config.ConfigurationProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.server.config.ini.async.AsyncIniConfiguration;

public class AsyncScafaLauncher extends AbstractScafaLauncher {

    private static final Logger LOG = Logger.getLogger(AsyncScafaLauncher.class.getName());
    private ScafaListener<HttpHandler, AsyncSocket> proxy;


    @Override
    public void launch(String profile) {
        try {
        	HttpStateMachine<HttpHandler, CompletableFuture<Void>> stateMachine = new HttpStateMachine<>(new AsyncHttpSink());
            DataSenderFactory dataSenderFactory = new DefaultDataSenderFactory();
            SocketFactory<HttpAsyncSocket<HttpRequest>> socketFactory = new DirectHttpAsyncSocketFactory(new DirectClientAsyncSocketFactory(), dataSenderFactory);
            DefaultProcessorFactory<Input, DataHandler> clientProcessorFactory = new DefaultProcessorFactory<>(
            		new PassthroughInputProcessorFactory(), new SimpleInputFactory());
			AsyncIniConfiguration configuration = AsyncIniConfiguration.create(profile, socketFactory, dataSenderFactory,
					clientProcessorFactory, stateMachine);
            Integer port = configuration.getPort();
            StatefulInputProcessorFactory<HttpHandler, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
            DefaultHttpConnectionFactoryFactory connectionFactoryFactory = new DefaultHttpConnectionFactoryFactory(
                    new ConfigurationProxyHttpConnectionFactory(configuration, socketFactory, dataSenderFactory, clientProcessorFactory));
            HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
            ProxyHttpHandlerFactory proxyHttpHandlerFactory = new ProxyHttpHandlerFactory(connectionFactoryFactory);
            AsyncServerSocketFactory<AsyncSocket> asyncServerSocketFactory = new DirectAsyncServerSocketFactory(port);
            DefaultProcessorFactory<HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                    inputProcessorFactory, processingContextFactory);
            proxy = new ScafaListener<>(asyncServerSocketFactory, defaultProcessorFactory, proxyHttpHandlerFactory);
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
