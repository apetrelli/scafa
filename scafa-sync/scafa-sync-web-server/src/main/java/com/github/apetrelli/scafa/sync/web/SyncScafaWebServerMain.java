package com.github.apetrelli.scafa.sync.web;

import com.github.apetrelli.scafa.sync.proto.jnet.DirectClientSyncSocketFactory;
import com.github.apetrelli.scafa.sync.proto.jnet.JnetSyncServerSocketFactoryFactory;
import com.github.apetrelli.scafa.sync.proto.loom.VirtualThreadRunnableStarterFactory;

public class SyncScafaWebServerMain {

	public static void main(String[] args) {
		
		ScafaWebServerLauncher launcher = new ScafaWebServerLauncher(new DirectClientSyncSocketFactory(),
				new JnetSyncServerSocketFactoryFactory(), new VirtualThreadRunnableStarterFactory());

		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                launcher.stop();
            }
        });

		String rootDirectory;
		if (args.length > 0) {
			rootDirectory = args[0];
		} else {
			rootDirectory = System.getProperty("user.dir");
		}
		launcher.launch(rootDirectory);
	}

}
