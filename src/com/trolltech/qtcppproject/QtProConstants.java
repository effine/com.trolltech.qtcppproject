package com.trolltech.qtcppproject;

import org.eclipse.swt.graphics.RGB;

public abstract interface QtProConstants {
	public static final String FILES_BEGIN_TAG = "# ECLIPSE_PROJECT_FILES_BEGIN";
	public static final String FILES_END_TAG = "# ECLIPSE_PROJECT_FILES_END";
	public static final String SETTINGS_BEGIN_TAG = "# ECLIPSE_PROJECT_SETTINGS_BEGIN";
	public static final String SETTINGS_END_TAG = "# ECLIPSE_PROJECT_SETTINGS_END";
	public static final String QTBUILDER_ID = "com.trolltech.qtcppproject.QtMakefileGenerator";
	public static final String QTNATURE_ID = "com.trolltech.qtcppproject.QtNature";
	public static final RGB PRO_SETTINGS_COLOR = new RGB(188, 188, 188);
	public static final RGB PRO_SOURCE_COLOR = new RGB(188, 188, 188);
	public static final RGB PRO_COMMENT_COLOR = new RGB(63, 127, 95);
	public static final RGB PRO_DEFAULT_COLOR = new RGB(0, 0, 255);
	public static final int NoModules = 0;
	public static final int QtCore = 1;
	public static final int QtGui = 2;
	public static final int QtSql = 4;
	public static final int QtXml = 8;
	public static final int QtXmlPatterns = 16;
	public static final int QtNetwork = 32;
	public static final int QtSvg = 64;
	public static final int QtOpenGL = 128;
	public static final int QtWebKit = 256;
	public static final int QtScript = 512;
	public static final int QtTest = 1024;
	public static final int QtHelp = 2048;
	public static final int Qt3Support = 4096;
}