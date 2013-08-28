package com.trolltech.qtcppproject.qmake;

import com.trolltech.qtcppproject.QtProjectPlugin;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.io.File;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class QtMakefileGenerator extends IncrementalProjectBuilder {
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind != 9) {
			IProject project = getProject();
			IPath proFilePath = QtUtils.findProFile(project);

			if (proFilePath != null) {
				IPath proDir = QtUtils.removeFileName(proFilePath);
				IPath makefilePath = proDir.append(new String("Makefile"));
				if ((!makefilePath.toFile().exists())
						|| (QtProjectPlugin.getDefault()
								.isRunningQMakeRequest(proFilePath.toOSString()))) {
					QMakeRunner.runQMake(project, monitor);
				}
			}
		}

		return null;
	}

	protected void clean(IProgressMonitor monitor) {
	}
}