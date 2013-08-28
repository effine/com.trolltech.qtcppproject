package com.trolltech.qtcppproject.wizards.pages;

import com.trolltech.qtcppcommon.QtWizardUtils;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import org.eclipse.cdt.core.CConventions;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class UiFileWizardPage extends WizardPage implements KeyListener {
	private boolean uichanged = false;
	private Group uigroup;
	private Text containerText;
	private Text classText;
	private Text uiFileName;
	private Text headerFileName;
	private Text sourceFileName;
	private Combo uiType;
	private Label uiTypeLable;
	private Button filesLowerCase;
	private ISelection selection;

	public UiFileWizardPage(ISelection selection) {
		super("");
		this.selection = selection;
		setTitle("User Interface Class");
		setDescription("Creates a user interface connected to a class.");
	}

	private String getUiFileName() {
		return this.uiFileName.getText();
	}

	private String getUiFileNameBase() {
		return QtUtils.getFileName(new Path(getUiFileName()), true);
	}

	private String getHeaderFileName() {
		return this.headerFileName.getText();
	}

	private String getHeaderFileNameBase() {
		return QtUtils.getFileName(new Path(getHeaderFileName()), true);
	}

	private String getRelativeHeaderFileName() {
		return QtUtils.makeRelative(
				new Path(getContainerName() + "/" + getHeaderFileName()),
				new Path(getContainerName() + "/" + getSourceFileName()))
				.toString();
	}

	private String getSourceFileName() {
		return this.sourceFileName.getText();
	}

	private String[] getFileNames() {
		return new String[] { getUiFileName(), getHeaderFileName(),
				getSourceFileName() };
	}

	private String[] getFileLocations() {
		return new String[] { getContainerName(), getContainerName(),
				getContainerName() };
	}

	private String getUISuperClass() {
		return this.uiType.getText();
	}

	public void createFiles(IProgressMonitor monitor) throws CoreException {
		String baseClass = getUISuperClass();
		String className = getClassName();
		HashMap replaceMap = new HashMap();
		replaceMap.put("%PRE_DEF%",
				getHeaderFileNameBase().toUpperCase(Locale.US)
						.replace('.', '_') + "_H");
		replaceMap.put("%UI_CLASS%", baseClass);
		replaceMap.put("%CLASS%", className);
		replaceMap.put("%UI_HDR%", "ui_" + getUiFileNameBase() + ".h");
		replaceMap.put("%INCLUDE%", getRelativeHeaderFileName());

		String[] templates = { baseClass + ".ui", "gui.h", "gui.cpp" };
		String[] fileNames = getFileNames();
		String[] locations = getFileLocations();
		for (int i = 0; i < templates.length; i++) {
			InputStream template = getClass().getResourceAsStream(
					"/com/trolltech/qtcppproject/wizards/templates/QtGui/"
							+ templates[i]);

			IFile file = QtWizardUtils.createFile(new Path(locations[i] + "/"
					+ fileNames[i]), monitor);
			String source = "";
			if (template != null)
				source = QtWizardUtils.patchTemplateFile(template, replaceMap);
			file.create(new ByteArrayInputStream(source.getBytes()), true,
					monitor);
		}
	}

	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, 0);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		mainComposite.setLayout(layout);

		Composite container = new Composite(mainComposite, 0);
		GridData gd = new GridData(768);
		container.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		container.setLayout(layout);

		Label label = new Label(container, 0);
		label.setText("&Source Folder:");

		this.containerText = new Text(container, 2052);
		gd = new GridData(768);
		this.containerText.setLayoutData(gd);
		this.containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				UiFileWizardPage.this.dialogChanged();
			}
		});
		Button button = new Button(container, 8);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				UiFileWizardPage.this.handleBrowse();
			}
		});
		label = new Label(container, 0);
		label.setText("&Class Name:");

		this.classText = new Text(container, 2052);
		gd = new GridData(768);
		this.classText.setLayoutData(gd);
		this.classText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				UiFileWizardPage.this.syncFileNameBase();
				UiFileWizardPage.this.dialogChanged();
			}
		});
		label = new Label(container, 0);

		this.uigroup = new Group(mainComposite, 32);
		this.uigroup.setLayoutData(new GridData(768));

		this.uigroup.setText("File Information");

		layout = new GridLayout();
		layout.numColumns = 2;
		this.uigroup.setLayout(layout);

		addFileNameLabel("UI File Name:");
		this.uiFileName = addFileNameEditor();
		addFileNameLabel("Header File Name:");
		this.headerFileName = addFileNameEditor();
		addFileNameLabel("Source File Name:");
		this.sourceFileName = addFileNameEditor();

		this.uiTypeLable = new Label(this.uigroup, 8);
		this.uiTypeLable.setText("UI Type:");

		this.uiType = new Combo(this.uigroup, 8);
		this.uiType.setLayoutData(new GridData(768));

		this.uiType
				.setItems(new String[] { "QWidget", "QMainWindow", "QDialog" });
		this.uiType.select(0);

		this.filesLowerCase = new Button(this.uigroup, 32);
		this.filesLowerCase.setText("Make file names lowercase");
		this.filesLowerCase.setSelection(true);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		this.filesLowerCase.setLayoutData(data);
		this.filesLowerCase.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				UiFileWizardPage.this.syncFileNameBase();
			}
		});
		initialize();
		dialogChanged();
		setControl(mainComposite);
		setPageComplete(true);
	}

	private Label addFileNameLabel(String text) {
		Label label = new Label(this.uigroup, 8);
		label.setText(text);
		return label;
	}

	private Text addFileNameEditor() {
		Text editor = new Text(this.uigroup, 2052);
		editor.setLayoutData(new GridData(768));

		editor.addKeyListener(this);
		editor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				UiFileWizardPage.this.dialogChanged();
			}
		});
		return editor;
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		this.uichanged = true;
		dialogChanged();
	}

	private void syncFileNameBase() {
		if (!this.uichanged) {
			String className = getClassName();
			if (className.length() == 0) {
				this.uiFileName.setText("");
				this.headerFileName.setText("");
				this.sourceFileName.setText("");
			} else {
				this.uiFileName.setText("./" + getFileBaseFromClassName()
						+ ".ui");
				this.headerFileName.setText("./" + getFileBaseFromClassName()
						+ ".h");
				this.sourceFileName.setText("./" + getFileBaseFromClassName()
						+ ".cpp");
			}
			dialogChanged();
		}
	}

	private String getContainerName() {
		return this.containerText.getText();
	}

	private String getClassName() {
		return this.classText.getText();
	}

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));

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
		if (getClassName().length() == 0) {
			updateStatus("Class name must be specified");
			return;
		}
		if (!isValidClassName()) {
			return;
		}
		if (getUiFileName().length() == 0) {
			updateStatus("Ui file name must be specified");
			return;
		}
		if (getHeaderFileName().length() == 0) {
			updateStatus("Header file name must be specified");
			return;
		}
		if (getSourceFileName().length() == 0) {
			updateStatus("Source file name must be specified");
			return;
		}
		if (!getUiFileName().substring(getUiFileName().length() - 3)
				.equalsIgnoreCase(".ui")) {
			updateStatus("Ui file name must end with '.ui'");
			return;
		}
		String[] fileNames = getFileNames();
		String[] locations = getFileLocations();
		for (int i = 0; i < fileNames.length; i++) {
			String name = locations[i] + "/" + fileNames[i];
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			if (root.findMember(name) != null) {
				updateStatus("File " + name + " already exists");
				return;
			}
			IPath path = new Path(name);
			if ((path.hasTrailingSeparator())
					|| (root.getFile(new Path(name)) == null)) {
				updateStatus("Invalid file resource '" + name + "'");
				return;
			}
		}
		updateStatus(null);
	}

	private boolean isValidClassName() {
		String invalidClassname = "Class name is not valid : ";
		IStatus val = CConventions.validateClassName(getClassName());
		if (val.getSeverity() == 4) {
			updateStatus("Class name is not valid : " + val.getMessage());
			return false;
		}
		if (val.getSeverity() == 2)
			setMessage("Class name is not valid : " + val.getMessage(), 2);
		else {
			setMessage(null);
		}
		setPageComplete(true);
		return true;
	}

	private String getFileBaseFromClassName() {
		String fileBase = getClassName();
		if (this.filesLowerCase.getSelection())
			fileBase = fileBase.toLowerCase(Locale.US);
		return fileBase;
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
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
		this.classText.setFocus();
	}
}