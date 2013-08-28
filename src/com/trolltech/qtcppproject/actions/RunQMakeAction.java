package com.trolltech.qtcppproject.actions;

import com.trolltech.qtcppproject.qmake.QMakeRunner;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceAction;

public class RunQMakeAction extends WorkspaceAction implements
		IWorkbenchWindowActionDelegate {
	private Set<String> projectsForQMake;

	public RunQMakeAction() {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), new String(
				"Run qmake"));

		this.projectsForQMake = new HashSet();
	}

	protected String getOperationMessage() {
		return new String("Running qmake");
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	protected void invokeOperation(IResource resource, IProgressMonitor monitor) {
		IProject project = resource.getProject();
		if (project == null) {
			return;
		}

		if (!QtUtils.isQtProject(project)) {
			return;
		}
		String projectPath = project.getLocation().toString();
		if (this.projectsForQMake.contains(projectPath))
			return;
		this.projectsForQMake.add(projectPath);
		QMakeRunner.runQMake(project, monitor);
	}

	private IProject qtProjectOfActiveEditor() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart epart = page.getActiveEditor();
		IWorkbenchPart wpart = page.getActivePart();
		if ((epart != null) && (epart.equals(wpart))) {
			IEditorInput input = epart.getEditorInput();
			return QtUtils.qtProjectOfEditorInput(input);
		}
		return null;
	}

	private boolean isQtProjectSelected() {
		List list = getActionResources();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			IResource resource = (IResource) iterator.next();
			IProject project = resource.getProject();
			if (QtUtils.isQtProject(project))
				return true;
		}
		return false;
	}

	public void run(IAction action) {
		final IProject project = qtProjectOfActiveEditor();
		if (project != null) {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					QMakeRunner.runQMake(project, monitor);
					monitor.done();
				}
			};
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.run(true, false, op);
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
				Throwable realException = e.getTargetException();
				MessageDialog.openError(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), "Error",
						realException.getMessage());
				return;
			}
		} else {
			this.projectsForQMake.clear();

			run();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if ((selection instanceof IStructuredSelection)) {
			selectionChanged((IStructuredSelection) selection);
		}

		boolean enabled = (qtProjectOfActiveEditor() != null)
				|| (isQtProjectSelected());
		setEnabled(enabled);
		action.setEnabled(enabled);
	}
}