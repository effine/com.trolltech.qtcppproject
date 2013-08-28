package com.trolltech.qtcppproject.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class QtModulesWizardPage extends WizardPage {
	private static int NUM_MODULES = 11;
	private Button[] buttons = new Button[NUM_MODULES];
	private int[] buttonConstants = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512,
			4096 };

	private String[] proEntries = { "core ", "gui ", "sql ", "xml ",
			"xmlpatterns ", "network ", "svg ", "opengl ", "webkit ",
			"script ", "qt3support " };

	private String[] visibleStrings = { "Core", "Gui", "SQL", "XML",
			"XMLPatterns", "Network", "SVG", "OpenGL", "WebKit", "Script",
			"Qt3 Support" };
	private boolean hasInitialized;
	private int selectedModules;
	private int requiredModules;

	public QtModulesWizardPage(String pageName) {
		super(pageName);
		setDescription("Select the Qt modules for the project.");
		setTitle("Qt Modules");
		selectedModules = 0;
		requiredModules = 0;
		hasInitialized = false;
	}

	public void setSelectedModules(int mods) {
		selectedModules = mods;
		refreshSelectedModules();
	}

	public void setRequiredModules(int mods) {
		requiredModules = mods;
		refreshSelectedModules();
	}

	public int getSelectedModules() {
		selectedModules = 0;
		for (int i = 0; i < NUM_MODULES; i++)
			if (buttons[i].getSelection())
				selectedModules |= buttonConstants[i]; // 使用按位 与 运算的作用是什么啊？
		return selectedModules;
	}

	public String getModules() {
		String modules = "";
		for (int i = 0; i < NUM_MODULES; i++)
			if (buttons[i].getSelection())
				modules = modules + proEntries[i];
		return modules;
	}

	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, 0);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		mainComposite.setLayout(layout);

		Group moduleGroup = new Group(mainComposite, 8);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = 4;
		gridData.verticalAlignment = 1;
		moduleGroup.setLayoutData(gridData);
		moduleGroup.setLayout(new RowLayout(512));
		moduleGroup.setText("Qt Modules");

		for (int i = 0; i < NUM_MODULES; i++) {
			this.buttons[i] = new Button(moduleGroup, 32);
			this.buttons[i].setText(this.visibleStrings[i]);
			this.buttons[i].setSelection(false);
		}

		this.hasInitialized = true;
		refreshSelectedModules();
		enableModules(this.requiredModules, false);

		setControl(mainComposite);
		setPageComplete(true);
	}

	private void refreshSelectedModules() {
		if (!this.hasInitialized)
			return;
		int modules = this.selectedModules | this.requiredModules;
		for (int i = 0; i < NUM_MODULES; i++)
			if ((modules & this.buttonConstants[i]) != 0)
				this.buttons[i].setSelection(true);
	}

	private void enableModules(int modules, boolean enabled) {
		if (!this.hasInitialized)
			return;
		for (int i = 0; i < NUM_MODULES; i++)
			if ((modules & this.buttonConstants[i]) != 0)
				this.buttons[i].setEnabled(enabled);
	}
}