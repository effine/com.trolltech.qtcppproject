package com.trolltech.qtcppproject;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class GeneratedFilesFilter extends ViewerFilter {
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IResource resource = null;
		if ((element instanceof IResource))
			resource = (IResource) element;
		else if ((element instanceof IAdaptable))
			resource = (IResource) ((IAdaptable) element)
					.getAdapter(IResource.class);
		if (resource != null) {
			if ((resource.getName().startsWith("moc_"))
					&& (resource.getName().endsWith(".cpp")))
				return false;
			if ((resource.getName().startsWith("ui_"))
					&& (resource.getName().endsWith(".h")))
				return false;
			if ((resource.getName().startsWith("qrc_"))
					&& (resource.getName().endsWith(".cpp")))
				return false;
		}
		return true;
	}
}