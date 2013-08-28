package com.trolltech.qtcppproject.dialogs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.trolltech.qtcppproject.pages.embedded.ScopeList;

import com.trolltech.qtcppproject.editors.ProEditor;
import com.trolltech.qtcppproject.utils.QtProjectVisitor;

public class SelectScopeDialog extends Dialog implements SelectionListener {
	private ScopeList m_scope;
	private Label m_label;
	private Combo m_combo;
	private IProject m_pro;
	private Map<String, Vector<?>> m_files;
	private Vector<IFile> m_profiles;
	private String m_desc = "";
	private boolean m_remove = false;

	public SelectScopeDialog(Shell parentShell, IProject pro) {
		super(parentShell);
		m_pro = pro;
		m_profiles = new Vector<IFile>();
		m_files = new HashMap<String, Vector<?>>();
	}

	public void setDescription(String desc) {
		m_desc = desc;
	}

	public void addFiles(Vector<?> files, String var) {
		m_remove = false;
		if (!m_desc.equals("")) {
			m_desc = "Select where you want to insert the files in the Qt project settings.";
		}
		m_files.put(var, files);
	}

	public void removeFiles(Vector<?> files, String var) {
		m_remove = true;
		if (!m_desc.equals("")) {
			m_desc = "The files will be removed from the following Qt project settings:";
		}
		m_files.put(var, files);
	}

	public void widgetSelected(SelectionEvent e) {
		int index = m_combo.getSelectionIndex();
		setProjectFile(m_profiles.get(index));
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void okPressed() {
		for (int i = 0; i < m_profiles.size(); i++) {
			IFile file = m_profiles.get(i);
			String proFileName = file.getLocation().toOSString();
			if (m_scope.isChanged(proFileName)) {
				String contents;

				if (m_remove)
					contents = m_scope.removeFiles(proFileName);
				else {
					contents = m_scope.addFiles(proFileName);
				}
				openAndModifyProjectFile(file, contents);
			}
		}
		close();
	}

	public void cancelPressed() {
		close();
	}

	protected void setProjectFile(IFile file) {
		m_scope.showModel(file.getLocation().toOSString(), getContents(file),
				!m_remove);
	}

	private String getContents(IFile file) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart part = page.findEditor(new FileEditorInput(file));
		String contents = new String();
		if ((part instanceof TextEditor)) {
			TextEditor te = (TextEditor) part;
			IDocumentProvider provider = te.getDocumentProvider();
			IDocument document = provider.getDocument(te.getEditorInput());
			contents = document.get();
		} else {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(file.getContents()));
				String line;
				do {
					line = reader.readLine();
					if (line != null)
						contents = contents + line + "\n";
				} while (line != null);
				reader.close();
			} catch (Exception e) {
			}
		}
		return contents;
	}

	protected void openAndModifyProjectFile(IFile file, String newContents) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			IEditorPart part = page.openEditor(new FileEditorInput(file),
					ProEditor.ID, true, 3);

			if ((part instanceof TextEditor)) {
				TextEditor te = (TextEditor) part;
				IDocumentProvider provider = te.getDocumentProvider();
				IDocument document = provider.getDocument(te.getEditorInput());
				document.set(newContents);
			}
		} catch (WorkbenchException e) {
			e.printStackTrace();
		}
	}

	// add code for resolve warn SuppressWarnings
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean findProjectFiles() {
		QtProjectVisitor provisitor = new QtProjectVisitor();
		m_profiles.addAll(provisitor.findFiles(m_pro, "pro"));
		m_profiles.addAll(provisitor.findFiles(m_pro, "pri"));
		Collections.sort(m_profiles, new Comparator() {
			public int compare(Object o1, Object o2) {
				int res = ((IResource) o1)
						.getParent()
						.getProjectRelativePath()
						.toOSString()
						.compareTo(
								((IResource) o2).getParent()
										.getProjectRelativePath().toOSString());

				if (res != 0)
					return res;
				return ((IResource) o1).getName().compareTo(
						((IResource) o2).getName());
			}

			public boolean equals(Object obj) {
				return compare(this, obj) == 0;
			}

		});
		return !m_profiles.isEmpty();
	}

	protected void addFiles() {
		Iterator<String> keyit = m_files.keySet().iterator();
		while (keyit.hasNext()) {
			String var = keyit.next();
			Vector<?> files = m_files.get(var);
			for (int i = 0; i < files.size(); i++)
				m_scope.addFile(((IFile) files.get(i)).getLocation()
						.toOSString(), var);
		}
	}

	protected void createRemoveDialog(Composite composite) {
		m_combo = new Combo(composite, 8);

		GridData data = new GridData(768);

		data.widthHint = convertHorizontalDLUsToPixels(300);
		m_combo.setLayoutData(data);
		m_combo.setFont(composite.getParent().getFont());

		m_scope = new ScopeList(composite, 16777216);
		data = new GridData(1808);

		data.widthHint = convertHorizontalDLUsToPixels(300);
		data.heightHint = 300;
		m_scope.setLayoutData(data);
		m_scope.setFont(composite.getParent().getFont());

		addFiles();

		Vector<IFile> profiles = m_profiles;
		m_profiles = new Vector<IFile>();

		for (int i = 0; i < profiles.size(); i++) {
			IFile file = profiles.get(i);
			if (m_scope.search(file.getLocation().toOSString(),
					getContents(file))) {
				m_profiles.add(file);
				m_combo.add(file.getProjectRelativePath().toOSString());
			}
		}

		if (!m_profiles.isEmpty()) {
			selectProperProFile();
		} else {
			m_label.setText("Could not find the removed file(s) in any Qt project file. You have to edit your pro/pri files manually.");
			m_combo.setEnabled(false);
			m_scope.showModel("", "", false);
		}
	}

	protected void createAddDialog(Composite composite) {
		m_combo = new Combo(composite, 8);

		for (int i = 0; i < m_profiles.size(); i++) {
			IResource res = m_profiles.get(i);
			if (res.getType() == 1) {
				IFile f = (IFile) res;
				m_combo.add(f.getProjectRelativePath().toOSString());
			}
		}

		GridData data = new GridData(768);

		data.widthHint = convertHorizontalDLUsToPixels(300);
		m_combo.setLayoutData(data);
		m_combo.setFont(composite.getParent().getFont());

		m_scope = new ScopeList(composite, 16777216);
		data = new GridData(1808);

		data.widthHint = convertHorizontalDLUsToPixels(300);
		data.heightHint = 300;

		m_scope.setLayoutData(data);
		m_scope.setFont(composite.getParent().getFont());

		addFiles();

		if (!m_profiles.isEmpty()) {
			selectProperProFile();
			m_scope.selectFirstVariable();
		} else {
			m_label.setText("Could not find any Qt project file. You have to edit your pro/pri files manually.");
			m_combo.setEnabled(false);
			m_scope.showModel("", "", false);
		}
	}

	private void selectProperProFile() {
		int index = 0;
		if (!m_files.values().isEmpty()) {
			IFile first = (IFile) ((Vector<?>) m_files.values().toArray()[0])
					.firstElement();
			String path = first.getParent().getProjectRelativePath()
					.toPortableString();
			for (int i = 0; i < m_profiles.size(); i++) {
				IPath otherPath = m_profiles.elementAt(i).getParent()
						.getProjectRelativePath();
				if (otherPath.toPortableString().equals(path)) {
					index = i;
					if ("pro".equals(otherPath.getFileExtension()))
						break;
				}
			}
		}
		m_combo.select(index);
		m_combo.addSelectionListener(this);
		setProjectFile(m_profiles.get(index));
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		m_label = new Label(composite, 64);
		m_label.setText(m_desc);

		GridData data = new GridData(768);

		data.widthHint = convertHorizontalDLUsToPixels(300);
		m_label.setLayoutData(data);
		m_label.setFont(composite.getParent().getFont());

		findProjectFiles();
		if (m_remove)
			createRemoveDialog(composite);
		else {
			createAddDialog(composite);
		}

		applyDialogFont(composite);
		return composite;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (m_remove)
			shell.setText("Remove files from Qt project");
		else
			shell.setText("Add files to Qt project");
	}
}