package com.trolltech.qtcppproject.wizards;

import com.trolltech.qtcppproject.QtProjectCreator;
import com.trolltech.qtcppproject.wizards.pages.ImportQtWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class ImportQtWizard extends Wizard implements IImportWizard,
		IExecutableExtension {
	ImportQtWizardPage mainPage;
	IWorkbench workbench;
	IConfigurationElement configuration;

	public boolean performFinish() {
		IProject hProject = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(mainPage.getProjectName());
		QtProjectCreator procreator = new QtProjectCreator(hProject,
				this.mainPage.getProFileLocation());

		procreator.create(getContainer());

		BasicNewProjectResourceWizard.updatePerspective(configuration);

		BasicNewResourceWizard.selectAndReveal(hProject,
				workbench.getActiveWorkbenchWindow());

		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		setWindowTitle("Qt Import Wizard");
		setNeedsProgressMonitor(true);
		this.mainPage = new ImportQtWizardPage("Import Qt Project");
	}

	public void addPages() {
		super.addPages();
		addPage(mainPage);
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		configuration = config;
	}
}