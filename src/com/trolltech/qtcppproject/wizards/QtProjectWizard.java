package com.trolltech.qtcppproject.wizards;

import com.trolltech.qtcppproject.QtProjectCreator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public abstract class QtProjectWizard extends BasicNewProjectResourceWizard
		implements INewWizard, QtProjectCreatorListener {
	protected WizardNewProjectCreationPage mainPage;

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setWindowTitle("New Qt Gui Application Project");
		mainPage = new WizardNewProjectCreationPage(
				"com.trolltech.qtcppproject.MainWizardPage");
	}

	public boolean performFinish() {
		if (mainPage.isPageComplete()) {
			createNewProject();
			updatePerspective();
			selectAndReveal(mainPage.getProjectHandle());
			return true;
		}
		return false;
	}

	public void createNewProject() {
		QtProjectCreator procreator = new QtProjectCreator(
				mainPage.getProjectHandle(), mainPage.getLocationPath());
		procreator.addQtProjectCreatorListener(this);
		procreator.create(getContainer());
	}
}