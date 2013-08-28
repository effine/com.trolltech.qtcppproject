package com.trolltech.qtcppproject;

import com.trolltech.qtcppproject.launch.QtLaunchConfig;
import com.trolltech.qtcppproject.preferences.QtPreferencePage;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.Vector;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorManager;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

public class QtProject {
	private static final String QTVERSION = "com.trolltech.qtcppproject.properties.qtversion";
	private static final String QTPROCHANGELISTENER = "com.trolltech.qtcppproject.properties.qtprochangelistener";
	private static final String QT_RELEASE_NAME = "Qt Release Build";
	private static final String QT_DEBUG_NAME = "Qt Debug Build";
	private static final String[] QT_INCLUDE_PATHS = { "", "ActiveQt",
			"phonon", "Qt3Support", "QtAssistant", "QtCore", "QtDBus",
			"QtDesigner", "QtGui", "QtHelp", "QtNetwork", "QtOpenGL",
			"QtScript", "QtSql", "QtSvg", "QtTest", "QtUiTools", "QtWebKit",
			"QtXml", "QtXmlPatterns" };
	private IProject wrapped;

	public QtProject(IProject wrappedProject) {
		this.wrapped = wrappedProject;
	}

	public IProject getProject() {
		return this.wrapped;
	}

	public String getQtBinPath() {
		try {
			String version = this.wrapped
					.getPersistentProperty(new QualifiedName("",
							"com.trolltech.qtcppproject.properties.qtversion"));
			return QtPreferencePage.getQtVersionBinPath(version);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getQtIncludePath() {
		try {
			String version = this.wrapped
					.getPersistentProperty(new QualifiedName("",
							"com.trolltech.qtcppproject.properties.qtversion"));
			return QtPreferencePage.getQtVersionIncludePath(version);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getQtVersion() {
		try {
			return this.wrapped.getPersistentProperty(new QualifiedName("",
					"com.trolltech.qtcppproject.properties.qtversion"));
		} catch (CoreException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public boolean setQtVersion(String version) {
		try {
			String oldBinPath = getQtBinPath();
			String oldIncludePath = getQtIncludePath();
			this.wrapped
					.setPersistentProperty(new QualifiedName("",
							"com.trolltech.qtcppproject.properties.qtversion"),
							version);
			updateQtDir(oldBinPath, oldIncludePath);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean runQMakeWhenProFileChanges() {
		try {
			String value = this.wrapped
					.getPersistentProperty(new QualifiedName("",
							"com.trolltech.qtcppproject.properties.qtprochangelistener"));
			return (value != null) && (value.equals("true"));
		} catch (CoreException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public void setRunQMakeWhenProFileChanges(boolean enable) {
		try {
			this.wrapped
					.setPersistentProperty(
							new QualifiedName("",
									"com.trolltech.qtcppproject.properties.qtprochangelistener"),
							enable ? "true" : "false");
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void updateQtDir(String oldBinPath, String oldIncludePath) {
		try {
			if (!this.wrapped.hasNature("com.trolltech.qtcppproject.QtNature")) {
				return;
			}
			if (!this.wrapped.hasNature(MakeProjectNature.NATURE_ID)) {
				return;
			}
			String qtBinPath = getQtBinPath();
			String qtIncludePath = getQtIncludePath();
			if ((qtBinPath == null) || (qtIncludePath == null))
				return;
			String mkspec = QtUtils.getMakeSpec(new Path(qtBinPath)
					.removeLastSegments(1).toOSString());
			boolean setmkcmd = QtPreferencePage.getAutoSetMkCmd();
			String mkcmd = null;
			if (setmkcmd) {
				mkcmd = QtUtils.getMakeCommand(mkspec);
			}
			IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(
					this.wrapped, MakeBuilder.BUILDER_ID);
			Map env = info.getEnvironment();

			if ((QtPreferencePage.getAutoSetMkSpec()) && (mkspec != null)) {
				env.put("QMAKESPEC", mkspec);
			}
			String path = QtUtils.createPath((String) env.get("PATH"),
					qtBinPath, oldBinPath);

			env.put("PATH", path);
			info.setEnvironment(env);

			if (setmkcmd) {
				info.setBuildAttribute(IMakeBuilderInfo.BUILD_COMMAND, mkcmd);

				info.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO,
						"debug");
				info.setBuildAttribute(
						IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, "debug");
			}

			IMakeTargetManager targetManager = MakeCorePlugin.getDefault()
					.getTargetManager();
			String[] builders = targetManager.getTargetBuilders(this.wrapped);
			for (int i = 0; i < builders.length; i++) {
				IMakeTarget target = targetManager.findTarget(this.wrapped,
						"Qt Release Build");
				if (target != null) {
					if (setmkcmd)
						target.setBuildAttribute(IMakeTarget.BUILD_COMMAND,
								mkcmd);
					target.setEnvironment(env);
				}
				target = targetManager.findTarget(this.wrapped,
						"Qt Debug Build");
				if (target != null) {
					if (setmkcmd)
						target.setBuildAttribute(IMakeTarget.BUILD_COMMAND,
								mkcmd);
					target.setEnvironment(env);
				}

			}

			QtLaunchConfig.updateLaunchPaths(this.wrapped, qtBinPath,
					oldBinPath);

			updateQtIncludeDir(qtIncludePath, oldIncludePath);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void scheduleRebuild() {
		final IProject project = this.wrapped;

		WorkspaceJob cleanJob = new WorkspaceJob("Clean " + project.getName()) {
			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
			}

			public IStatus runInWorkspace(IProgressMonitor monitor) {
				try {
					project.build(15, monitor);

					WorkspaceJob buildJob = new WorkspaceJob("Build "
							+ project.getName()) {
						public boolean belongsTo(Object family) {
							return ResourcesPlugin.FAMILY_MANUAL_BUILD
									.equals(family);
						}

						public IStatus runInWorkspace(IProgressMonitor monitor) {
							try {
								project.build(10, monitor);
							} catch (CoreException e) {
							}
							return Status.OK_STATUS;
						}
					};
					buildJob.setRule(project.getWorkspace().getRuleFactory()
							.buildRule());

					buildJob.setUser(true);
					buildJob.schedule();
				} catch (CoreException e) {
				}
				return Status.OK_STATUS;
			}

		};
		cleanJob.setRule(project.getWorkspace().getRuleFactory().buildRule());

		cleanJob.setUser(true);
		cleanJob.schedule();
	}

	public void convertToQtProject(IProgressMonitor monitor)
			throws CoreException {
		if (!this.wrapped.hasNature(MakeProjectNature.NATURE_ID)) {
			CCorePlugin.getDefault().convertProjectToCC(this.wrapped, monitor,
					MakeCorePlugin.MAKE_PROJECT_ID);
		}
		if (!this.wrapped.hasNature(MakeProjectNature.NATURE_ID))
			MakeProjectNature.addNature(this.wrapped, new SubProgressMonitor(
					monitor, 1));
		if (!this.wrapped.hasNature(ScannerConfigNature.NATURE_ID)) {
			ScannerConfigNature.addScannerConfigNature(this.wrapped);
			IScannerConfigBuilderInfo2 scannerConfig = ScannerConfigProfileManager
					.createScannerConfigBuildInfo2(this.wrapped,
							"org.eclipse.cdt.make.core.GCCStandardMakePerProjectProfile");

			scannerConfig.save();
		}
		addQtNature(monitor);
	}

	private void addQtNature(IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = this.wrapped.getDescription();
		String[] natures = description.getNatureIds();

		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[(natures.length + 0)] = "com.trolltech.qtcppproject.QtNature";

		description.setNatureIds(newNatures);
		this.wrapped.setDescription(description, monitor);
		configureMake();
	}

	private void updateQtIncludeDir(String newQtIncludePath,
			String oldQtIncludePath) {
		try {
			Vector v = new Vector();
			IPathEntryStore store = CoreModel.getPathEntryStore(this.wrapped);

			File newDirectory = new File(newQtIncludePath);
			String[] includePaths;
			if ((newDirectory.canRead()) && (newDirectory.isDirectory())) {
				File[] subDirectories = newDirectory
						.listFiles(new FileFilter() {
							public boolean accept(File pathname) {
								if ((pathname.canRead())
										&& (pathname.isDirectory()))
									return true;
								return false;
							}
						});
				includePaths = new String[subDirectories.length + 1];
				includePaths[0] = "";
				for (int i = 0; i < subDirectories.length; i++)
					includePaths[(i + 1)] = subDirectories[i].getName();
			} else {
				includePaths = QT_INCLUDE_PATHS;
			}
			for (int i = 0; i < includePaths.length; i++) {
				IIncludeEntry pathEntry = CoreModel.newIncludeEntry(
						new Path(""), new Path(newQtIncludePath), new Path(
								includePaths[i]));

				v.add(pathEntry);
			}

			IPathEntry[] paths = store.getRawPathEntries();
			for (int i = 0; i < paths.length; i++) {
				if ((paths[i] instanceof IIncludeEntry)) {
					String oldIndexerIncludePath = ((IIncludeEntry) paths[i])
							.getFullIncludePath().toOSString();
					if (((oldQtIncludePath != null) && (oldIndexerIncludePath
							.startsWith(oldQtIncludePath)))
							|| (oldIndexerIncludePath
									.startsWith(newQtIncludePath)))
						;
				} else {
					v.add(paths[i]);
				}
			}
			store.setRawPathEntries((IPathEntry[]) v.toArray(new IPathEntry[v
					.size()]));
		} catch (CoreException e) {
		}
	}

	private void configureMake() {
		try {
			IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(
					this.wrapped, MakeBuilder.BUILDER_ID);
			info.setAppendEnvironment(true);
			info.setUseDefaultBuildCmd(false);
			info.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, "");
			info.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL,
					"");
		} catch (CoreException e) {
			e.printStackTrace();
		}

		if (Platform.getOS().equals("win32")) {
			setBinaryParser("org.eclipse.cdt.core.PE");
		}
		createQtTargets();
		updateQtDir("", "");
	}

	private void setBinaryParser(final String id) {
		ICDescriptorOperation op = new ICDescriptorOperation() {

			public void execute(ICDescriptor descriptor,
					IProgressMonitor monitor) throws CoreException {
				descriptor.create("org.eclipse.cdt.core.BinaryParser", id);
			}
		};
		try {
			CCorePlugin.getDefault().getCDescriptorManager()
					.runDescriptorOperation(this.wrapped, op, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void createQtTargets() {
		MakeCorePlugin makecore = MakeCorePlugin.getDefault();
		IMakeTargetManager targetManager = makecore.getTargetManager();
		try {
			String[] builders = targetManager.getTargetBuilders(this.wrapped);
			for (int i = 0; i < builders.length; i++) {
				IMakeTarget target = targetManager.findTarget(this.wrapped,
						"Qt Release Build");
				if (target == null) {
					target = targetManager.createTarget(this.wrapped,
							"Qt Release Build", builders[i]);
					targetManager.addTarget(target);
				}
				initTarget(target, "release");

				target = targetManager.findTarget(this.wrapped,
						"Qt Debug Build");
				if (target == null) {
					target = targetManager.createTarget(this.wrapped,
							"Qt Debug Build", builders[i]);
					targetManager.addTarget(target);
				}
				initTarget(target, "debug");
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private static void initTarget(IMakeTarget target, String config) {
		try {
			target.setBuildAttribute(IMakeTarget.BUILD_TARGET, config);
			target.setUseDefaultBuildCmd(false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}