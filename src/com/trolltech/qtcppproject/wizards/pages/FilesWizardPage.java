package com.trolltech.qtcppproject.wizards.pages;

import java.util.Locale;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FilesWizardPage extends WizardPage {
	private boolean filechanged = false;
	private Text classLE;
	private Text cppLE;
	private Text hLE;
	private Text uiLE;
	private Combo uiType;

	public FilesWizardPage(String pageName) {
		super(pageName);
		setDescription("Setup template files for the project.");
		setTitle("Files");
	}

	public static String toValidClassName(String name) {
		String validName = "";

		char[] underscoreCandidates = { ' ', '-' };
		for (int i = 0; i < underscoreCandidates.length; i++) {
			name = name.replace(underscoreCandidates[i], '_');
		}
		for (int i = 0; i < name.length(); i++) {
			char character = name.charAt(i);
			boolean isAlpha = ((character >= 'a') && (character <= 'z'))
					|| ((character >= 'A') && (character <= 'Z'));

			boolean isDigit = (character >= '0') && (character <= '9');
			boolean isUnderscore = character == '_';
			boolean isCharacterValid = (isAlpha)
					|| ((isDigit) && (validName.length() > 0))
					|| (isUnderscore);
			validName = validName + (isCharacterValid ? character : '_');
		}

		return validName.length() == 0 ? "new_qt_project" : validName;
	}

	public void setClassName(String name) {
		if (this.classLE != null) {
			String validName = toValidClassName(name);
			classLE.setText(validName);
			updateLEs(validName);
		}
	}

	public String getClassName() {
		return classLE.getText();
	}

	public String getSourceFileName() {
		return this.cppLE.getText();
	}

	public String getHeaderFileName() {
		return this.hLE.getText();
	}

	public String getUIFileName() {
		return this.uiLE.getText();
	}

	public String getUiClassName() {
		return this.uiType.getText();
	}

	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, 0);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		mainComposite.setLayout(layout);

		Label cl = new Label(mainComposite, 8);
		cl.setText("Class Name:");

		this.classLE = new Text(mainComposite, 2052);
		this.classLE.setLayoutData(new GridData(768));

		Label cppl = new Label(mainComposite, 8);
		cppl.setText("Source Filename:");

		this.cppLE = new Text(mainComposite, 2052);
		this.cppLE.setLayoutData(new GridData(768));

		this.cppLE.addKeyListener(new FileTextListener());

		Label hl = new Label(mainComposite, 8);
		hl.setText("Header Filename:");

		this.hLE = new Text(mainComposite, 2052);
		this.hLE.setLayoutData(new GridData(768));

		this.hLE.addKeyListener(new FileTextListener());

		Label uil = new Label(mainComposite, 8);
		uil.setText("UI Filename:");

		this.uiLE = new Text(mainComposite, 2052);
		this.uiLE.setLayoutData(new GridData(768));

		this.uiLE.addKeyListener(new FileTextListener());

		Label uiTypeLable = new Label(mainComposite, 8);
		uiTypeLable.setText("UI Type:");

		this.uiType = new Combo(mainComposite, 8);
		this.uiType.setLayoutData(new GridData(768));

		this.uiType
				.setItems(new String[] { "QWidget", "QMainWindow", "QDialog" });
		this.uiType.select(0);

		this.classLE.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				FilesWizardPage.this.updateLEs(FilesWizardPage.this.classLE
						.getText());
				FilesWizardPage.this.checkValidPage();
			}
		});
		setControl(mainComposite);
		setPageComplete(true);
	}

	private void updateLEs(String text) {
		if (!this.filechanged) {
			this.cppLE.setText(text.toLowerCase(Locale.US) + ".cpp");
			this.hLE.setText(text.toLowerCase(Locale.US) + ".h");
			this.uiLE.setText(text.toLowerCase(Locale.US) + ".ui");
		}
	}

	private void checkValidPage() {
		Text[] text = { this.classLE, this.cppLE, this.hLE, this.uiLE };
		for (int i = 0; i < text.length; i++) {
			String string = text[i].getText().trim();
			if ((string.length() > 0) && (string.indexOf(" \\/") > -1)) {
				setPageComplete(false);
				return;
			}
		}
		setPageComplete(true);
	}

	class FileTextListener implements KeyListener {
		FileTextListener() {
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			FilesWizardPage.this.filechanged = true;
			FilesWizardPage.this.checkValidPage();
		}
	}

	class TextListener implements KeyListener {
		TextListener() {
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			FilesWizardPage.this.checkValidPage();
		}
	}
}