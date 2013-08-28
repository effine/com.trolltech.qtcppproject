package com.trolltech.qtcppproject.perspective;

import org.eclipse.cdt.internal.ui.wizards.CWizardRegistry;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class QtCppPerspectiveFactory implements IPerspectiveFactory {
	private static final String perspectiveId = "com.trolltech.qtcppproject.QtCppPerspective";

	public static String getPerspectiveId() {
		return "com.trolltech.qtcppproject.QtCppPerspective";
	}

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout folder1 = layout.createFolder("topLeft", 1, 0.25F,
				editorArea);
		folder1.addView("org.eclipse.ui.navigator.ProjectExplorer");
		folder1.addPlaceholder("org.eclipse.ui.views.ResourceNavigator");
		folder1.addPlaceholder("org.eclipse.ui.views.BookmarkView");
		folder1.addView("com.trolltech.qtcppdesigner.views.widgetboxview");

		IFolderLayout folder2 = layout.createFolder("bottom", 4, 0.75F,
				editorArea);
		folder2.addView("org.eclipse.ui.views.ProblemView");
		folder2.addView("org.eclipse.ui.console.ConsoleView");
		folder2.addView("org.eclipse.ui.views.TaskList");

		folder2.addView("org.eclipse.cdt.ui.includeBrowser");
		folder2.addView("org.eclipse.cdt.ui.callHierarchy");
		folder2.addView("org.eclipse.cdt.ui.typeHierarchy");
		folder2.addView("org.eclipse.cdt.debug.ui.executablesView");

		folder2.addView("com.trolltech.qtcppdesigner.views.actioneditorview");
		folder2.addView("com.trolltech.qtcppdesigner.views.signalsloteditorview");

		IFolderLayout folder3 = layout.createFolder("topRight", 2, 0.75F,
				editorArea);
		folder3.addView("org.eclipse.ui.views.ContentOutline");
		folder3.addView("com.trolltech.qtcppdesigner.views.propertyeditorview");
		folder3.addView("com.trolltech.qtcppdesigner.views.objectinspectorview");

		layout.addActionSet("org.eclipse.cdt.ui.SearchActionSet");
		layout.addActionSet("org.eclipse.cdt.ui.CElementCreationActionSet");
		layout.addActionSet("org.eclipse.ui.NavigateActionSet");

		layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView");

		layout.addShowViewShortcut("org.eclipse.search.ui.views.SearchView");

		layout.addShowViewShortcut("org.eclipse.ui.views.ContentOutline");
		layout.addShowViewShortcut("org.eclipse.ui.views.ProblemView");
		layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");
		layout.addShowViewShortcut("org.eclipse.ui.views.ResourceNavigator");
		layout.addShowViewShortcut("org.eclipse.ui.views.PropertySheet");

		layout.addShowInPart("org.eclipse.ui.navigator.ProjectExplorer");
		layout.addShowInPart("org.eclipse.ui.views.ResourceNavigator");

		addCWizardShortcuts(layout);
	}

	private void addCWizardShortcuts(IPageLayout layout) {
		String[] wizIDs = CWizardRegistry.getProjectWizardIDs();
		for (int i = 0; i < wizIDs.length; i++) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}

		wizIDs = CWizardRegistry.getFolderWizardIDs();
		for (int i = 0; i < wizIDs.length; i++) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}

		wizIDs = CWizardRegistry.getFileWizardIDs();
		for (int i = 0; i < wizIDs.length; i++) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}

		wizIDs = CWizardRegistry.getTypeWizardIDs();
		for (int i = 0; i < wizIDs.length; i++)
			layout.addNewWizardShortcut(wizIDs[i]);
	}
}