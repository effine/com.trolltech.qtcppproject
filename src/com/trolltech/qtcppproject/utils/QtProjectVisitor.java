package com.trolltech.qtcppproject.utils;

import java.util.Vector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;

public class QtProjectVisitor implements IResourceProxyVisitor {
	private Vector result;
	private String extention;

	public QtProjectVisitor() {
		this.result = new Vector();
	}

	public boolean visit(IResourceProxy proxy) throws CoreException {
		if ((proxy.getName().endsWith(this.extention))
				&& (proxy.getType() == 1)) {
			this.result.add(proxy.requestResource());
			return false;
		}
		return true;
	}

	public Vector findFiles(IProject project, String ext) {
		this.extention = ("." + ext);
		try {
			this.result.clear();
			project.accept(this, 0);
			return this.result;
		} catch (Exception e) {
		}
		return null;
	}
}