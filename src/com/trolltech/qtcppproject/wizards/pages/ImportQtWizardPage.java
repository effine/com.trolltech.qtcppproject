package com.trolltech.qtcppproject.wizards.pages;

import com.trolltech.qtcppproject.utils.QtUtils;
import java.io.File;
import java.io.IOException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ImportQtWizardPage extends WizardPage implements
		SelectionListener, ModifyListener {
	private Table list;
	private Text profilelocation;
	private IPath pathToProFile = null;

	public ImportQtWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName);
		setDescription("Import a Qt project from the local file system into the workspace");
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite workArea = new Composite(parent, 0);
		setControl(workArea);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;

		workArea.setLayout(layout);
		workArea.setLayoutData(new GridData(1808));

		Label label = new Label(workArea, 0);
		label.setText("Select .pro file: ");

		this.profilelocation = new Text(workArea, 2048);
		this.profilelocation.setLayoutData(new GridData(768));
		this.profilelocation.addModifyListener(this);

		Button browse = new Button(workArea, 0);
		browse.setText("Browse...");
		browse.addSelectionListener(this);

		this.list = new Table(workArea, 2080);
		this.list.addSelectionListener(this);
		GridData data = new GridData(1808);
		data.horizontalSpan = 3;
		this.list.setLayoutData(data);
	}

	public String getProjectName() {
		return QtUtils.getFileName(this.pathToProFile, true);
	}

	public IPath getProFileLocation() {
		return QtUtils.removeFileName(this.pathToProFile);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget == this.list) {
			updatePage();
		} else {
			FileDialog filedlg = new FileDialog(getShell());

			String[] filterext = { "*.pro" };
			String[] filtername = { "Qt Project File" };
			filedlg.setFilterExtensions(filterext);
			filedlg.setFilterNames(filtername);
			filedlg.setText("Select Qt Project File");
			String filename = filedlg.open();
			if (filename != null)
				this.profilelocation.setText(filename);
		}
	}

	private void updatePage() {
		setErrorMessage(null);
		boolean enabled = false;
		for (int i = 0; i < this.list.getItemCount(); i++) {
			if (this.list.getItem(i).getChecked()) {
				enabled = true;
				break;
			}
		}
		if (!checkProFileLocation())
			enabled = false;
		setPageComplete(enabled);
	}

	private boolean checkProFileLocation() {
		try {
			String rootPath = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toFile().getCanonicalPath();
			String proPath = getProFilePath().toFile().getCanonicalPath();
			if (proPath.startsWith(rootPath)) {
				setErrorMessage("Cannot import pro-file from workspace location");
				return false;
			}
		} catch (IOException e) {
			setErrorMessage("Cannot resolve path");
			return false;
		}
		return true;
	}

	public void modifyText(ModifyEvent e) {
		this.list.removeAll();
		if (isValidProFile()) {
			TableItem item = new TableItem(this.list, 0);
			item.setText(getProjectName());
			item.setChecked(true);
		}
		updatePage();
	}

	private IPath getProFilePath() {
		return new Path(this.profilelocation.getText().trim());
	}

	private boolean isValidProFile() {
		this.pathToProFile = getProFilePath();
		if (!this.pathToProFile.getFileExtension().equals("pro")) {
			return false;
		}
		File file = this.pathToProFile.toFile();
		if (!file.exists()) {
			return false;
		}
		return true;
	}
}