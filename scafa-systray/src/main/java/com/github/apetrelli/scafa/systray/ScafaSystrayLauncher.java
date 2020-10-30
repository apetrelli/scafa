package com.github.apetrelli.scafa.systray;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.github.apetrelli.scafa.server.ConfigurationUtils;
import com.github.apetrelli.scafa.server.AsyncScafaLauncher;
import com.github.apetrelli.scafa.systray.edit.ConfigurationWindow;
import com.github.apetrelli.scafa.systray.edit.PromptWindow;

public class ScafaSystrayLauncher {

    private static final Logger LOG = Logger.getLogger(ScafaSystrayLauncher.class.getName());
	private AsyncScafaLauncher launcher;
	private Menu selectionMenu;
	private Menu editMenu;
	private Shell shell;

    public void launch() {
        try (InputStream is = ScafaSystrayLauncher.class.getResourceAsStream("/scafa.png")) {
            Display.setAppName("Scafa");
			DeviceData data = new DeviceData();
			Display display = new Display(data);
			launcher = new AsyncScafaLauncher();
			launcher.initialize();
			shell = new Shell(display);
            selectionMenu = createSelectionMenu(shell, launcher);
            editMenu = createEditMenu(shell, launcher);
			createIcon(launcher, shell, is, display);
			String lastUsedProfile = launcher.getLastUsedProfile();
			if (lastUsedProfile != null) {
				launcher.launch(lastUsedProfile);
			}
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            SWTResourceManager.dispose();
            display.dispose();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot load systray image", e);
            System.exit(1);
        }
    }

    public void reload() {
    	selectionMenu.dispose();
    	editMenu.dispose();
        selectionMenu = createSelectionMenu(shell, launcher);
        editMenu = createEditMenu(shell, launcher);
    }

	private void createIcon(AsyncScafaLauncher launcher, Shell shell, InputStream img, Display display) {
		Image image = new Image(display, img);
        final Tray tray = display.getSystemTray();
        if (tray == null) {
            shell.dispose();
        } else  {
            TrayItem trayItem = new TrayItem(tray, SWT.NONE);
            trayItem.setImage(image);
            trayItem.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    selectionMenu.setVisible(true);
                }
            });
            trayItem.addListener(SWT.MenuDetect, new Listener() {
                public void handleEvent(Event event) {
                    editMenu.setVisible(true);
                }
            });
            trayItem.setImage(image);
        }
	}

	private Menu createSelectionMenu(Shell shell, AsyncScafaLauncher launcher) {
		final Menu menu = new Menu(shell, SWT.POP_UP);
		final MenuItem profilesItem = new MenuItem(menu, SWT.CASCADE);
		profilesItem.setText("Profiles");
		Menu profilesMenu = new Menu(shell, SWT.DROP_DOWN);
		profilesItem.setMenu(profilesMenu);
		String[] profiles = launcher.getProfiles();
		String lastUsedProfile = launcher.getLastUsedProfile();
		for (int i = 0; i < profiles.length; i++) {
		    String profile = profiles[i];
		    MenuItem profileItem = new MenuItem(profilesMenu, SWT.RADIO);
		    profileItem.setText(profile);
		    if (lastUsedProfile.equals(profile)) {
		        profileItem.setSelection(true);
		    }
		    profileItem.addListener(SWT.Selection, new Listener() {

		        @Override
		        public void handleEvent(Event event) {
		            launcher.stop();
		            launcher.launch(profile);
		            launcher.saveLastUsedProfile(profile);
		        }
		    });
		}
		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem gc = new MenuItem(menu, SWT.PUSH);
		gc.setText("Garbage collect");
		gc.addListener(SWT.Selection, new Listener() {

		    @Override
		    public void handleEvent(Event event) {
		        System.gc();
		    }
		});
		MenuItem exit = new MenuItem(menu, SWT.PUSH);
		exit.setText("Exit");
		exit.addListener(SWT.Selection, new Listener() {

		    @Override
		    public void handleEvent(Event event) {
		        launcher.stop();
		        shell.dispose();
		        System.exit(0);
		    }
		});
		return menu;
	}
	private Menu createEditMenu(Shell shell, AsyncScafaLauncher launcher) {
		final Menu menu = new Menu(shell, SWT.POP_UP);
		ConfigurationWindow configurationDialog = new ConfigurationWindow();
		MenuItem createProfileItem = new MenuItem(menu, SWT.PUSH);
		createProfileItem.setText("Create new profile");
		createProfileItem.addListener(SWT.Selection, e -> {
			PromptWindow prompt = new PromptWindow();
			prompt.open("Create new profile", "Profile name", t -> {
				configurationDialog.open(t);
	        	reload();
			});
		});
		final MenuItem profilesItem = new MenuItem(menu, SWT.CASCADE);
		profilesItem.setText("Edit profiles");
		Menu profilesMenu = new Menu(shell, SWT.DROP_DOWN);
		profilesItem.setMenu(profilesMenu);
		String[] profiles = launcher.getProfiles();
		for (int i = 0; i < profiles.length; i++) {
		    String profile = profiles[i];
		    MenuItem profileItem = new MenuItem(profilesMenu, SWT.PUSH);
		    profileItem.setText(profile);
		    profileItem.addListener(SWT.Selection, new Listener() {

		        @Override
		        public void handleEvent(Event event) {
		        	configurationDialog.open(profile);
		        	reload();
		        }
		    });
		}
		final MenuItem deleteProfilesItem = new MenuItem(menu, SWT.CASCADE);
		deleteProfilesItem.setText("Delete profiles");
		Menu deleteProfilesMenu = new Menu(shell, SWT.DROP_DOWN);
		deleteProfilesItem.setMenu(deleteProfilesMenu);
		for (int i = 0; i < profiles.length; i++) {
		    String profile = profiles[i];
		    MenuItem profileItem = new MenuItem(deleteProfilesMenu, SWT.PUSH);
		    profileItem.setText(profile);
		    profileItem.addListener(SWT.Selection, new Listener() {

		        @Override
		        public void handleEvent(Event event) {
		        	ConfigurationUtils.delete(profile);
		        	reload();
		        }
		    });
		}
		return menu;
	}

}
