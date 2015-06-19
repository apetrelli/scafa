/**
 * Scafa - Universal roadwarrior non-caching proxy
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
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.http.HttpByteSink;
import com.github.apetrelli.scafa.http.HttpInput;
import com.github.apetrelli.scafa.http.HttpStatus;
import com.github.apetrelli.scafa.http.impl.ProxyHttpByteSinkFactory;

public class ScafaMain {

    private static final Logger LOG = Logger.getLogger(ScafaMain.class.getName());

    public static void main(String[] args) {
        try (InputStream stream = ScafaMain.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot load logging configuration, exiting", e);
            System.exit(1);
        }
        ScafaListener<HttpInput, HttpByteSink> proxy = new ScafaListener<>(new ProxyHttpByteSinkFactory(), HttpStatus.IDLE);
        try {
            proxy.listen();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot start proxy", e);
        }

        while (true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                LOG.log(Level.INFO, "Main thread interrupted", e);
                System.exit(0);
            }
        }
    }
}
