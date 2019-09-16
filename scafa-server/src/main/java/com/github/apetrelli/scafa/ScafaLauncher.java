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
package com.github.apetrelli.scafa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.github.apetrelli.scafa.config.Configuration;
import com.github.apetrelli.scafa.http.HttpHandler;
import com.github.apetrelli.scafa.http.HttpProcessingContext;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.impl.HttpProcessingContextFactory;
import com.github.apetrelli.scafa.http.impl.HttpStateMachine;
import com.github.apetrelli.scafa.http.proxy.impl.ConfigurationProxyHttpConnectionFactory;
import com.github.apetrelli.scafa.http.proxy.impl.DefaultHttpConnectionFactoryFactory;
import com.github.apetrelli.scafa.http.proxy.impl.ProxyHttpHandlerFactory;
import com.github.apetrelli.scafa.proto.aio.ScafaListener;
import com.github.apetrelli.scafa.proto.aio.impl.DefaultProcessorFactory;
import com.github.apetrelli.scafa.proto.processor.impl.StatefulInputProcessorFactory;

public class ScafaLauncher {

    private static final Logger LOG = Logger.getLogger(ScafaLauncher.class.getName());
    private ScafaListener<HttpHandler> proxy;
    private File scafaDirectory;

    public void initialize() {
        File home = new File(System.getProperty("user.home"));
        scafaDirectory = new File(home, ".scafa");
        ensureConfigDirectoryPresent(scafaDirectory);
        try (InputStream stream = new FileInputStream(new File(scafaDirectory, "logging.properties"))) {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot load logging configuration, exiting", e);
            System.exit(1);
        }
    }

    public String getLastUsedProfile() {
        File file = new File(scafaDirectory, "lastused.txt");
        if (file.exists()) {
            try {
                String profile = FileUtils.readFileToString(file);
                File profileFile = new File(scafaDirectory, profile + ".ini");
                if (profileFile.exists()) {
                    return profile;
                } else {
                    LOG.severe("The file " + profile + ".ini does not exist, defaulting to direct");
                }
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Cannot load current profile configuration", e);
            }
            return null;
        }
        return "direct";
    }

    public void saveLastUsedProfile(String profile) {
        File file = new File(scafaDirectory, "lastused.txt");
        try {
            FileUtils.write(file, profile);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot write current profile configuration", e);
        }
    }

    public String[] getProfiles() {
        File[] files = scafaDirectory.listFiles((File d, String n ) -> { return n.endsWith(".ini");});
        String[] profiles = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String filename = files[i].getName();
            profiles[i] = filename.substring(0, filename.lastIndexOf('.'));
        }
        return profiles;
    }

    public void launch(String profile) {
        File home = new File(System.getProperty("user.home"));
        File scafaDirectory = new File(home, ".scafa");
        ensureConfigDirectoryPresent(scafaDirectory);
        try (InputStream stream = new FileInputStream(new File(scafaDirectory, "logging.properties"))) {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot load logging configuration, exiting", e);
            System.exit(1);
        }
        try {
        	HttpStateMachine stateMachine = new HttpStateMachine();
            Configuration configuration = Configuration.create(profile, stateMachine);
            Integer port = configuration.getMainConfiguration().get("port", int.class);
            StatefulInputProcessorFactory<HttpHandler, HttpStatus, HttpProcessingContext> inputProcessorFactory = new StatefulInputProcessorFactory<>(stateMachine);
            HttpProcessingContextFactory processingContextFactory = new HttpProcessingContextFactory();
            DefaultHttpConnectionFactoryFactory connectionFactoryFactory = new DefaultHttpConnectionFactoryFactory(
                    new ConfigurationProxyHttpConnectionFactory(configuration));
            ProxyHttpHandlerFactory proxyHttpHandlerFactory = new ProxyHttpHandlerFactory(connectionFactoryFactory);
            DefaultProcessorFactory<HttpStatus, HttpProcessingContext, HttpHandler> defaultProcessorFactory = new DefaultProcessorFactory<>(
                    inputProcessorFactory, processingContextFactory);
            proxy = new ScafaListener<HttpHandler>(defaultProcessorFactory, proxyHttpHandlerFactory, port);
            proxy.listen();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot start proxy", e);
        }
    }

    public void stop() {
        if (proxy != null) {
            proxy.stop();
        }
    }

    private static void ensureConfigDirectoryPresent(File scafaDirectory) {
        if (!scafaDirectory.exists()) {
            scafaDirectory.mkdirs();
            copyToConfigDirectory(scafaDirectory, "direct.ini");
            copyToConfigDirectory(scafaDirectory, "work.ini");
            copyToConfigDirectory(scafaDirectory, "logging.properties");
        }
    }

    private static void copyToConfigDirectory(File scafaDirectory, String filename) {
        File destination = new File(scafaDirectory, filename);
        try (InputStream is = ScafaLauncher.class.getResourceAsStream("/config/" + filename);
                OutputStream os = new FileOutputStream(destination)) {
            IOUtils.copy(is, os);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot transfer file", e);
        }
    }
}
