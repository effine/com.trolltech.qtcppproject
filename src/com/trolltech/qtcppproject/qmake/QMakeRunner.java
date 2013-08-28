package com.trolltech.qtcppproject.qmake;

import com.trolltech.qtcppproject.QtProject;
import com.trolltech.qtcppproject.QtProjectPlugin;
import com.trolltech.qtcppproject.preferences.QtPreferencePage;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class QMakeRunner {
	private static final boolean WINDOWS = File.separatorChar == '\\';
	private static final String QMAKE_PROBLEM = "com.trolltech.qtcppproject.qtproblem";

	public static String runQMake(IProject project, IProgressMonitor monitor) {
		checkCancel(monitor);
		removeQMakeErrors(project);
		try {
			String errStr = runQMake(project, null, monitor);
			if (errStr != null) {
				reportQMakeError(project, errStr);
				return errStr;
			}
		} catch (CoreException e) {
			e.printStackTrace();
			reportQMakeError(project, e.getLocalizedMessage());
			return e.getLocalizedMessage();
		}
		return null;
	}

	public static void checkCancel(IProgressMonitor monitor) {
		if ((monitor != null) && (monitor.isCanceled()))
			throw new OperationCanceledException();
	}

	private static void removeQMakeErrors(IProject project) {
		try {
			project.deleteMarkers("com.trolltech.qtcppproject.qtproblem",
					false, 0);
		} catch (CoreException ex) {
		}
	}

	private static void reportQMakeError(IProject project, String message) {
		try {
			IMarker marker = project
					.createMarker("com.trolltech.qtcppproject.qtproblem");
			marker.setAttribute("message", message);
			marker.setAttribute("severity", 2);
		} catch (CoreException ex) {
		}
	}

	public static String runQMake(IProject project,
			Map<String, String> envMods, IProgressMonitor monitor)
			throws CoreException {
		IPath proFilePath = QtUtils.findProFile(project);

		if (proFilePath == null) {
			return "Unable to locate pro file for project";
		}

		IPath buildCommand = getQMakeCommand(project);
		if (buildCommand == null) {
			return "Could not execute qmake (no Qt version set). Open Preferences->Qt and set a Qt version to use.";
		}

		IConsole console = CCorePlugin.getDefault().getConsole();
		console.start(project);
		ConsoleOutputStream consoleOut = console.getOutputStream();
		ConsoleOutputStream consoleErr = console.getErrorStream();

		Properties envProps = EnvironmentReader.getEnvVars();
		IPath workingDir = QtUtils.removeFileName(proFilePath);
		envProps.setProperty("CWD", workingDir.toOSString());
		envProps.setProperty("PWD", workingDir.toOSString());

		for (IQMakeEnvironmentModifier envModifier : QtProjectPlugin
				.getDefault().getEnvironmentModifierExtensions()) {
			envProps = envModifier.getModifiedEnvironment(project, envProps);
		}

		if (envMods != null) {
			for (String name : envMods.keySet()) {
				envProps.setProperty(name, (String) envMods.get(name));
			}
		}

		String errStr = runQMake(proFilePath, buildCommand, consoleOut,
				consoleErr, envProps, monitor);
		if (errStr == null)
			try {
				project.refreshLocal(2, null);
			} catch (CoreException e) {
			}
		markGeneratedFilesAsDerived(project);

		return errStr;
	}

	public static void markGeneratedFilesAsDerived(IProject project) {
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if ((resource instanceof IFile)) {
						String name = resource.getName().toLowerCase();
						if ((name.equals("bld.inf")) || (name.endsWith(".mmp"))) {
							resource.setDerived(true);
						}
						return false;
					}
					return true;
				}
			}, 2, 4);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private static String runQMake(IPath proFilePath, IPath buildCommand,
			ConsoleOutputStream stdout, ConsoleOutputStream stderr,
			Properties envProps, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		subMonitor.newChild(1).subTask("QMake Runner - Collecting Data");

		CommandLauncher launcher = new CommandLauncher();
		launcher.showCommand(true);

		String errMsg = null;

		List args = new ArrayList();
		args.add("-recursive");
		if (!WINDOWS) {
			args.add("CONFIG+=debug_and_release");
		}

		args.add(QtUtils.getFileName(proFilePath, false));
		IPath workingDir = QtUtils.removeFileName(proFilePath);

		Process p = launcher.execute(buildCommand,
				(String[]) args.toArray(new String[args.size()]),
				createEnvStringList(envProps), workingDir);

		if (p != null) {
			try {
				p.getOutputStream().close();
			} catch (IOException e) {
			}
			subMonitor.newChild(1).subTask(
					"QMake Runner - Starting QMake: "
							+ launcher.getCommandLine());

			if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
					monitor, 0)) != 0) {
				errMsg = launcher.getErrorMessage();
			}
			if (p.exitValue() != 0) {
				String outputStr = stderr.readBuffer();
				if ((outputStr != null) && (outputStr.length() > 0)) {
					errMsg = outputStr;
				} else {
					outputStr = stdout.readBuffer();
					if ((outputStr != null) && (outputStr.length() > 0))
						errMsg = outputStr;
					else
						errMsg = "Error processing '" + proFilePath + "'";
				}
			} else {
				QtProjectPlugin.getDefault().clearRunningQMakeRequest(
						proFilePath.toOSString());
			}
			try {
				stdout.close();
				stderr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			errMsg = launcher.getErrorMessage();
		}

		monitor.done();

		return errMsg;
	}

	private static IPath getQMakeCommand(IProject project) {
		String qtBinPath = null;

		if (project != null) {
			QtProject qtProject = new QtProject(project);
			qtBinPath = qtProject.getQtBinPath();
		} else {
			String defaultVersion = QtProjectPlugin.getDefault()
					.getPreferenceStore()
					.getString("com.trolltech.qtcppproject.qtversiondefault");
			qtBinPath = QtPreferencePage.getQtVersionBinPath(defaultVersion);
		}

		if (qtBinPath == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getDisplay()
							.getActiveShell();
					MessageDialog dialog = new MessageDialog(
							shell,
							"Build Error",
							null,
							"No default Qt version is set. Open the Qt page in the preferences dialog and add a Qt version.",
							1, new String[] { "Open preferences",
									IDialogConstants.CANCEL_LABEL }, 0);

					if (dialog.open() == 1)
						return;
					PreferencesUtil
							.createPreferenceDialogOn(
									shell,
									"com.trolltech.qtcppproject.preferences.QtPreferencePage",
									null, null).open();
				}
			});
			if (project != null) {
				QtProject qtProject = new QtProject(project);
				qtBinPath = qtProject.getQtBinPath();
			} else {
				String defaultVersion = QtProjectPlugin
						.getDefault()
						.getPreferenceStore()
						.getString(
								"com.trolltech.qtcppproject.qtversiondefault");
				qtBinPath = QtPreferencePage
						.getQtVersionBinPath(defaultVersion);
			}

			if (qtBinPath == null) {
				return null;
			}
		}

		return new Path(qtBinPath).append("qmake");
	}

	private static String[] createEnvStringList(Properties envProps) {
		String[] env = null;
		List envList = new ArrayList();
		Enumeration names = envProps.propertyNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				envList.add(key + "=" + envProps.getProperty(key));
			}
			env = (String[]) envList.toArray(new String[envList.size()]);
		}
		return env;
	}
}