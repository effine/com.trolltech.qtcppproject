package com.trolltech.qtcppproject;

import com.trolltech.qtcppproject.dialogs.SelectScopeDialog;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.util.Vector;
import org.eclipse.cdt.core.CConventions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class QtProjectMonitor implements IResourceChangeListener, Runnable {
	private IProject m_pro;
	private Vector m_filesAdded;
	private Vector m_filesRemoved;
	private static final String SOURCE_FILES = "SOURCES";
	private static final String HEADER_FILES = "HEADERS";
	private static final String FORM_FILES = "FORMS";
	private static final String RESOURCE_FILES = "RESOURCES";

	public QtProjectMonitor() {
		m_filesAdded = new Vector();
		m_filesRemoved = new Vector();

		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ws.addResourceChangeListener(this, 1);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		m_filesAdded.clear();
		m_filesRemoved.clear();

		IResourceDelta delta = event.getDelta();

		if ((delta.getKind() == 1) || (delta.getKind() == 2)
				|| ((delta.getFlags() & 0x4000) != 0)) {
			if (isQtProject(delta.getResource())) {
				return;
			}
		}
		handleResourceDeltas(delta);

		if ((!m_filesAdded.isEmpty()) || (!m_filesRemoved.isEmpty()))
			Display.getDefault().syncExec(this);
	}

	public boolean isQtProject(IResource res) {
		if (res.getType() == 4) {
			IProject pro = (IProject) res;
			try {
				if ((pro.exists())
						&& (pro.isOpen())
						&& (pro.hasNature("com.trolltech.qtcppproject.QtNature")))
					return true;
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void handleResourceDeltas(IResourceDelta delta) {
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
			IResourceDelta child = children[i];
			IResource current = child.getResource();

			if (child.getKind() == 1) {
				if (isQtProject(current))
					return;
				if (isValidFile(current))
					m_filesAdded.add(current);
			} else if (child.getKind() == 2) {
				if (isQtProject(current))
					return;
				if (isValidFile(current))
					m_filesRemoved.add(current);
			} else if (((child.getFlags() & 0x4000) != 0)
					&& (isQtProject(current))) {
				return;
			}

			handleResourceDeltas(child);
		}
	}

	public boolean isValidFile(IResource resource) {
		if (resource.getType() != 1) {
			return false;
		}
		m_pro = resource.getProject();
		if (isQtProject(m_pro)) {
			return true;
		}
		return false;
	}

	protected void distributeFileTypes(boolean remove) {
		Vector files = m_filesAdded;
		if (remove) {
			files = m_filesRemoved;
		}
		Vector sources = new Vector();
		Vector headers = new Vector();
		Vector forms = new Vector();
		Vector resources = new Vector();

		for (int i = 0; i < files.size(); i++) {
			IFile file = (IFile) files.get(i);
			IProject pro = file.getProject();
			String name = file.getName();

			if (CConventions.validateSourceFileName(pro, name).isOK()) {
				if (!QtUtils.isGeneratedSourceFile(name))
					sources.add(file);
			} else if (CConventions.validateHeaderFileName(pro, name).isOK()) {
				if (!QtUtils.isGeneratedHeaderFile(name))
					headers.add(file);
			} else if (QtUtils.isFormFile(name))
				forms.add(file);
			else if (QtUtils.isResourceFile(name)) {
				resources.add(file);
			}
		}

		if ((sources.isEmpty()) && (headers.isEmpty()) && (forms.isEmpty())
				&& (resources.isEmpty())) {
			return;
		}
		IWorkbenchWindow win = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		SelectScopeDialog dlg = new SelectScopeDialog(win.getShell(), m_pro);

		if (remove) {
			if (!sources.isEmpty())
				dlg.removeFiles(sources, "SOURCES");
			if (!headers.isEmpty())
				dlg.removeFiles(headers, "HEADERS");
			if (!forms.isEmpty())
				dlg.removeFiles(forms, "FORMS");
			if (!resources.isEmpty())
				dlg.removeFiles(resources, "RESOURCES");
		} else {
			if (!sources.isEmpty())
				dlg.addFiles(sources, "SOURCES");
			if (!headers.isEmpty())
				dlg.addFiles(headers, "HEADERS");
			if (!forms.isEmpty())
				dlg.addFiles(forms, "FORMS");
			if (!resources.isEmpty()) {
				dlg.addFiles(resources, "RESOURCES");
			}
		}
		dlg.open();
	}

	public void run() {
		distributeFileTypes(false);
		distributeFileTypes(true);
	}
}