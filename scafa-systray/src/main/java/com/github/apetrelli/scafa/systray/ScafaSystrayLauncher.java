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

import com.github.apetrelli.scafa.ScafaLauncher;
import com.github.apetrelli.scafa.systray.edit.ConfigurationWindow;
import com.github.apetrelli.scafa.systray.edit.PromptWindow;

public class ScafaSystrayLauncher {

    private static final Logger LOG = Logger.getLogger(ScafaSystrayLauncher.class.getName());

    public void launch() {
        try (InputStream is = ScafaSystrayLauncher.class.getResourceAsStream("/scafa.png")) {
            Display.setAppName("Scafa");
			DeviceData data = new DeviceData();
			Display display = new Display(data);
			Shell shell = createIcon(is, display);
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

	private Shell createIcon(InputStream img, Display display) {
		Shell shell = new Shell(display);
        Image image = new Image(display, img);
        final Tray tray = display.getSystemTray();
        if (tray == null) {
            shell.dispose();
        } else  {
            TrayItem trayItem = new TrayItem(tray, SWT.NONE);
            trayItem.setImage(image);
            ScafaLauncher launcher = new ScafaLauncher();
            launcher.initialize();
            final Menu menu = createSelectionMenu(shell, launcher);
            final Menu editMenu = createEditMenu(new Shell(display), launcher);
            trayItem.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    menu.setVisible(true);
                }
            });
            trayItem.addListener(SWT.MenuDetect, new Listener() {
                public void handleEvent(Event event) {
                    editMenu.setVisible(true);
                }
            });
            trayItem.setImage(image);
        }
        return shell;
	}

	private Menu createSelectionMenu(Shell shell, ScafaLauncher launcher) {
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
		        launcher.launch(profile);
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
	private Menu createEditMenu(Shell shell, ScafaLauncher launcher) {
		final Menu menu = new Menu(shell, SWT.POP_UP);
		final MenuItem profilesItem = new MenuItem(menu, SWT.CASCADE);
		profilesItem.setText("Edit profiles");
		Menu profilesMenu = new Menu(shell, SWT.DROP_DOWN);
		profilesItem.setMenu(profilesMenu);
		String[] profiles = launcher.getProfiles();
		ConfigurationWindow configurationDialog = new ConfigurationWindow();
		for (int i = 0; i < profiles.length; i++) {
		    String profile = profiles[i];
		    MenuItem profileItem = new MenuItem(profilesMenu, SWT.PUSH);
		    profileItem.setText(profile);
		    profileItem.addListener(SWT.Selection, new Listener() {

		        @Override
		        public void handleEvent(Event event) {
		        	configurationDialog.open(profile);
		        }
		    });
		}
		MenuItem createProfileItem = new MenuItem(menu, SWT.PUSH);
		createProfileItem.setText("Create new profile");
		createProfileItem.addListener(SWT.Selection, e -> {
			PromptWindow prompt = new PromptWindow();
			prompt.open("Create new profile", "Profile name", t -> {
				configurationDialog.open(t);
			});
		});
		return menu;
	}

}
