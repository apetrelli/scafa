package com.github.apetrelli.scafa.sync.web;

public class SyncScafaWebServerMain {

	public static void main(String[] args) {
		
		com.github.apetrelli.scafa.sync.web.ScafaWebServerLauncher launcher = new com.github.apetrelli.scafa.sync.web.ScafaWebServerLauncher();

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
