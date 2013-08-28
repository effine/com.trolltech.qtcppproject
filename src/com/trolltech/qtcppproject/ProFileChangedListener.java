package com.trolltech.qtcppproject;

import com.trolltech.qtcppproject.qmake.QMakeRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ProFileChangedListener implements IResourceChangeListener {
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == 1) {
			IResourceDelta[] projects = event.getDelta().getAffectedChildren();
			if (projects.length > 0)
				for (IResourceDelta projectDelta : projects)
					if (((projectDelta.getFlags() & 0x4000) == 0)
							&& (projectDelta.getKind() != 2)) {
						if ((projectDelta.getFlags() & 0x20000) == 0) {
							if ((projectDelta.getFlags() & 0x80000) == 0) {
								IResource resource = projectDelta.getResource();
								if ((resource != null)
										&& ((resource instanceof IProject))) {
									final IProject project = (IProject) resource;
									try {
										if ((project.isAccessible())
												&& (project
														.hasNature("com.trolltech.qtcppproject.QtNature"))) {
											if (new QtProject(project)
													.runQMakeWhenProFileChanges()) {
												boolean runQmake = visitChildren(projectDelta);
												if (runQmake) {
													WorkspaceJob runQmakJob = new WorkspaceJob(
															"Invoking qmake") {
														public IStatus runInWorkspace(
																IProgressMonitor monitor)
																throws CoreException {
															String errStr = QMakeRunner
																	.runQMake(
																			project,
																			monitor);
															if (errStr != null) {
																return new Status(
																		4,
																		"com.trolltech.qtcppproject",
																		errStr);
															}
															return Status.OK_STATUS;
														}
													};
													runQmakJob.setUser(true);
													runQmakJob.setRule(project);
													runQmakJob.schedule();
												}
											}
										}
									} catch (CoreException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
		}
	}

	private boolean visitChildren(IResourceDelta delta) {
		IResourceDelta[] changedChildren = delta.getAffectedChildren(4);
		if (changedChildren.length > 0) {
			for (IResourceDelta child : changedChildren) {
				if ((child.getFlags() & 0x20000) == 0) {
					IResource resource = child.getResource();
					if (resource != null) {
						if ((resource instanceof IFile)) {
							String name = resource.getName().toLowerCase();

							if ((name.endsWith(".pro"))
									|| (name.endsWith(".pri"))) {
								return true;
							}
						} else if ((resource instanceof IFolder)) {
							visitChildren(child);
						}
					}
				}
			}
		}
		return false;
	}
}