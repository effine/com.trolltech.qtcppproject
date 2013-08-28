package com.trolltech.qtcppproject.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

import com.trolltech.qtcppcommon.editors.EditorInputWatcher;
import com.trolltech.qtcppcommon.editors.IQtEditor;

import com.trolltech.qtcppproject.pages.QrcTreePage;

public class QrcEditor extends FormEditor implements IQtEditor {
	private QrcTreePage treePage = null;
	private EditorInputWatcher listener;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		listener = new EditorInputWatcher(this);
	}

	public void reload() {
		this.treePage.reload();
	}

	public void doSave(IProgressMonitor monitor) {
		this.treePage.doSave(monitor);
		this.listener.updateTimeStamp();
	}

	public void doSaveAs() {
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	protected void addPages() {
		setPartName(getEditorInput().getName());
		try {
			this.treePage = new QrcTreePage(this);
			addPage(this.treePage);
		} catch (Exception e) {
		}
	}

	public boolean isDirty() {
		return this.treePage.isDirty();
	}

	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 0)
			this.treePage.setFocus();
	}

	public void dispose() {
		this.listener.dispose();
		super.dispose();
	}

	public void setFocus() {
		super.setFocus();
		if (getCurrentPage() == 0)
			this.treePage.setFocus();
	}
}