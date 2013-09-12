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

	/* check project nmae */
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
		if (classLE != null) {
			String validName = toValidClassName(name);
			classLE.setText(validName);
			updateLEs(validName);
		}
	}

	public String getClassName() {
		return classLE.getText();
	}

	public String getSourceFileName() {
		return cppLE.getText();
	}

	public String getHeaderFileName() {
		return hLE.getText();
	}

	public String getUIFileName() {
		return uiLE.getText();
	}

	public String getUiClassName() {
		return uiType.getText();
	}

	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, 0);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		mainComposite.setLayout(layout);

		Label cl = new Label(mainComposite, 8);
		cl.setText("Class Name:");

		classLE = new Text(mainComposite, 2052);
		classLE.setLayoutData(new GridData(768));

		Label cppl = new Label(mainComposite, 8);
		cppl.setText("Source Filename:");

		cppLE = new Text(mainComposite, 2052);
		cppLE.setLayoutData(new GridData(768));

		cppLE.addKeyListener(new FileTextListener());

		Label hl = new Label(mainComposite, 8);
		hl.setText("Header Filename:");

		hLE = new Text(mainComposite, 2052);
		hLE.setLayoutData(new GridData(768));

		hLE.addKeyListener(new FileTextListener());

		Label uil = new Label(mainComposite, 8);
		uil.setText("UI Filename:");

		uiLE = new Text(mainComposite, 2052);
		uiLE.setLayoutData(new GridData(768));

		uiLE.addKeyListener(new FileTextListener());

		Label uiTypeLable = new Label(mainComposite, 8);
		uiTypeLable.setText("UI Type:");

		uiType = new Combo(mainComposite, 8);
		uiType.setLayoutData(new GridData(768));

		uiType.setItems(new String[] { "QWidget", "QMainWindow", "QDialog" });
		uiType.select(0);

		classLE.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				updateLEs(classLE.getText());
				checkValidPage();
			}
		});
		setControl(mainComposite);
		setPageComplete(true);
	}

	private void updateLEs(String text) {
		if (!filechanged) {
			cppLE.setText(text.toLowerCase(Locale.US) + ".cpp");
			hLE.setText(text.toLowerCase(Locale.US) + ".h");
			uiLE.setText(text.toLowerCase(Locale.US) + ".ui");
		}
	}

	private void checkValidPage() {
		Text[] text = { classLE, cppLE, hLE, uiLE };
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
			filechanged = true;
			checkValidPage();
		}
	}

	class TextListener implements KeyListener {
		TextListener() {
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			checkValidPage();
		}
	}
}