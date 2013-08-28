package com.trolltech.qtcppproject.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface QtProjectCreatorListener {

	public abstract void projectCreated(IProject iproject,
			IProgressMonitor iprogressmonitor);
}
