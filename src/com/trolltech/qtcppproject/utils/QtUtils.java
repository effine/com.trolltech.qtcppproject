package com.trolltech.qtcppproject.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Vector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

public class QtUtils {
	public static final int QT_PATH_TYPE_BIN = 0;
	public static final int QT_PATH_TYPE_INCLUDE = 1;

	public static String getMakeSpec(String qtdir) {
		try {
			Path qmakecash = new Path(qtdir + "/.qmake.cache");
			BufferedReader in = new BufferedReader(new FileReader(
					qmakecash.toOSString()));
			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				if (line.startsWith("QMAKESPEC")) {
					String[] args = line.split("=");
					if (args.length == 2) {
						line = args[1].trim();
						line = line.replace('\\', '/');
						int lastindex = line.lastIndexOf('/');
						if (lastindex == -1)
							return line.trim();
						return line.substring(lastindex + 1).trim();
					}
				}
			}

		} catch (IOException e) {
		}

		return null;
	}

	public static String getMakeCommand(String mkspec) {
		if (mkspec != null) {
			if (mkspec.endsWith("win32-g++"))
				return "mingw32-make";
			if (mkspec.indexOf("win32") > -1) {
				return "nmake";
			}
		}

		return "make";
	}

	public static IPath removeFileName(IPath path) {
		if (path == null)
			return null;
		if (path.hasTrailingSeparator())
			return path;
		return path.removeLastSegments(1);
	}

	public static String getFileName(IPath path, boolean noextension) {
		if (path == null)
			return null;
		if (path.hasTrailingSeparator())
			return "";
		if (noextension) {
			path = path.removeFileExtension();
		}
		return path.lastSegment();
	}

	public static IPath getCanonicalPath(IPath path) {
		IPath result = new Path("");
		for (int i = 0; i < path.segmentCount(); i++) {
			String segment = path.segment(i);
			if (!segment.equals(".")) {
				if ((segment.equals("..")) && (result.segmentCount() > 0))
					result = result.removeLastSegments(1);
				else
					result = result.append(segment + "/");
			}
		}
		if (!path.hasTrailingSeparator())
			result = result.removeTrailingSeparator();
		if (path.isAbsolute())
			result = result.makeAbsolute();
		return result;
	}

	public static IPath makeRelative(IPath path, IPath base) {
		IPath dir = getCanonicalPath(path);
		IPath baseDir = getCanonicalPath(base);
		if (!dir.hasTrailingSeparator())
			dir = dir.removeLastSegments(1);
		if (!baseDir.hasTrailingSeparator())
			baseDir = baseDir.removeLastSegments(1);
		int common = dir.matchingFirstSegments(baseDir);
		dir = dir.removeFirstSegments(common);
		IPath relative = new Path("");
		for (int i = 0; i < baseDir.segmentCount() - common; i++)
			relative = relative.append("../");
		relative = relative.append(dir);
		relative = relative.append(getFileName(path, false));
		return relative;
	}

	public static boolean isFormFile(String filename) {
		return filename.toLowerCase(Locale.US).endsWith(".ui");
	}

	public static boolean isResourceFile(String filename) {
		return filename.toLowerCase(Locale.US).endsWith(".qrc");
	}

	public static boolean isGeneratedHeaderFile(String filename) {
		return filename.toLowerCase(Locale.US).startsWith("ui_");
	}

	public static boolean isGeneratedSourceFile(String filename) {
		return (filename.toLowerCase(Locale.US).startsWith("qrc_"))
				|| (filename.toLowerCase(Locale.US).startsWith("moc_"));
	}

	public static IPath findProFile(IProject pro) {
		QtProjectVisitor visitor = new QtProjectVisitor();
		Vector result = visitor.findFiles(pro, "pro");
		if (result.size() <= 0)
			return null;
		IPath path = ((IResource) result.get(0)).getLocation();
		for (int i = 0; i < result.size(); i++)
			if (((IResource) result.get(i)).getProjectRelativePath()
					.segmentCount() == 1)
				path = ((IResource) result.get(i)).getLocation();
		return path;
	}

	public static String createPath(String path, String prepend, String remove) {
		Vector result = new Vector();
		result.add(prepend);

		if (remove != null) {
			result.add(remove);
		}

		result.add("${env_var:PATH}");

		String splitchar = ";";
		if (!Platform.getOS().equals("win32")) {
			splitchar = ":";
		}
		if (path != null) {
			String[] pathlist = path.split(splitchar);
			for (int i = 0; i < pathlist.length; i++) {
				if (!result.contains(pathlist[i])) {
					result.add(pathlist[i]);
				}
			}
		}
		if (remove != null) {
			result.remove(remove);
		}
		path = prepend;
		for (int i = 1; i < result.size(); i++) {
			path = path + splitchar + (String) result.get(i);
		}

		return path;
	}

	public static IProject[] getQtProjects() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		Vector qtProjects = new Vector();
		for (int i = 0; i < allProjects.length; i++)
			try {
				if (allProjects[i]
						.hasNature("com.trolltech.qtcppproject.QtNature"))
					qtProjects.add(allProjects[i]);
			} catch (CoreException ex) {
			}
		IProject[] result = new IProject[qtProjects.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = ((IProject) qtProjects.elementAt(i));
		return result;
	}

	public static String getQmakeExecutableName() {
		return System.getProperty("os.name").indexOf("Windows") == 0 ? "qmake.exe"
				: "qmake";
	}

	public static String getQtPathLastSegment(int qtPathType) {
		return qtPathType == 0 ? "bin" : "include";
	}

	public static boolean isValidQtPath(String path, int qtPathType) {
		String subElement = qtPathType == 0 ? getQmakeExecutableName()
				: "QtCore";
		boolean subElementMustBeDirectory = qtPathType == 1;
		Path subElementPath = new Path(path);
		String subElementOSString = subElementPath.append(subElement)
				.toOSString();

		File subElementFile = new File(subElementOSString);
		return (subElementFile.exists())
				&& (subElementFile.isDirectory() == subElementMustBeDirectory);
	}

	public static String getSiblingQtPath(String sourcePath, int sourceType,
			int targetType) {
		String result = "";
		if (isValidQtPath(sourcePath, sourceType)) {
			Path qtPath = new Path(new Path(sourcePath).removeLastSegments(1)
					.toOSString());
			String targetPathOSString = qtPath.append(
					getQtPathLastSegment(targetType)).toOSString();
			if (isValidQtPath(targetPathOSString, targetType))
				result = targetPathOSString;
		}
		return result;
	}

	public static String getQtSubPathUnderQtPath(String qtPath, int subPathType) {
		String result = "";
		if (new File(qtPath).exists()) {
			Path subPath = new Path(new Path(qtPath).append(
					getQtPathLastSegment(subPathType)).toOSString());
			String subPathOSString = subPath.toOSString();
			if (isValidQtPath(subPathOSString, subPathType))
				result = subPathOSString;
		}
		return result;
	}

	public static boolean isQtProject(IProject project) {
		if (project == null)
			return false;
		try {
			return project.hasNature("com.trolltech.qtcppproject.QtNature");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static IProject qtProjectOfEditorInput(IEditorInput input) {
		if ((input instanceof IFileEditorInput)) {
			IResource resource = ((IFileEditorInput) input).getFile();
			IProject project = resource.getProject();

			if ((project != null) && (isQtProject(project))) {
				return project;
			}
		}
		return null;
	}
}