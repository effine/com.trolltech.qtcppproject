package com.trolltech.qtcppproject.wizards;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;

import com.trolltech.qtcppcommon.QtWizardUtils;
import com.trolltech.qtcppproject.wizards.pages.FilesWizardPage;
import com.trolltech.qtcppproject.wizards.pages.QtModulesWizardPage;
import com.trolltech.qtcppproject.wizards.pages.QtTargetWizardPage;

public class QtGuiProjectWizard extends QtProjectWizard {
	private FilesWizardPage filesPage;
	private QtModulesWizardPage modulesPage;
	private QtTargetWizardPage targetPage;

	public void addPages() {
		String title = "Qt Gui Project";

		// main page
		mainPage.setTitle(title);
		mainPage.setDescription("Create a new Qt Gui Application Project.");
		addPage(mainPage);

		// filePage
		filesPage = new FilesWizardPage(
				"com.trolltech.qtcppproject.FilesWizardPage");
		filesPage.setTitle(title);
		filesPage.setDescription("Setup the class and file names.");
		addPage(filesPage);

		// modelPage
		modulesPage = new QtModulesWizardPage(
				"com.trolltech.qtcppproject.QtModulesPage");
		modulesPage.setSelectedModules(3);
		modulesPage.setRequiredModules(3);
		modulesPage.setTitle(title);
		modulesPage.setDescription("Select the Qt modules.");
		addPage(modulesPage);

		/* target platform wizardpage */
		targetPage = new QtTargetWizardPage(
				"com.trolltech.qtcppproject.QtTargetPage");
		targetPage.setTitle(title);
		targetPage.setDescription("Select the Qt target .");
		addPage(targetPage);

	}

	public IWizardPage getNextPage(IWizardPage page) {
		if ((mainPage.isPageComplete())
				&& (getContainer().getCurrentPage() == mainPage)) {
			filesPage.setClassName(mainPage.getProjectHandle().getName());
		}
		return super.getNextPage(page);
	}

	public void projectCreated(IProject project, IProgressMonitor monitor) {
		addFiles(project, monitor);
	}

	private static String toValidProjectName(String projectName) {
		return FilesWizardPage.toValidClassName(projectName);
	}

	private void addFiles(IProject pro, IProgressMonitor monitor) {
		HashMap replaceMap = new HashMap();

		String hdr = filesPage.getHeaderFileName();
		if (hdr.endsWith(".h"))
			hdr = hdr.substring(0, hdr.length() - 2);
		String preDef = hdr.toUpperCase(Locale.US) + "_H";

		String uiHdr = filesPage.getUIFileName();
		if (uiHdr.endsWith(".ui"))
			uiHdr = uiHdr.substring(0, uiHdr.length() - 3);
		uiHdr = "ui_" + uiHdr + ".h";

		String headerFile = filesPage.getHeaderFileName();
		String srcFile = filesPage.getSourceFileName();
		String uiFile = filesPage.getUIFileName();

		String targetPlatName = targetPage.getChildTarget();
		replaceMap.put("%TargetPlatform%", targetPlatName);

		replaceMap.put("%MODULES%", modulesPage.getModules());

		replaceMap.put("%INCLUDE%", headerFile);
		replaceMap.put("%CLASS%", filesPage.getClassName());

		replaceMap.put("%PRE_DEF%", preDef);
		replaceMap.put("%UI_HDR%", uiHdr);

		String uiClass = filesPage.getUiClassName();
		replaceMap.put("%UI_CLASS%", uiClass);

		String projectName = toValidProjectName(pro.getName());
		replaceMap.put("%PROJECT%", projectName);

		replaceMap.put("%HEADER_FILE%", headerFile);
		replaceMap.put("%SOURCE_FILE%", srcFile);
		replaceMap.put("%UI_FILE%", uiFile);

		InputStream src = getClass().getResourceAsStream(
				"/com/trolltech/qtcppproject/wizards/templates/QtGui/gui.pro");
		File dest = new File(pro.getLocation().toOSString() + "/" + projectName
				+ ".pro");
		QtWizardUtils.addTemplateFile(src, dest, replaceMap);

		src = getClass().getResourceAsStream(
				"/com/trolltech/qtcppproject/wizards/templates/QtGui/main.cpp");
		dest = new File(pro.getLocation().toOSString() + "/main.cpp");
		QtWizardUtils.addTemplateFile(src, dest, replaceMap);

		src = getClass().getResourceAsStream(
				"/com/trolltech/qtcppproject/wizards/templates/QtGui/gui.cpp");
		dest = new File(pro.getLocation().toOSString() + "/" + srcFile);
		QtWizardUtils.addTemplateFile(src, dest, replaceMap);

		src = getClass().getResourceAsStream(
				"/com/trolltech/qtcppproject/wizards/templates/QtGui/gui.h");
		dest = new File(pro.getLocation().toOSString() + "/" + headerFile);
		QtWizardUtils.addTemplateFile(src, dest, replaceMap);

		src = getClass().getResourceAsStream(
				"/com/trolltech/qtcppproject/wizards/templates/QtGui/"
						+ uiClass + ".ui");
		dest = new File(pro.getLocation().toOSString() + "/" + uiFile);
		QtWizardUtils.addTemplateFile(src, dest, replaceMap);
	}
}