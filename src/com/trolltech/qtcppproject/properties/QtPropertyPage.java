package com.trolltech.qtcppproject.properties;

import com.trolltech.qtcppproject.QtProject;
import com.trolltech.qtcppproject.preferences.QtPreferencePage;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class QtPropertyPage extends PropertyPage {
	private Combo versioncombo;
	private Button proFileListenerCheck;
	private int oldVersionIndex;

	private void addControls(Composite parent) {
		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = 4;
		data.horizontalAlignment = 4;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);

		Label label = new Label(composite, 0);
		label.setText("Use Qt Version: ");
		this.versioncombo = new Combo(composite, 8);

		data = new GridData();
		data.horizontalAlignment = 4;
		data.grabExcessHorizontalSpace = true;
		this.versioncombo.setLayoutData(data);

		this.proFileListenerCheck = new Button(composite, 32);
		this.proFileListenerCheck.setText("Run qmake when .pro file changes");
		this.proFileListenerCheck
				.setToolTipText("When enabled, qmake will be invoked whenever the .pro file or any of its includes are changed.");
		data = new GridData();
		data.horizontalAlignment = 4;
		data.grabExcessHorizontalSpace = true;
		this.proFileListenerCheck.setLayoutData(data);
	}

	private void loadPersistentSettings() {
		this.versioncombo.add("<Default>");

		String[] versions = QtPreferencePage.getQtVersions();

		QtProject qtProject = new QtProject((IProject) getElement());
		String currentVersion = qtProject.getQtVersion();

		this.versioncombo.select(0);
		if (versions != null) {
			for (int i = 0; i < versions.length; i++) {
				this.versioncombo.add(versions[i]);
				if (versions[i].equals(currentVersion)) {
					this.versioncombo.select(i + 1);
				}
			}
		}
		setOldVersionToSelectedVersion();

		this.proFileListenerCheck.setSelection(qtProject
				.runQMakeWhenProFileChanges());
	}

	private boolean savePersistentSettings() {
		QtProject qtProject = new QtProject((IProject) getElement());
		qtProject.setRunQMakeWhenProFileChanges(this.proFileListenerCheck
				.getSelection());

		return qtProject.setQtVersion(this.versioncombo
				.getItem(this.versioncombo.getSelectionIndex()));
	}

	private boolean versionHasChanged() {
		return this.versioncombo.getSelectionIndex() != this.oldVersionIndex;
	}

	private void setOldVersionToSelectedVersion() {
		this.oldVersionIndex = this.versioncombo.getSelectionIndex();
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(4);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addControls(composite);

		loadPersistentSettings();

		return composite;
	}

	protected void performDefaults() {
		this.versioncombo.select(0);
	}

	public boolean performOk() {
		if (savePersistentSettings()) {
			if (versionHasChanged()) {
				if (!requestFullBuild())
					return false;
				setOldVersionToSelectedVersion();
			}
		} else
			return false;

		return true;
	}

	private boolean requestFullBuild() {
		boolean accepted = false;
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog dialog = new MessageDialog(
				shell,
				"Qt Version Changed",
				null,
				"The project's Qt version has changed. A rebuild of the project is required for changes to take effect. Do a full rebuild now?",
				3, new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL }, 2);

		switch (dialog.open()) {
		case 2:
			accepted = false;
			break;
		case 0:
			new QtProject((IProject) getElement()).scheduleRebuild();
			accepted = true;
			break;
		case 1:
			accepted = true;
		}

		return accepted;
	}
}