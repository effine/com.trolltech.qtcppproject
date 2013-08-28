package com.trolltech.qtcppproject;

import com.trolltech.qtcppproject.wizards.QtProjectCreatorListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class QtProjectCreator {
	private IProject project;
	private IPath projectLocation = null;
	private QtProjectCreatorListener listener = null;
	private boolean copyToWorkspace = false;
	private Shell shell;

	public QtProjectCreator(IProject project, IPath projectLocation) {
		this(null, project, projectLocation, false);
	}

	public QtProjectCreator(Shell shell, IProject project,
			IPath projectLocation, boolean copyToWorkspace) {
		this.project = project;
		this.projectLocation = projectLocation;
		this.copyToWorkspace = copyToWorkspace;
		this.shell = shell;
	}

	public void addQtProjectCreatorListener(QtProjectCreatorListener listener) {
		this.listener = listener;
	}

	public void create(IRunnableContext runnableContext) {
		IPath defaultPath = Platform.getLocation();

		if ((defaultPath.equals(this.projectLocation))
				|| (defaultPath.equals(this.projectLocation
						.removeLastSegments(1)))) {
			this.projectLocation = null;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace
				.newProjectDescription(this.project.getName());

		final IProject newProject = this.project;
		if (!this.copyToWorkspace)
			description.setLocation(this.projectLocation);
		else {
			description.setLocation(null);
		}
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws CoreException {
				try {
					monitor.subTask("Configuring Qt Project...");
					newProject.create(description, monitor);
					if (monitor.isCanceled())
						throw new OperationCanceledException();
					newProject.open(monitor);
					if (QtProjectCreator.this.copyToWorkspace) {
						if (monitor.isCanceled())
							throw new OperationCanceledException();
						monitor.subTask("Copying the files...");
						CopyFilesAndFoldersOperation copyProc = new CopyFilesAndFoldersOperation(
								QtProjectCreator.this.shell);
						String sourcePath = QtProjectCreator.this.projectLocation
								.addTrailingSeparator().toOSString();
						File sourceDir = new File(sourcePath);
						String[] copyFileNames = sourceDir.list();
						if (copyFileNames != null) {
							for (int i = 0; i < copyFileNames.length; i++)
								copyFileNames[i] = (sourcePath + copyFileNames[i]);
							copyProc.copyFilesInCurrentThread(copyFileNames,
									newProject, monitor);
						}
					}
					monitor.subTask("Setting up project builders.");
					if (monitor.isCanceled())
						throw new OperationCanceledException();
					if (QtProjectCreator.this.listener != null)
						QtProjectCreator.this.listener.projectCreated(
								newProject, monitor);
					newProject.refreshLocal(1, monitor);
					new QtProject(newProject).convertToQtProject(monitor);
					monitor.done();
				} catch (CoreException ex) {
					IStatus status = ex.getStatus();
					String message = "";

					if (!status.isMultiStatus()) {
						message = status.getMessage();
					} else {
						IStatus[] children = status.getChildren();
						for (int i = 0; i < children.length; i++)
							if (!children[i].isOK()) {
								message = message + children[i].getMessage();
							}
					}
					message.trim();
					Shell shell = PlatformUI.getWorkbench().getDisplay()
							.getActiveShell();
					MessageDialog.openError(shell, "Project creation failed",
							message);
					monitor.done();
					return;
				}
			}
		};
		try {
			runnableContext.run(false, true, op);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}