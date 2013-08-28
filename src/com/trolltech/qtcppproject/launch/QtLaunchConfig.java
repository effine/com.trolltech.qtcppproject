package com.trolltech.qtcppproject.launch;

import com.trolltech.qtcppproject.QtProject;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;

public class QtLaunchConfig implements ILaunchConfigurationListener {
	private static ILaunchConfiguration[] getQtLaunchConfigurations(IProject pro) {
		Vector result = new Vector();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations();
			for (int i = 0; i < configs.length; i++) {
				String projectName = AbstractCLaunchDelegate
						.getProjectName(configs[i]);
				if (projectName != null) {
					projectName = projectName.trim();
					if (projectName.length() > 0) {
						IProject project = ResourcesPlugin.getWorkspace()
								.getRoot().getProject(projectName);
						if (project == pro)
							result.add(configs[i]);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return (ILaunchConfiguration[]) result
				.toArray(new ILaunchConfiguration[result.size()]);
	}

	public static void updateLaunchPaths(IProject pro, String prepend,
			String remove) {
		ILaunchConfiguration[] configs = getQtLaunchConfigurations(pro);
		for (int i = 0; i < configs.length; i++)
			updateConfigPath(configs[i], prepend, remove);
	}

	private static void updateConfigPath(ILaunchConfiguration configuration,
			String prepend, String remove) {
		try {
			ILaunchConfigurationWorkingCopy config = configuration
					.getWorkingCopy();
			Map m = config.getAttribute(
					ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, new HashMap());
			String path = QtUtils.createPath((String) m.get("PATH"), prepend,
					remove);
			m.put("PATH", path);
			config.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, m);
			config.doSave();

			updateDebugSources(configuration, prepend, remove);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private static void updateDebugSources(ILaunchConfiguration configuration,
			String prepend, String remove) throws CoreException {
		IPath pold = null;
		if (remove != null) {
			pold = new Path(remove).removeLastSegments(1);
			pold = pold.append("src");
		}

		if (prepend == null) {
			return;
		}
		IPath pnew = new Path(prepend).removeLastSegments(1);
		pnew = pnew.append("src");

		String memento = configuration
				.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO,
						(String) null);
		String type = configuration.getAttribute(
				ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String) null);
		if (type == null) {
			type = configuration.getType().getSourceLocatorId();
		}
		if (type == null) {
			return;
		}
		ISourceLocator locator = DebugPlugin.getDefault().getLaunchManager()
				.newSourceLocator(type);
		AbstractSourceLookupDirector fLocator = (AbstractSourceLookupDirector) locator;
		if (memento == null)
			fLocator.initializeDefaults(configuration);
		else {
			fLocator.initializeFromMemento(memento, configuration);
		}

		Vector newlist = new Vector();
		ISourceContainer[] containers = fLocator.getSourceContainers();
		for (int i = 0; i < containers.length; i++) {
			ISourceContainer c = containers[i];
			if ((pold != null) && ((c instanceof DirectorySourceContainer))) {
				DirectorySourceContainer dc = (DirectorySourceContainer) c;
				if (dc.getDirectory().getPath()
						.compareTo(pold.toFile().getPath()) != 0)
					newlist.add(dc);
			} else {
				newlist.add(c);
			}
		}

		newlist.add(new DirectorySourceContainer(pnew, true));

		ILaunchConfigurationWorkingCopy config = configuration.getWorkingCopy();

		fLocator.setSourceContainers((ISourceContainer[]) newlist
				.toArray(new ISourceContainer[newlist.size()]));

		config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO,
				fLocator.getMemento());
		config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
				fLocator.getId());
		config.doSave();
	}

	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		try {
			ICProject pro = AbstractCLaunchDelegate.getCProject(configuration);
			if ((pro == null)
					|| (!pro.getProject().hasNature(
							"com.trolltech.qtcppproject.QtNature")))
				return;
			String qtdir = new QtProject(pro.getProject()).getQtBinPath();
			updateConfigPath(configuration, qtdir, null);

			setStandardWindowsCommandFactory(configuration);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setStandardWindowsCommandFactory(
			ILaunchConfiguration configuration) {
		if (Platform.getOS().compareTo("win32") != 0) {
			return;
		}
		try {
			ILaunchConfigurationWorkingCopy config = configuration
					.getWorkingCopy();
			config.setAttribute(
					IMILaunchConfigurationConstants.ATTR_DEBUGGER_COMMAND_FACTORY,
					"org.eclipse.cdt.debug.mi.core.standardWinCommandFactory");

			config.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
	}

	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
	}
}