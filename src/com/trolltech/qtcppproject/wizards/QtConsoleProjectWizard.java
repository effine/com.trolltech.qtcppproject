package com.trolltech.qtcppproject.wizards;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.trolltech.qtcppcommon.QtWizardUtils;

import com.trolltech.qtcppproject.wizards.pages.QtModulesWizardPage;

public class QtConsoleProjectWizard extends QtProjectWizard {
	private QtModulesWizardPage modulesPage;

	public void addPages() {
		String title = "Qt Console Project";
		this.mainPage.setTitle(title);
		this.mainPage
				.setDescription("Create a new Qt Console Application Project.");
		addPage(this.mainPage);

		this.modulesPage = new QtModulesWizardPage(
				"com.trolltech.qtcppproject.QtModulesPage");

		this.modulesPage.setSelectedModules(1);
		this.modulesPage.setRequiredModules(1);

		this.modulesPage.setTitle(title);
		this.modulesPage.setDescription("Select the Qt modules.");
		addPage(this.modulesPage);
	}

	public void projectCreated(IProject project, IProgressMonitor monitor) {
		addFiles(project, monitor);
	}

	private void addFiles(IProject project, IProgressMonitor monitor) {
		HashMap replaceMap = new HashMap();

		String projectName = project.getName();
		replaceMap.put("%PROJECT%", projectName);
		replaceMap.put("%MODULES%", this.modulesPage.getModules());

		InputStream src = getClass()
				.getResourceAsStream(
						"/com/trolltech/qtcppproject/wizards/templates/QtConsole/console.pro");
		File dest = new File(project.getLocation().toOSString() + "/"
				+ projectName + ".pro");
		QtWizardUtils.addTemplateFile(src, dest, replaceMap);

		src = getClass()
				.getResourceAsStream(
						"/com/trolltech/qtcppproject/wizards/templates/QtConsole/main.cpp");
		dest = new File(project.getLocation().toOSString() + "/main.cpp");
		QtWizardUtils.addTemplateFile(src, dest, null);
	}
}