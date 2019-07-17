package com.github.apetrelli.scafa.systray.edit;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.config.Configuration;

public class ConfigurationWindow {

	private Ini ini;

	protected Object result;
	protected Shell shell;
	private Text serverPortNumber;
	private Text proxyName;
	private Text proxyHost;
	private Text proxyPort;
	private Text proxyDomain;
	private Text proxyUsername;
	private Text proxyPassword;

	private static final Logger LOG = Logger.getLogger(ConfigurationWindow.class.getName());

	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		open(null);
	}

	public void open(String profile) {
		Display display = Display.getDefault();
		createContents();
		if (profile != null) {
			setConfiguration(profile);
		}
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void setConfiguration(String profile) {
		try {
			this.ini = Configuration.loadIni(profile);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Cannot load file for profile " + profile);
			this.ini = new Ini();
		}
		serverPortNumber.setText("");
		Section mainSection = ini.get("main");
		if (mainSection != null) {
			serverPortNumber.setText(mainSection.get("port"));
		}
	}



	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell();
		shell.setText("Configuration");
		shell.setSize(728, 609);
		shell.setLayout(new FormLayout());

		Label lblNewLabel = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Server port number");

		serverPortNumber = new Text(shell, SWT.BORDER);
		FormData fd_serverPortNumber = new FormData();
		fd_serverPortNumber.left = new FormAttachment(lblNewLabel, 6);
		fd_serverPortNumber.right = new FormAttachment(100, -10);
		fd_serverPortNumber.top = new FormAttachment(0);
		serverPortNumber.setLayoutData(fd_serverPortNumber);

		Label label = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_label = new FormData();
		fd_label.top = new FormAttachment(serverPortNumber, 6);
		fd_label.left = new FormAttachment(0, 29);
		fd_label.right = new FormAttachment(100, -10);
		label.setLayoutData(fd_label);

		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		fd_label.bottom = new FormAttachment(lblNewLabel_1, -2);
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.right = new FormAttachment(0, 153);
		fd_lblNewLabel_1.top = new FormAttachment(0, 39);
		fd_lblNewLabel_1.left = new FormAttachment(0, 33);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText("Proxy List");

		Button btnNewButton = new Button(shell, SWT.NONE);
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.width = 100;
		fd_btnNewButton.bottom = new FormAttachment(100, -10);
		fd_btnNewButton.right = new FormAttachment(100, -10);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("Cancel");

		Button btnNewButton_1 = new Button(shell, SWT.NONE);
		FormData fd_btnNewButton_1 = new FormData();
		fd_btnNewButton_1.width = 100;
		fd_btnNewButton_1.bottom = new FormAttachment(100, -10);
		fd_btnNewButton_1.right = new FormAttachment(btnNewButton, -6);
		btnNewButton_1.setLayoutData(fd_btnNewButton_1);
		btnNewButton_1.setText("Save");

		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.bottom = new FormAttachment(btnNewButton, -110, SWT.BOTTOM);
		fd_scrolledComposite.right = new FormAttachment(0, 189);
		fd_scrolledComposite.top = new FormAttachment(lblNewLabel_1, 6);
		fd_scrolledComposite.left = new FormAttachment(0, 10);
		scrolledComposite.setLayoutData(fd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		List proxyList = new List(scrolledComposite, SWT.BORDER);
		scrolledComposite.setContent(proxyList);
		scrolledComposite.setMinSize(proxyList.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Label lblNewLabel_2 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_2 = new FormData();
		fd_lblNewLabel_2.top = new FormAttachment(label, 31);
		fd_lblNewLabel_2.left = new FormAttachment(scrolledComposite, 6);
		lblNewLabel_2.setLayoutData(fd_lblNewLabel_2);
		lblNewLabel_2.setText("Name");

		proxyName = new Text(shell, SWT.BORDER);
		FormData fd_proxyName = new FormData();
		fd_proxyName.top = new FormAttachment(label, 31);
		fd_proxyName.right = new FormAttachment(100, -10);
		fd_proxyName.left = new FormAttachment(lblNewLabel_2, 6);
		proxyName.setLayoutData(fd_proxyName);

		Label lblNewLabel_3 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_3 = new FormData();
		fd_lblNewLabel_3.left = new FormAttachment(scrolledComposite, 6);
		fd_lblNewLabel_3.top = new FormAttachment(proxyName, 6);
		lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
		lblNewLabel_3.setText("Type");

		Combo proxyType = new Combo(shell, SWT.READ_ONLY);
		fd_proxyName.bottom = new FormAttachment(proxyType, -6);
		proxyType.setItems(new String[] {"Direct", "Proxy with anonymous connection", "Proxy with Basic authentication", "Proxy with NTML authentication"});
		FormData fd_proxyType = new FormData();
		fd_proxyType.left = new FormAttachment(lblNewLabel_3, 16);
		fd_proxyType.right = new FormAttachment(100, -10);
		fd_proxyType.top = new FormAttachment(0, 104);
		proxyType.setLayoutData(fd_proxyType);

		Label lblNewLabel_4 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_4 = new FormData();
		fd_lblNewLabel_4.left = new FormAttachment(scrolledComposite, 6);
		fd_lblNewLabel_4.top = new FormAttachment(proxyType, 6);
		lblNewLabel_4.setLayoutData(fd_lblNewLabel_4);
		lblNewLabel_4.setText("Host");

		proxyHost = new Text(shell, SWT.BORDER);
		FormData fd_proxyHost = new FormData();
		fd_proxyHost.left = new FormAttachment(lblNewLabel_4, 17);
		fd_proxyHost.right = new FormAttachment(100, -10);
		fd_proxyHost.top = new FormAttachment(proxyType, 6);
		proxyHost.setLayoutData(fd_proxyHost);

		Label lblNewLabel_5 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_5 = new FormData();
		fd_lblNewLabel_5.left = new FormAttachment(scrolledComposite, 6);
		fd_lblNewLabel_5.top = new FormAttachment(proxyHost, 6);
		lblNewLabel_5.setLayoutData(fd_lblNewLabel_5);
		lblNewLabel_5.setText("Port");

		proxyPort = new Text(shell, SWT.BORDER);
		FormData fd_proxyPort = new FormData();
		fd_proxyPort.left = new FormAttachment(lblNewLabel_5, 20);
		fd_proxyPort.right = new FormAttachment(100, -10);
		fd_proxyPort.top = new FormAttachment(proxyHost, 6);
		proxyPort.setLayoutData(fd_proxyPort);

		Label lblNewLabel_6 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_6 = new FormData();
		fd_lblNewLabel_6.left = new FormAttachment(scrolledComposite, 6);
		fd_lblNewLabel_6.top = new FormAttachment(proxyPort, 6);
		lblNewLabel_6.setLayoutData(fd_lblNewLabel_6);
		lblNewLabel_6.setText("Domain");

		proxyDomain = new Text(shell, SWT.BORDER);
		FormData fd_proxyDomain = new FormData();
		fd_proxyDomain.right = new FormAttachment(serverPortNumber, 0, SWT.RIGHT);
		fd_proxyDomain.top = new FormAttachment(proxyPort, 6);
		fd_proxyDomain.left = new FormAttachment(lblNewLabel_6, 6);
		proxyDomain.setLayoutData(fd_proxyDomain);

		Label lblNewLabel_7 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_7 = new FormData();
		fd_lblNewLabel_7.left = new FormAttachment(scrolledComposite, 6);
		fd_lblNewLabel_7.top = new FormAttachment(proxyDomain, 6);
		lblNewLabel_7.setLayoutData(fd_lblNewLabel_7);
		lblNewLabel_7.setText("Username");

		proxyUsername = new Text(shell, SWT.BORDER);
		FormData fd_proxyUsername = new FormData();
		fd_proxyUsername.right = new FormAttachment(100, -10);
		fd_proxyUsername.left = new FormAttachment(lblNewLabel_7, 6);
		fd_proxyUsername.top = new FormAttachment(proxyDomain, 6);
		proxyUsername.setLayoutData(fd_proxyUsername);

		Label lblNewLabel_8 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_8 = new FormData();
		fd_lblNewLabel_8.left = new FormAttachment(scrolledComposite, 6);
		fd_lblNewLabel_8.top = new FormAttachment(lblNewLabel_7, 17);
		lblNewLabel_8.setLayoutData(fd_lblNewLabel_8);
		lblNewLabel_8.setText("Password");

		proxyPassword = new Text(shell, SWT.BORDER);
		FormData fd_proxyPassword = new FormData();
		fd_proxyPassword.right = new FormAttachment(serverPortNumber, 0, SWT.RIGHT);
		fd_proxyPassword.top = new FormAttachment(proxyUsername, 6);
		fd_proxyPassword.left = new FormAttachment(lblNewLabel_8, 5);
		proxyPassword.setLayoutData(fd_proxyPassword);

		Label lblNewLabel_9 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_9 = new FormData();
		fd_lblNewLabel_9.left = new FormAttachment(scrolledComposite, 6);
		fd_lblNewLabel_9.top = new FormAttachment(lblNewLabel_8, 6);
		lblNewLabel_9.setLayoutData(fd_lblNewLabel_9);
		lblNewLabel_9.setText("Exclusions");

		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite_1 = new FormData();
		fd_scrolledComposite_1.bottom = new FormAttachment(scrolledComposite, 0, SWT.BOTTOM);
		fd_scrolledComposite_1.top = new FormAttachment(lblNewLabel_9, 6);
		fd_scrolledComposite_1.right = new FormAttachment(100, -10);
		fd_scrolledComposite_1.left = new FormAttachment(scrolledComposite, 6);
		scrolledComposite_1.setLayoutData(fd_scrolledComposite_1);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);

		List exclusions = new List(scrolledComposite_1, SWT.BORDER);
		scrolledComposite_1.setContent(exclusions);
		scrolledComposite_1.setMinSize(exclusions.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Button btnNewButton_2 = new Button(shell, SWT.NONE);
		FormData fd_btnNewButton_2 = new FormData();
		fd_btnNewButton_2.top = new FormAttachment(scrolledComposite, 6);
		fd_btnNewButton_2.left = new FormAttachment(0, 10);
		btnNewButton_2.setLayoutData(fd_btnNewButton_2);
		btnNewButton_2.setText("Add");

		Button btnNewButton_3 = new Button(shell, SWT.NONE);
		FormData fd_btnNewButton_3 = new FormData();
		fd_btnNewButton_3.top = new FormAttachment(scrolledComposite_1, 6);
		fd_btnNewButton_3.left = new FormAttachment(lblNewLabel_2, 0, SWT.LEFT);
		btnNewButton_3.setLayoutData(fd_btnNewButton_3);
		btnNewButton_3.setText("Apply");

	}
}
