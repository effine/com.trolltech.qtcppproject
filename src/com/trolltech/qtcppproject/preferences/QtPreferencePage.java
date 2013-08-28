package com.trolltech.qtcppproject.preferences;

import com.trolltech.qtcppproject.QtProject;
import com.trolltech.qtcppproject.QtProjectPlugin;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.util.Iterator;
import java.util.Vector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.swt.SWT;

public class QtPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	public QtPreferencePage() {
	}

	private Label label;
	private Table table;
	private Button removeButton;
	private Button editButton;
	private Button defaultButton;
	private Button autosetmkspec;
	private Button autosetmkcmd;

	public static boolean getAutoSetMkSpec() {
		IPreferenceStore store = QtProjectPlugin.getDefault()
				.getPreferenceStore();

		if (store.contains("com.trolltech.qtcppproject.qtautosetmkspec")) {
			return store
					.getBoolean("com.trolltech.qtcppproject.qtautosetmkspec");
		}
		return true;
	}

	public static boolean getAutoSetMkCmd() {
		IPreferenceStore store = QtProjectPlugin.getDefault()
				.getPreferenceStore();

		if (store.contains("com.trolltech.qtcppproject.qtautosetmkcmd")) {
			return store
					.getBoolean("com.trolltech.qtcppproject.qtautosetmkcmd");
		}
		return true;
	}

	public static String[] getQtVersions() {
		Vector versions = new Vector();

		IPreferenceStore store = QtProjectPlugin.getDefault()
				.getPreferenceStore();
		if (!store.contains("com.trolltech.qtcppproject.qtversioncount")) {
			return null;
		}
		int count = store.getInt("com.trolltech.qtcppproject.qtversioncount");
		for (int i = 0; i < count; i++) {
			String name = "com.trolltech.qtcppproject.qtversionname."
					+ Integer.toString(i);
			if (store.contains(name)) {
				versions.add(store.getString(name));
			}
		}
		return (String[]) versions.toArray(new String[versions.size()]);
	}

	private static String[] getQtPaths(String version) {
		IPreferenceStore store = QtProjectPlugin.getDefault()
				.getPreferenceStore();
		if (!store.contains("com.trolltech.qtcppproject.qtversioncount")) {
			return null;
		}
		String defaultVersionName = store
				.getString("com.trolltech.qtcppproject.qtversiondefault");
		String defaultVersionBinPath = "";
		String defaultVersionIncludePath = "";
		int count = store.getInt("com.trolltech.qtcppproject.qtversioncount");
		for (int i = 0; i < count; i++) {
			String nameKey = "com.trolltech.qtcppproject.qtversionname."
					+ Integer.toString(i);
			String binpathKey = "com.trolltech.qtcppproject.qtversionbinpath."
					+ Integer.toString(i);
			String includepathKey = "com.trolltech.qtcppproject.qtversionincludepath."
					+ Integer.toString(i);
			String name = "";
			String binpath = "";
			String includepath = "";

			if (store.contains(nameKey))
				name = store.getString(nameKey);
			if (store.contains(binpathKey))
				binpath = store.getString(binpathKey);
			if (store.contains(includepathKey)) {
				includepath = store.getString(includepathKey);
			}
			if (name.equals(version))
				return new String[] { name, binpath, includepath };
			if (name.equals(defaultVersionName)) {
				defaultVersionBinPath = binpath;
				defaultVersionIncludePath = includepath;
			}
		}

		return new String[] { defaultVersionName, defaultVersionBinPath,
				defaultVersionIncludePath };
	}

	public static String getQtVersionBinPath(String version) {
		String[] paths = getQtPaths(version);
		if (paths != null)
			return getQtPaths(version)[1];
		return null;
	}

	public static String getQtVersionIncludePath(String version) {
		String[] paths = getQtPaths(version);
		if (paths != null)
			return getQtPaths(version)[2];
		return null;
	}

	public String[] getCurrentItem() {
		TableItem[] items = this.table.getSelection();
		if (items.length == 0)
			return null;
		return new String[] { items[0].getText(0), items[0].getText(1),
				items[0].getText(2) };
	}

	public void enableButtons(boolean enabled) {
		this.removeButton.setEnabled(enabled);
		this.defaultButton.setEnabled(enabled);
		this.editButton.setEnabled(enabled);
	}

	public void updateItem(String name, String binPath, String includePath) {
		TableItem[] items = this.table.getSelection();
		if (items.length == 0)
			return;
		items[0].setText(new String[] { name, binPath, includePath });
	}

	public void addItem(String name, String binPath, String includePath) {
		TableItem item = new TableItem(this.table, 0);
		item.setText(new String[] { name, binPath, includePath });
	}

	public void removeItem() {
		if (this.table.getSelectionCount() > 0) {
			int removeindex = this.table.getSelectionIndex();
			int defindex = getDefaultIndex();
			this.table.remove(removeindex);

			if (removeindex == defindex) {
				setDefaultIndex(0);
			}
		}
		enableButtons(this.table.getSelectionCount() > 0);
	}

	public boolean performOk() {
		IPreferenceStore store = QtProjectPlugin.getDefault()
				.getPreferenceStore();

		IProject[] pros = QtUtils.getQtProjects();
		QtProject[] qtProjects = new QtProject[pros.length];
		String[] oldBinPaths = new String[pros.length];
		String[] oldIncludePaths = new String[pros.length];
		for (int i = 0; i < pros.length; i++) {
			qtProjects[i] = new QtProject(pros[i]);
			oldBinPaths[i] = qtProjects[i].getQtBinPath();
			oldIncludePaths[i] = qtProjects[i].getQtIncludePath();
		}
		String defaultVersion = getDefaultQtVersionName();
		if (defaultVersion != null) {
			store.setValue("com.trolltech.qtcppproject.qtversiondefault",
					defaultVersion);
		}
		store.setValue("com.trolltech.qtcppproject.qtversioncount",
				this.table.getItemCount());
		for (int i = 0; i < this.table.getItemCount(); i++) {
			store.setValue("com.trolltech.qtcppproject.qtversionname."
					+ Integer.toString(i), this.table.getItem(i).getText(0));

			store.setValue("com.trolltech.qtcppproject.qtversionbinpath."
					+ Integer.toString(i), this.table.getItem(i).getText(1));

			store.setValue("com.trolltech.qtcppproject.qtversionincludepath."
					+ Integer.toString(i), this.table.getItem(i).getText(2));
		}

		store.setValue("com.trolltech.qtcppproject.qtautosetmkspec",
				this.autosetmkspec.getSelection());
		store.setValue("com.trolltech.qtcppproject.qtautosetmkcmd",
				this.autosetmkspec.getSelection());

		Vector outdated = new Vector();
		for (int i = 0; i < qtProjects.length; i++) {
			qtProjects[i].updateQtDir(oldBinPaths[i], oldIncludePaths[i]);
			if (((qtProjects[i].getQtBinPath() == null) && (oldBinPaths[i] != null))
					|| ((qtProjects[i].getQtBinPath() != null) && (!qtProjects[i]
							.getQtBinPath().equals(oldBinPaths[i])))
					|| ((qtProjects[i].getQtIncludePath() == null) && (oldIncludePaths[i] != null))
					|| ((qtProjects[i].getQtIncludePath() != null) && (!qtProjects[i]
							.getQtIncludePath().equals(oldIncludePaths[i])))) {
				outdated.add(qtProjects[i]);
			}
		}

		if (!outdated.isEmpty()) {
			askForRebuild(outdated);
		}
		return true;
	}

	private void askForRebuild(final Vector projects) {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		MessageDialog dialog = new MessageDialog(
				shell,
				"Qt Versions Changed",
				null,
				"Some projects' Qt versions have changed. A rebuild of the projects is required for changes to take effect. Do a full rebuild now?",
				3, new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL }, 0);

		if (dialog.open() == 0) {
			WorkspaceJob rebuild = new WorkspaceJob("Rebuild projects") {
				public boolean belongsTo(Object family) {
					return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
				}

				public IStatus runInWorkspace(IProgressMonitor monitor) {
					Iterator i = projects.iterator();
					while (i.hasNext()) {
						QtProject project = (QtProject) i.next();
						project.scheduleRebuild();
					}
					return Status.OK_STATUS;
				}
			};
			rebuild.setRule(ResourcesPlugin.getWorkspace().getRuleFactory()
					.buildRule());

			rebuild.setUser(true);
			rebuild.schedule();
		}
	}

	private void setDefaultIndex(int index) {
		for (int i = 0; i < this.table.getItemCount(); i++) {
			TableItem item = this.table.getItem(i);
			Font fnt = item.getFont();
			FontData fntdata = fnt.getFontData()[0];

			if (i == index) {
				int style = fntdata.getStyle() | 0x1;
				fntdata.setStyle(style);
				item.setFont(new Font(fnt.getDevice(), fntdata));
			} else if ((fntdata.getStyle() & 0x1) != 0) {
				int style = fntdata.getStyle() & 0xFFFFFFFE;
				fntdata.setStyle(style);
				item.setFont(new Font(fnt.getDevice(), fntdata));
			}
		}
	}

	private int getDefaultIndex() {
		for (int i = 0; i < this.table.getItemCount(); i++) {
			TableItem item = this.table.getItem(i);
			Font fnt = item.getFont();
			FontData fntdata = fnt.getFontData()[0];

			if ((fntdata.getStyle() & 0x1) != 0) {
				return i;
			}
		}
		if (this.table.getItemCount() > 0) {
			return 0;
		}
		return -1;
	}

	private String getDefaultQtVersionName() {
		int index = getDefaultIndex();
		if (index == -1) {
			return null;
		}
		return this.table.getItem(index).getText(0);
	}

	public void setCurrentDefault() {
		setDefaultIndex(this.table.getSelectionIndex());
	}

	private void updateItems() {
		IPreferenceStore store = QtProjectPlugin.getDefault()
				.getPreferenceStore();

		this.autosetmkspec.setSelection(getAutoSetMkSpec());
		this.autosetmkcmd.setSelection(getAutoSetMkCmd());

		if (!store.contains("com.trolltech.qtcppproject.qtversioncount")) {
			return;
		}
		int defaultVersionIndex = 0;
		String defaultVersionName = store
				.getString("com.trolltech.qtcppproject.qtversiondefault");

		int count = store.getInt("com.trolltech.qtcppproject.qtversioncount");
		for (int i = 0; i < count; i++) {
			String nameKey = "com.trolltech.qtcppproject.qtversionname."
					+ Integer.toString(i);
			String binpathKey = "com.trolltech.qtcppproject.qtversionbinpath."
					+ Integer.toString(i);
			String includepathKey = "com.trolltech.qtcppproject.qtversionincludepath."
					+ Integer.toString(i);
			String name = "";
			String binpath = "";
			String includepath = "";

			if (store.contains(nameKey))
				name = store.getString(nameKey);
			if (store.contains(binpathKey))
				binpath = store.getString(binpathKey);
			if (store.contains(includepathKey))
				includepath = store.getString(includepathKey);
			addItem(name, binpath, includepath);

			if (name.equals(defaultVersionName))
				defaultVersionIndex = i;
		}
		setDefaultIndex(defaultVersionIndex);
	}

	private void addQtBuildsSection(Composite parent) {
		if (Platform.getOS().equals("win32")) {
			Link vsWarning = new Link(parent, 0);
			vsWarning
					.setText("Please note:\nQt versions built with MinGW offer the full functionality of the Eclipse integration.\nVisual Studio builds bring limitations with them. <A HREF=\"/com.trolltech.qtcppintegrationhelp/doc/eclipse-integration-managing-projects.html#basic-qt-version-management\">Read about these limitations</A>.");

			SelectionListener vsWarningSelectionListener = new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					PlatformUI.getWorkbench().getHelpSystem()
							.displayHelpResource(e.text);
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			};
			vsWarning.addSelectionListener(vsWarningSelectionListener);
			GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			vsWarning.setLayoutData(gridData);
		}

		this.label = new Label(parent, 16777216);
		this.label.setText("Qt Versions:");
		GridData gridData = new GridData();
		gridData.horizontalAlignment = 32;
		gridData.horizontalSpan = 2;
		this.label.setLayoutData(gridData);

		this.table = new Table(parent, 67588);
		this.table.setHeaderVisible(true);

		TableColumn column = new TableColumn(this.table, 16384);
		column.setText("Name");
		column.setWidth(100);
		column.setResizable(true);
		column = new TableColumn(this.table, 16384);
		column.setText("Bin Path");
		column.setResizable(true);
		column.setWidth(100);
		column = new TableColumn(this.table, 16384);
		column.setText("Include Path");
		column.setResizable(true);
		column.setWidth(100);

		gridData = new GridData();
		gridData.verticalSpan = 5;
		gridData.widthHint = 250;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = 4;
		gridData.verticalAlignment = 4;

		this.table.setLayoutData(gridData);
		this.table.addSelectionListener(new QtVersionListener(this, 5));

		Button addButton = new Button(parent, 0);
		addButton.addSelectionListener(new QtVersionListener(this, 1));
		addButton.setText("Add...");
		gridData = new GridData();
		gridData.horizontalAlignment = 4;
		addButton.setLayoutData(gridData);

		this.editButton = new Button(parent, 0);
		this.editButton.addSelectionListener(new QtVersionListener(this, 2));
		this.editButton.setText("Edit...");
		gridData = new GridData();
		gridData.horizontalAlignment = 4;
		this.editButton.setLayoutData(gridData);

		this.removeButton = new Button(parent, 0);
		this.removeButton.addSelectionListener(new QtVersionListener(this, 3));
		this.removeButton.setText("Remove");
		gridData = new GridData();
		gridData.horizontalAlignment = 4;
		this.removeButton.setLayoutData(gridData);

		this.defaultButton = new Button(parent, 0);
		this.defaultButton.addSelectionListener(new QtVersionListener(this, 4));
		this.defaultButton.setText("Default");
		gridData = new GridData();
		gridData.horizontalAlignment = 4;
		this.defaultButton.setLayoutData(gridData);

		Composite spacer = new Composite(parent, 0);
		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = 4;
		gridData.horizontalAlignment = SWT.RIGHT;
		spacer.setLayoutData(gridData);

		this.autosetmkspec = new Button(parent, 32);
		this.autosetmkspec
				.setText("Auto update QMAKESPEC when applying changes.");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		this.autosetmkspec.setLayoutData(gridData);

		this.autosetmkcmd = new Button(parent, 32);
		this.autosetmkcmd
				.setText("Auto update make command when applying changes.");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		this.autosetmkcmd.setLayoutData(gridData);
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, 0);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		addQtBuildsSection(composite);
		updateItems();
		enableButtons(this.table.getSelectionCount() > 0);

		return composite;
	}

	public void init(IWorkbench workbench) {
	}
}