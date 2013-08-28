package com.trolltech.qtcppproject;

import com.trolltech.qtcppproject.qmake.IQMakeEnvironmentModifier;
import com.trolltech.qtcppproject.utils.QtUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.BundleContext;

public class QtProjectPlugin extends AbstractUIPlugin {
	private static QtProjectPlugin plugin;
	private ProFileChangedListener proFileListener = new ProFileChangedListener();
	private Map<String, Boolean> filePathToQObjectMacro;
	private Map<String, String> filePathToProjectPath;
	private Map<String, Boolean> projectPathToRunQMake;
	private static List<IQMakeEnvironmentModifier> envModifiers = null;
	public static final String TEMPLATE_LOCATION = "/com/trolltech/qtcppproject/wizards/templates/";
	public static final String PLUGIN_ID = "com.trolltech.qtcppproject";

	public QtProjectPlugin() {
		plugin = this;
		this.filePathToQObjectMacro = new HashMap();
		this.filePathToProjectPath = new HashMap();
		this.projectPathToRunQMake = new HashMap();
	}

	public boolean isRunningQMakeRequest(String projectPath) {
		Boolean run = (Boolean) this.projectPathToRunQMake.get(projectPath);
		if ((run != null) && (run.booleanValue() == true))
			return true;
		return false;
	}

	public void clearRunningQMakeRequest(String projectPath) {
		this.projectPathToRunQMake.remove(projectPath);
	}

	private boolean isEndingQuote(String contents, int quoteIndex) {
		boolean endingQuote = true;
		if (quoteIndex > 0) {
			int previous = 1;
			while (contents.charAt(quoteIndex - previous) == '\\') {
				previous++;
				endingQuote = !endingQuote;
			}
		}
		return endingQuote;
	}

	private boolean hasQObjectMacro(String contents) {
		int idx = 0;
		int count = contents.length();
		while ((idx < count) && (idx >= 0)) {
			int macroIdx = contents.indexOf("Q_OBJECT", idx);
			if (macroIdx == -1)
				return false;
			if (macroIdx == idx)
				return true;
			if (contents.indexOf("//", idx) == idx) {
				idx = contents.indexOf('\n', idx + 2) + 1;
			} else if (contents.indexOf("/*", idx) == idx) {
				idx = contents.indexOf("*/", idx + 2) + 2;
			} else if (contents.indexOf("'\\\"'", idx) == idx) {
				idx += 4;
			} else if (contents.charAt(idx) == '"') {
				do
					idx = contents.indexOf('"', idx + 1);
				while ((idx > 0) && (!isEndingQuote(contents, idx)));
				if (idx < 0)
					return false;
				idx++;
			} else {
				idx++;
			}
		}
		return false;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				this.proFileListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				new IResourceChangeListener() {
					private List<IFile> changedFiles = new ArrayList();

					public void resourceChanged(IResourceChangeEvent event) {
						if (event.getType() != 1)
							return;
						this.changedFiles.clear();
						visitChildren(event.getDelta());
						for (IFile changedFile : this.changedFiles) {
							String changedFilePath = changedFile.getLocation()
									.toOSString();
							if (QtProjectPlugin.this.filePathToProjectPath
									.containsKey(changedFilePath)) {
								boolean hadMacro = ((Boolean) QtProjectPlugin.this.filePathToQObjectMacro
										.get(changedFilePath)).booleanValue();
								boolean hasMacro = QtProjectPlugin.this
										.hasQObjectMacro(getContents(new FileEditorInput(
												changedFile)));
								if (hadMacro != hasMacro) {
									QtProjectPlugin.this.filePathToQObjectMacro
											.put(changedFilePath,
													Boolean.valueOf(hasMacro));
									QtProjectPlugin.this.projectPathToRunQMake
											.put(QtProjectPlugin.this.filePathToProjectPath
													.get(changedFilePath),
													Boolean.valueOf(true));
								}
							}
						}
						this.changedFiles.clear();
					}

					private String getContents(IFileEditorInput input) {
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						IEditorPart part = page.findEditor(input);
						if ((part instanceof TextEditor)) {
							TextEditor te = (TextEditor) part;
							IDocumentProvider provider = te
									.getDocumentProvider();
							IDocument document = provider.getDocument(te
									.getEditorInput());
							String contents = document.get();
							return contents;
						}
						return null;
					}

					private void visitChildren(IResourceDelta delta) {
						IResourceDelta[] changedChildren = delta
								.getAffectedChildren(4);
						if (changedChildren.length > 0)
							for (IResourceDelta child : changedChildren) {
								if ((child.getFlags() & 0x20000) == 0) {
									IResource resource = child.getResource();
									if (resource != null)
										if ((resource instanceof IFile))
											this.changedFiles
													.add((IFile) resource);
										else if ((resource instanceof IFolder))
											visitChildren(child);
										else if ((resource instanceof IProject))
											visitChildren(child);
								}
							}
					}
				});
		IWorkbench workbench = PlatformUI.getWorkbench();

		workbench.addWindowListener(new IWindowListener() {
			public void windowActivated(IWorkbenchWindow window) {
			}

			public void windowClosed(IWorkbenchWindow window) {
			}

			public void windowDeactivated(IWorkbenchWindow window) {
			}

			public void windowOpened(IWorkbenchWindow window) {
				QtProjectPlugin.this.addPartListener(window);
			}
		});
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (IWorkbenchWindow window : windows)
			addPartListener(window);
	}

	public void addPartListener(IWorkbenchWindow window) {
		IPartService service = window.getPartService();
		service.addPartListener(new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
			}

			private String getContents(IWorkbenchPart part) {
				if ((part instanceof TextEditor)) {
					TextEditor te = (TextEditor) part;
					IDocumentProvider provider = te.getDocumentProvider();
					IDocument document = provider.getDocument(te
							.getEditorInput());
					String contents = document.get();
					return contents;
				}
				return null;
			}

			private IFileEditorInput getEditorInput(IWorkbenchPart part) {
				if ((part instanceof TextEditor)) {
					TextEditor te = (TextEditor) part;
					IEditorInput ei = te.getEditorInput();

					if ((QtUtils.isQtProject(QtUtils.qtProjectOfEditorInput(ei)))
							&& ((ei instanceof IFileEditorInput))) {
						IFileEditorInput fei = (IFileEditorInput) ei;
						return fei;
					}
				}

				return null;
			}

			private String getFilePath(IFileEditorInput fei) {
				if (fei != null) {
					return fei.getFile().getLocation().toOSString();
				}
				return null;
			}

			private String getFilePath(IWorkbenchPart part) {
				IFileEditorInput fei = getEditorInput(part);
				if (fei != null) {
					return getFilePath(fei);
				}
				return null;
			}

			private void storeQObjectMacroData(IWorkbenchPart part) {
				IFileEditorInput fei = getEditorInput(part);
				String filePath = getFilePath(fei);
				if (filePath != null) {
					String proFilePath = QtUtils.findProFile(
							fei.getFile().getProject()).toOSString();
					if (filePath.equals(proFilePath)) {
						return;
					}

					if (!QtProjectPlugin.this.filePathToQObjectMacro
							.containsKey(filePath)) {
						IProject project = QtUtils.qtProjectOfEditorInput(fei);
						IPath proPath = QtUtils.findProFile(project);
						if (proPath != null) {
							QtProjectPlugin.this.filePathToProjectPath.put(
									filePath, proPath.toOSString());
							QtProjectPlugin.this.filePathToQObjectMacro.put(
									filePath,
									Boolean.valueOf(QtProjectPlugin.this
											.hasQObjectMacro(getContents(part))));
						}
					}
				}
			}

			public void partBroughtToTop(IWorkbenchPart part) {
				storeQObjectMacroData(part);
			}

			public void partClosed(IWorkbenchPart part) {
				IFileEditorInput fei = getEditorInput(part);
				if (fei != null) {
					IWorkbenchWindow window = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					if (window == null)
						return;
					IWorkbenchPage page = window.getActivePage();
					if (page == null)
						return;
					IEditorReference[] refs = page.findEditors(fei, null, 1);

					if (refs.length == 0) {
						String filePath = getFilePath(fei);

						QtProjectPlugin.this.filePathToQObjectMacro
								.remove(filePath);
						QtProjectPlugin.this.filePathToProjectPath
								.remove(filePath);
					}
				}
			}

			public void partDeactivated(IWorkbenchPart part) {
			}

			public void partOpened(IWorkbenchPart part) {
				storeQObjectMacroData(part);
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
				this.proFileListener);
		plugin = null;
	}

	public static QtProjectPlugin getDefault() {
		return plugin;
	}

	public String getDefaultQtVersion() {
		return getDefault().getPreferenceStore().getString(
				"com.trolltech.qtcppproject.qtversiondefault");
	}

	public void addDefaultQtVersion(String versionName, IPath binDir,
			IPath incDir) {
		IPreferenceStore store = getPreferenceStore();

		int count = store.getInt("com.trolltech.qtcppproject.qtversioncount");
		store.setValue("com.trolltech.qtcppproject.qtversioncount", count + 1);

		store.setValue(
				"com.trolltech.qtcppproject.qtversionname."
						+ Integer.toString(count), versionName);
		store.setValue(
				"com.trolltech.qtcppproject.qtversionbinpath."
						+ Integer.toString(count), binDir.toOSString());
		store.setValue("com.trolltech.qtcppproject.qtversionincludepath."
				+ Integer.toString(count), incDir.toOSString());

		store.setValue("com.trolltech.qtcppproject.qtversiondefault",
				versionName);
	}

	public List<IQMakeEnvironmentModifier> getEnvironmentModifierExtensions() {
		if (envModifiers == null) {
			envModifiers = new ArrayList();

			IExtensionRegistry extensionRegistry = Platform
					.getExtensionRegistry();
			IExtensionPoint extensionPoint = extensionRegistry
					.getExtensionPoint("com.trolltech.qtcppproject.qmakeEnvironmentModifier");
			IExtension[] extensions = extensionPoint.getExtensions();

			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] elements = extension
						.getConfigurationElements();
				IConfigurationElement element = elements[0];

				boolean failed = false;
				try {
					Object extObject = element
							.createExecutableExtension("class");
					if ((extObject instanceof IQMakeEnvironmentModifier))
						envModifiers.add((IQMakeEnvironmentModifier) extObject);
					else
						failed = true;
				} catch (CoreException e) {
					failed = true;
				}

				if (failed) {
					logErrorMessage("Unable to load qmakeEnvironmentModifier extension from "
							+ extension.getContributor().getName());
				}
			}
		}

		return envModifiers;
	}

	public void logErrorMessage(String errMsg) {
		ILog log = plugin.getLog();
		if (log != null)
			log.log(new Status(4, "com.trolltech.qtcppproject", errMsg));
	}
}