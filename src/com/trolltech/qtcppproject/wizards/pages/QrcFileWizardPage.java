package com.trolltech.qtcppproject.wizards.pages;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class QrcFileWizardPage extends WizardPage {
	private Text containerText;
	private Text fileText;
	private ISelection selection;

	public QrcFileWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Qt Resource File");
		setDescription("Creates a new file that tells Qt about resources that should be linked to the project.");
		this.selection = selection;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, 0);
		label.setText("&Source Folder:");

		this.containerText = new Text(container, 2052);
		GridData gd = new GridData(768);
		this.containerText.setLayoutData(gd);
		this.containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				QrcFileWizardPage.this.dialogChanged();
			}
		});
		Button button = new Button(container, 8);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				QrcFileWizardPage.this.handleBrowse();
			}
		});
		label = new Label(container, 0);
		label.setText("&File name:");

		this.fileText = new Text(container, 2052);
		gd = new GridData(768);
		this.fileText.setLayoutData(gd);
		this.fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				QrcFileWizardPage.this.dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	private void initialize() {
		if ((this.selection != null) && (!this.selection.isEmpty())
				&& ((this.selection instanceof IStructuredSelection))) {
			IStructuredSelection ssel = (IStructuredSelection) this.selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if ((!(obj instanceof IResource)) && ((obj instanceof IAdaptable)))
				obj = ((IAdaptable) obj).getAdapter(IResource.class);
			if ((obj instanceof IResource)) {
				IContainer container;
				if ((obj instanceof IContainer))
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				this.containerText.setText(container.getFullPath()
						.makeRelative().toString());
			}
		}
		this.fileText.setFocus();
	}

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select source folder");

		if (dialog.open() == 0) {
			Object[] result = dialog.getResult();
			if (result.length == 1)
				this.containerText.setText(((Path) result[0]).makeRelative()
						.toString());
		}
	}

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));

		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("Source folder must be specified");
			return;
		}
		if ((container == null) || ((container.getType() & 0x6) == 0)) {
			updateStatus("Source folder must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		if (fileName.indexOf(' ') >= 0) {
			updateStatus("File name must not contain spaces");
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		String ext = "";
		if (dotLoc != -1) {
			ext = fileName.substring(dotLoc + 1);
		}
		if (!ext.equalsIgnoreCase("qrc")) {
			updateStatus("File extension must be \"qrc\"");
			return;
		}
		if (ResourcesPlugin.getWorkspace().getRoot()
				.findMember(getContainerName() + "/" + fileName) != null) {
			updateStatus("File already exists");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return this.containerText.getText();
	}

	public String getFileName() {
		return this.fileText.getText();
	}
}