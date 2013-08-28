package com.trolltech.qtcppproject.pages;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.FileEditorInput;

import com.trolltech.qtcppcommon.editors.EditorInputWatcher;
import com.trolltech.qtcppproject.pages.embedded.QrcTreeView;
import com.trolltech.qtcppproject.pages.embedded.QrcTreeViewListener;

import com.trolltech.qtcppproject.editors.QrcEditor;

public class QrcTreePage extends FormPage implements QrcTreeViewListener {
	private QrcEditor m_editor;
	private QrcTreeView qrctreeview;

	public QrcTreePage(QrcEditor editor) {
		super(editor, "com.trolltech.QtProEditor.pages.QrcTreePage",
				"Resources");
		this.m_editor = editor;
	}

	public void setActive(boolean active) {
		super.setActive(active);
	}

	public void widgetSelected(SelectionEvent e) {
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public boolean isDirty() {
		return this.qrctreeview != null ? this.qrctreeview.isDirty() : false;
	}

	public void doSave(IProgressMonitor monitor) {
		if (this.qrctreeview == null)
			return;
		FileEditorInput fin = (FileEditorInput) getEditorInput();
		if (!this.qrctreeview.save())
			MessageDialog.openError(getSite().getShell(),
					"Error while saving file", "Can't write to: "
							+ fin.getFile().getLocation().toOSString()
							+ "\nMake sure it's not write protected.");
	}

	protected void createFormContent(IManagedForm managedForm) {
		Composite parent = managedForm.getForm().getBody();
		FileEditorInput fin = (FileEditorInput) getEditorInput();
		String fileName = fin.getFile().getLocation().toOSString();
		File file = new File(fileName);
		boolean readable = (file.exists()) && (file.canRead());
		if (!readable) {
			EditorInputWatcher.createMissingFileInfo(parent, fileName);
			return;
		}
		FormToolkit toolkit = managedForm.getToolkit();
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		createQrcTreeView(toolkit, parent);
		this.qrctreeview.load(fileName);
	}

	public void reload() {
		FileEditorInput fin = (FileEditorInput) getEditorInput();
		String fileName = fin.getFile().getLocation().toOSString();
		this.qrctreeview.load(fileName);
	}

	private void createQrcTreeView(FormToolkit toolkit, Composite parent) {
		this.qrctreeview = new QrcTreeView(parent, 16777216);
		this.qrctreeview.addQrcTreeViewListener(this);
		this.qrctreeview.setLayoutData(new GridData(500, 400));
		toolkit.adapt(this.qrctreeview);
		toolkit.paintBordersFor(this.qrctreeview);
	}

	public String contents() {
		return this.qrctreeview != null ? this.qrctreeview.contents() : "";
	}

	public void dirtyChanged() {
		this.m_editor.editorDirtyStateChanged();
	}

	public void setFocus() {
		super.setFocus();
		if (this.qrctreeview != null)
			this.qrctreeview.setFocus();
	}
}