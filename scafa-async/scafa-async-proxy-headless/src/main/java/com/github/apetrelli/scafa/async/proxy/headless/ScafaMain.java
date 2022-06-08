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
package com.github.apetrelli.scafa.async.proxy.headless;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.apetrelli.scafa.async.proxy.AsyncScafaLauncher;

public class ScafaMain {

    private static final Logger LOG = Logger.getLogger(ScafaMain.class.getName());

    public static void main(String[] args) {
        String profile = null;
        if (args.length > 0) {
            profile = args[0];
        }
        AsyncScafaLauncher launcher = new AsyncScafaLauncher();
        launcher.initialize();
        launcher.launch(profile);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                launcher.stop();
            }
        });

        while (true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                LOG.log(Level.INFO, "Main thread interrupted", e);
            }
        }
    }
}
