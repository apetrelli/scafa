package com.github.apetrelli.scafa.systray.edit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.wb.swt.SWTResourceManager;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.apetrelli.scafa.ConfigurationUtils;

public class ConfigurationWindow {

	private static final Logger LOG = Logger.getLogger(ConfigurationWindow.class.getName());

	private static final String[] PROXY_TYPES = {"direct", "anon", "basic", "ntlm"};

	private static final Map<String, Integer> PROXY_TYPE_TO_POSITION;

	static {
		PROXY_TYPE_TO_POSITION = new HashMap<String, Integer>();
		for (int i = 0; i < PROXY_TYPES.length; i++) {
			PROXY_TYPE_TO_POSITION.put(PROXY_TYPES[i], i);
		}
	}

	private Ini ini;
	private String profile;

	protected Object result;
	protected Shell shell;
	private Text serverPortNumber;
	private Text proxyHost;
	private Text proxyPort;
	private Text proxyDomain;
	private Text proxyUsername;
	private Text proxyPassword;

	private List proxyList;

	private List proxyExclusions;

	private Combo proxyType;
	private Text proxyExclusionToAdd;
	private Label proxyName;

	private static String emptyIfNull(String string) {
		return string == null ? "" : string;
	}

	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		open(null);
	}

	public void open(String profile) {
		this.profile = profile;
		Display display = Display.getDefault();
		createContents();
		if (profile != null) {
			setConfiguration(profile);
		}
		shell.setText("Configuration for " + profile + " profile");
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
			this.ini = ConfigurationUtils.loadIni(profile);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Cannot load file for profile " + profile);
			this.ini = new Ini();
		}
		serverPortNumber.setText("");
		Section mainSection = ini.get("main");
		if (mainSection == null) {
			mainSection = ini.add("main");
		}
		String serverPort = mainSection.get("port");
		if (serverPort != null) {
			serverPortNumber.setText(serverPort);
		}
		for (Section section : ini.values()) {
			if (!"main".equals(section.getName())) {
				proxyList.add(section.getName());
			}
		}
	}



	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell();
		shell.setImage(SWTResourceManager.getImage(ConfigurationWindow.class, "/scafa.png"));
		shell.setText("Configuration");
		shell.setSize(728, 688);
		shell.setLayout(new FormLayout());

		Label lblNewLabel = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		fd_lblNewLabel.left = new FormAttachment(0, 10);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Server port number");

		serverPortNumber = new Text(shell, SWT.BORDER);
		FormData fd_serverPortNumber = new FormData();
		fd_serverPortNumber.left = new FormAttachment(lblNewLabel, 6);
		fd_serverPortNumber.right = new FormAttachment(100, -10);
		fd_serverPortNumber.top = new FormAttachment(0, 10);
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

		Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		FormData fd_cancelButton = new FormData();
		fd_cancelButton.width = 100;
		fd_cancelButton.bottom = new FormAttachment(100, -10);
		fd_cancelButton.right = new FormAttachment(100, -10);
		cancelButton.setLayoutData(fd_cancelButton);
		cancelButton.setText("Cancel");

		Button btnNewButton_1 = new Button(shell, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyProxyData();
				Section mainSection = ini.get("main");
				if (mainSection == null) {
					mainSection = ini.add("main");
				}
				mainSection.put("port", serverPortNumber.getText());
				try {
					ConfigurationUtils.saveIni(ini, profile);
				} catch (IOException e1) {
					LOG.log(Level.SEVERE, "Cannot save ini file", e1);
				}
				shell.close();
			}
		});
		FormData fd_btnNewButton_1 = new FormData();
		fd_btnNewButton_1.width = 100;
		fd_btnNewButton_1.bottom = new FormAttachment(100, -10);
		fd_btnNewButton_1.right = new FormAttachment(cancelButton, -6);
		btnNewButton_1.setLayoutData(fd_btnNewButton_1);
		btnNewButton_1.setText("Save");

		proxyList = new List(shell, SWT.BORDER | SWT.V_SCROLL);
		FormData fd_proxyListScroller = new FormData();
		fd_proxyListScroller.bottom = new FormAttachment(cancelButton, -110, SWT.BOTTOM);
		fd_proxyListScroller.right = new FormAttachment(0, 189);
		fd_proxyListScroller.top = new FormAttachment(lblNewLabel_1, 6);
		fd_proxyListScroller.left = new FormAttachment(0, 10);
		proxyList.setLayoutData(fd_proxyListScroller);
		proxyList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateProxySelection();
			}
		});

		Label lblNewLabel_2 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_2 = new FormData();
		fd_lblNewLabel_2.top = new FormAttachment(label, 31);
		fd_lblNewLabel_2.left = new FormAttachment(proxyList, 6);
		lblNewLabel_2.setLayoutData(fd_lblNewLabel_2);
		lblNewLabel_2.setText("Name");

		Label lblNewLabel_3 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_3 = new FormData();
		fd_lblNewLabel_3.top = new FormAttachment(lblNewLabel_2, 13);
		fd_lblNewLabel_3.left = new FormAttachment(proxyList, 6);
		lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
		lblNewLabel_3.setText("Type");

		proxyType = new Combo(shell, SWT.READ_ONLY);
		proxyType.setItems(new String[] {"Direct", "Proxy with anonymous connection", "Proxy with Basic authentication", "Proxy with NTLM authentication"});
		FormData fd_proxyType = new FormData();
		fd_proxyType.left = new FormAttachment(lblNewLabel_3, 16);
		fd_proxyType.right = new FormAttachment(100, -10);
		fd_proxyType.top = new FormAttachment(0, 104);
		proxyType.setLayoutData(fd_proxyType);

		Label lblNewLabel_4 = new Label(shell, SWT.NONE);
		FormData fd_lblNewLabel_4 = new FormData();
		fd_lblNewLabel_4.left = new FormAttachment(proxyList, 6);
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
		fd_lblNewLabel_5.left = new FormAttachment(proxyList, 6);
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
		fd_lblNewLabel_6.left = new FormAttachment(proxyList, 6);
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
		fd_lblNewLabel_7.left = new FormAttachment(proxyList, 6);
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
		fd_lblNewLabel_8.left = new FormAttachment(proxyList, 6);
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
		fd_lblNewLabel_9.left = new FormAttachment(proxyList, 6);
		fd_lblNewLabel_9.top = new FormAttachment(lblNewLabel_8, 6);
		lblNewLabel_9.setLayoutData(fd_lblNewLabel_9);
		lblNewLabel_9.setText("Exclusions");

		proxyExclusions = new List(shell, SWT.BORDER | SWT.V_SCROLL);
		FormData fd_proxyExclusions = new FormData();
		fd_proxyExclusions.left = new FormAttachment(proxyList, 6);
		fd_proxyExclusions.bottom = new FormAttachment(proxyList, 0, SWT.BOTTOM);
		proxyExclusions.setLayoutData(fd_proxyExclusions);

		Button btnNewButton_2 = new Button(shell, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PromptWindow dialog = new PromptWindow();
				dialog.open("Add proxy", "Proxy", t -> {
					proxyList.add(t);
					ini.add(t);
					proxyList.select(proxyList.getItemCount() - 1);
					updateProxySelection();
				});
			}
		});
		FormData fd_btnNewButton_2 = new FormData();
		fd_btnNewButton_2.top = new FormAttachment(proxyList, 6);
		fd_btnNewButton_2.left = new FormAttachment(0, 10);
		btnNewButton_2.setLayoutData(fd_btnNewButton_2);
		btnNewButton_2.setText("Add Proxy");

		Button btnNewButton_3 = new Button(shell, SWT.NONE);
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyProxyData();
			}
		});
		FormData fd_btnNewButton_3 = new FormData();
		fd_btnNewButton_3.top = new FormAttachment(proxyExclusions, 6);
		fd_btnNewButton_3.left = new FormAttachment(lblNewLabel_2, 0, SWT.LEFT);
		btnNewButton_3.setLayoutData(fd_btnNewButton_3);
		btnNewButton_3.setText("Apply");

		Button btnNewButton_4 = new Button(shell, SWT.NONE);
		btnNewButton_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String exclusion = proxyExclusionToAdd.getText();
				if (!exclusion.trim().isEmpty()) {
					proxyExclusions.add(exclusion);
				}
			}
		});
		FormData fd_btnNewButton_4 = new FormData();
		fd_btnNewButton_4.bottom = new FormAttachment(proxyExclusions, -6);
		fd_btnNewButton_4.left = new FormAttachment(btnNewButton_1, 0, SWT.LEFT);
		fd_btnNewButton_4.width = 100;
		btnNewButton_4.setLayoutData(fd_btnNewButton_4);
		btnNewButton_4.setText("Add");

		Button btnNewButton_5 = new Button(shell, SWT.NONE);
		btnNewButton_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = proxyExclusions.getSelectionIndex();
				if (index >= 0) {
					proxyExclusions.deselectAll();
					proxyExclusions.remove(index);
				}
			}
		});
		fd_proxyExclusions.right = new FormAttachment(btnNewButton_5, -6);
		fd_proxyExclusions.top = new FormAttachment(btnNewButton_5, 0, SWT.TOP);
		FormData fd_btnNewButton_5 = new FormData();
		fd_btnNewButton_5.width = 100;
		fd_btnNewButton_5.top = new FormAttachment(0, 390);
		fd_btnNewButton_5.right = new FormAttachment(serverPortNumber, 0, SWT.RIGHT);
		btnNewButton_5.setLayoutData(fd_btnNewButton_5);
		btnNewButton_5.setText("Delete");

		proxyExclusionToAdd = new Text(shell, SWT.BORDER);
		FormData fd_proxyExclusionToAdd = new FormData();
		fd_proxyExclusionToAdd.right = new FormAttachment(btnNewButton_4, -6);
		fd_proxyExclusionToAdd.top = new FormAttachment(lblNewLabel_9, 6);
		fd_proxyExclusionToAdd.left = new FormAttachment(proxyList, 6);
		proxyExclusionToAdd.setLayoutData(fd_proxyExclusionToAdd);

		proxyName = new Label(shell, SWT.NONE);
		FormData fd_proxyName = new FormData();
		fd_proxyName.width = 200;
		fd_proxyName.bottom = new FormAttachment(lblNewLabel_2, 0, SWT.BOTTOM);
		fd_proxyName.left = new FormAttachment(lblNewLabel_2, 6);
		proxyName.setLayoutData(fd_proxyName);
		proxyName.setText("");

		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (proxyList.getSelectionCount() == 1) {
					int position = proxyList.getSelectionIndex();
					String proxy = proxyList.getItem(position);
					ini.remove(proxy);
					proxyList.remove(position);
					updateProxySelection();
				}
			}
		});
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.left = new FormAttachment(btnNewButton_2, 0, SWT.LEFT);
		fd_btnNewButton.top = new FormAttachment(btnNewButton_2, 5);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("Delete proxy");

	}

	private void update(Section section, Text component, String property) {
		String text = component.getText();
		if (!text.trim().isEmpty()) {
			section.put(property, text);
		} else {
			section.remove(property);
		}
	}

	private void updateProxySelection() {
		if (proxyList.getSelectionCount() == 1) {
			String profile = proxyList.getSelection()[0];
			Section section = ini.get(profile);
			proxyName.setText(section.getName());
			Integer position = PROXY_TYPE_TO_POSITION.get(section.get("type"));
			if (position != null) {
				proxyType.select(position);
			} else {
				proxyType.clearSelection();
			}
			proxyHost.setText(emptyIfNull(section.get("host")));
			proxyPort.setText(emptyIfNull(section.get("port")));
			proxyDomain.setText(emptyIfNull(section.get("domain")));
			proxyUsername.setText(emptyIfNull(section.get("username")));
			proxyPassword.setText(emptyIfNull(section.get("password")));
			java.util.List<String> exclusions = section.getAll("exclude");
			if (exclusions != null) {
				proxyExclusions.setItems(exclusions.toArray(new String[exclusions.size()]));
			} else {
				proxyExclusions.setItems();
			}
		}
	}

	private void applyProxyData() {
		if (proxyList.getSelectionCount() == 1) {
			Section section = ini.get(proxyList.getSelection()[0]);
			int proxyTypeIndex = proxyType.getSelectionIndex();
			if (proxyTypeIndex >= 0) {
				section.put("type", PROXY_TYPES[proxyTypeIndex]);
			} else {
				section.remove("type");
			}
			update(section, proxyHost, "host");
			update(section, proxyPort, "port");
			update(section, proxyDomain, "domain");
			update(section, proxyUsername, "username");
			update(section, proxyPassword, "password");
			section.remove("exclude");
			for (String exclusion : proxyExclusions.getItems()) {
				section.add("exclude", exclusion);
			}
		}
	}
}
