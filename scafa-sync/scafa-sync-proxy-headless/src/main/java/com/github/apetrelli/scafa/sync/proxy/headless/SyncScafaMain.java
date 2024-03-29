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
package com.github.apetrelli.scafa.sync.proxy.headless;

import com.github.apetrelli.scafa.sync.proto.jnet.DirectClientSyncSocketFactory;
import com.github.apetrelli.scafa.sync.proto.jnet.JnetSyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.sync.proto.loom.VirtualThreadRunnableStarterFactory;
import com.github.apetrelli.scafa.sync.proxy.SyncScafaLauncher;

public class SyncScafaMain {

    public static void main(String[] args) {
        String profile = null;
        if (args.length > 0) {
            profile = args[0];
        }

		SyncScafaLauncher launcher = new SyncScafaLauncher(new DirectClientSyncSocketFactory(),
				new JnetSyncServerSocketFactoryFactory(), new VirtualThreadRunnableStarterFactory());
        launcher.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                launcher.stop();
            }
        });

        launcher.launch(profile);
    }
}
