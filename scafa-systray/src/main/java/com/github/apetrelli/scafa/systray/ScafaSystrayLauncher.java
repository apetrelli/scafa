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

import com.github.apetrelli.scafa.ScafaLauncher;

public class ScafaSystrayLauncher {

    private static final Logger LOG = Logger.getLogger(ScafaSystrayLauncher.class.getName());

    public void launch() {
        try (InputStream is = ScafaSystrayLauncher.class.getResourceAsStream("/scafa.png")) {
            Shell shell = createIcon(is);
            Display display = shell.getDisplay();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            display.dispose();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot load systray image", e);
            System.exit(1);
        }
    }

    private Shell createIcon(InputStream img) {
        Display.setAppName("Scafa");
        DeviceData data = new DeviceData();
        Display display = new Display(data);
        Shell shell = new Shell(display);
        Image image = new Image(display, img);
        final Tray tray = display.getSystemTray();
        if (tray == null) {
            shell.dispose();
        } else  {
            TrayItem trayItem = new TrayItem(tray, SWT.NONE);
            trayItem.setImage(image);
            final Menu menu = new Menu(shell, SWT.POP_UP);
            final MenuItem profilesItem = new MenuItem(menu, SWT.CASCADE);
            profilesItem.setText("Profiles");
            Menu profilesMenu = new Menu(shell, SWT.DROP_DOWN);
            profilesItem.setMenu(profilesMenu);
            ScafaLauncher launcher = new ScafaLauncher();
            launcher.initialize();
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
            trayItem.addListener(SWT.MenuDetect, new Listener() {
                public void handleEvent(Event event) {
                    menu.setVisible(true);
                }
            });
            trayItem.setImage(image);
        }
        return shell;
    }

}
