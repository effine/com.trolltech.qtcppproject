package com.trolltech.qtcppproject.preferences;

import com.trolltech.qtcppproject.wizards.QtVersionWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;

class QtVersionListener extends SelectionAdapter {
	public static final int ADD = 1;
	public static final int EDIT = 2;
	public static final int REMOVE = 3;
	public static final int DEFAULT = 4;
	public static final int SELECTION = 5;
	private QtPreferencePage m_prefpage;
	private int m_control;

	public QtVersionListener(QtPreferencePage prefpage, int control) {
		this.m_control = control;
		this.m_prefpage = prefpage;
	}

	public void widgetSelected(SelectionEvent e) {
		if (m_control == 1) {
			QtVersionWizard versionWizard = new QtVersionWizard();
			WizardDialog versionDialog = new WizardDialog(
					m_prefpage.getShell(), versionWizard);
			versionDialog.create();

			versionDialog.setTitle("Add new Qt version");
			if (versionDialog.open() == 0) {
				m_prefpage.addItem(versionWizard.getVersionName(),
						versionWizard.getBinPath(),
						versionWizard.getIncludePath());
			}

		} else if (m_control == 2) {
			String[] current = m_prefpage.getCurrentItem();

			QtVersionWizard versionWizard = new QtVersionWizard();
			WizardDialog versionDialog = new WizardDialog(
					m_prefpage.getShell(), versionWizard);
			versionDialog.create();

			versionWizard.setVersionName(current[0]);
			versionWizard.setBinPath(current[1]);
			versionWizard.setIncludePath(current[2]);

			versionDialog.setTitle("Edit Qt Version");
			if (versionDialog.open() == 0) {
				m_prefpage.updateItem(versionWizard.getVersionName(),
						versionWizard.getBinPath(),
						versionWizard.getIncludePath());
			}

		} else if (m_control == 3) {
			m_prefpage.removeItem();
		} else if (m_control == 4) {
			m_prefpage.setCurrentDefault();
		} else if (m_control == 5) {
			Table table = (Table) e.widget;
			m_prefpage.enableButtons(table.getSelectionCount() > 0);
		}
	}
}