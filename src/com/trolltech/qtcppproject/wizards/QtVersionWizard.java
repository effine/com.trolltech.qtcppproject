package com.trolltech.qtcppproject.wizards;

import com.trolltech.qtcppproject.wizards.pages.QtVersionWizardPage;
import org.eclipse.jface.wizard.Wizard;

public class QtVersionWizard extends Wizard {
	String versionName;
	String binPath;
	String includePath;
	QtVersionWizardPage wizardPage;

	public boolean performFinish() {
		this.versionName = this.wizardPage.getVersionName();
		this.binPath = this.wizardPage.getBinPath();
		this.includePath = this.wizardPage.getIncludePath();
		return true;
	}

	public void addPages() {
		this.wizardPage = new QtVersionWizardPage("Qt Version");
		addPage(this.wizardPage);
	}

	public String getVersionName() {
		return this.versionName;
	}

	public void setVersionName(String versionName) {
		this.wizardPage.setVersionName(versionName);
	}

	public String getBinPath() {
		return this.binPath;
	}

	public void setBinPath(String binPath) {
		this.wizardPage.setBinPath(binPath);
	}

	public String getIncludePath() {
		return this.includePath;
	}

	public void setIncludePath(String includePath) {
		this.wizardPage.setIncludePath(includePath);
	}
}