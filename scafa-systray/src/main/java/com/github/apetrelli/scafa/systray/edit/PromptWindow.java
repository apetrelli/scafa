package com.github.apetrelli.scafa.systray.edit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PromptWindow {

	private TextCallback callback;

	protected Shell shell;
	private Text text;
	private Button btnNewButton;
	private Button btnNewButton_1;
	private Label label;

	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		open("Title", "Label", null);
	}

	public void open(String title, String text, TextCallback callback) {
		this.callback = callback;
		Display display = Display.getDefault();
		createContents();
		shell.setText(title);
		shell.open();
		shell.layout();
		label.setText(text);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 106);
		shell.setLayout(new FormLayout());

		label = new Label(shell, SWT.NONE);
		FormData fd_label = new FormData();
		fd_label.width = 100;
		fd_label.top = new FormAttachment(0, 10);
		fd_label.left = new FormAttachment(0, 10);
		label.setLayoutData(fd_label);
		label.setText("New Label");

		text = new Text(shell, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.left = new FormAttachment(label, 6);
		fd_text.right = new FormAttachment(95);
		fd_text.top = new FormAttachment(0, 10);
		text.setLayoutData(fd_text);

		btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.width = 100;
		fd_btnNewButton.top = new FormAttachment(text, 6);
		fd_btnNewButton.right = new FormAttachment(text, 0, SWT.RIGHT);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("Cancel");

		btnNewButton_1 = new Button(shell, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String writtenText = text.getText();
				shell.close();
				callback.writtenText(writtenText);
			}
		});
		FormData fd_btnNewButton_1 = new FormData();
		fd_btnNewButton_1.width = 100;
		fd_btnNewButton_1.top = new FormAttachment(text, 6);
		fd_btnNewButton_1.right = new FormAttachment(btnNewButton, -6);
		btnNewButton_1.setLayoutData(fd_btnNewButton_1);
		btnNewButton_1.setText("OK");

	}
}
