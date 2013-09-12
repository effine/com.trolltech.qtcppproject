package com.trolltech.qtcppproject.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author arno
 * @version 2013-6-6 ����10:34:48
 */

public class TargetPlatformType {

	private static String fatherPath = "C:\\FileTest";
	private static String childPath = fatherPath + "C:\\mkspecs";

	public static String[] wPlatform; // windows
	public static String[] lPlatform; // Linux
	public static String[] vPlatform; // VxWorks
	public static String[] delArray = { "common", "features" };

	public static String[] FATHERNAME = getDirectoryName(fatherPath);

	public TargetPlatformType() {

		String wpath, lpath, vpath;

		for (String str : FATHERNAME) {
			if ("win32".trim().equals(str)) {
				wpath = fatherPath + "\\win32\\mkspecs";
				wPlatform = arrContrast(getDirectoryName(wpath), delArray);
			}

			if ("linux".trim().equals(str)) {
				lpath = fatherPath + "\\linux\\mkspecs";
				lPlatform = arrContrast(getDirectoryName(lpath), delArray);
			}

			if ("VxWorks".trim().equals(str)) {
				vpath = fatherPath + "\\VxWorks\\mkspecs";
				vPlatform = arrContrast(getDirectoryName(vpath), delArray);
			}
		}

	}

	public static String[] getDirectoryName(String path) {
		File file = new File(path);
		String[] directory = file.list();
		return directory;
	}

	private static String[] arrContrast(String[] arr1, String[] arr2) {
		List<String> list = new LinkedList<String>();
		for (String str : arr1) {
			if (!list.contains(str)) {
				list.add(str);
			}
		}
		for (String str : arr2) {
			if (list.contains(str)) {
				list.remove(str);
			}
		}
		String[] result = {};
		return list.toArray(result);
	}

	public static String getOS() {
		return System.getProperty("os.name");
	}
}
