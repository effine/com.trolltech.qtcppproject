package com.trolltech.qtcppproject.utils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author arno
 * @version 2013-6-6 上午10:34:48
 */

public class TargetPlatformType {

	// 目标平台 所处文件夾路径
	private static String fatherPath = "C:\\FileTest";
	private static String childPath = fatherPath + "C:\\mkspecs";

	// 定义多个数组，存放Father类别下的目标平台名字
	public static String[] wPlatform; // windows平台
	public static String[] lPlatform; // Linux平台
	public static String[] vPlatform; // VxWorks平台
	public static String[] delArray = { "common", "features" }; // 目标文件夹下需要删除的目录

	// 选择目标平台：大范围的分类
	public static String[] FATHERNAME = getDirectoryName(fatherPath);

	// 构造方法 给childPlatform 变量赋值
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

	// 获取目录下所有的目录
	public static String[] getDirectoryName(String path) {
		File file = new File(path);
		String[] directory = file.list();
		return directory;
	}

	// 方便删除平台目录下的common features
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

	// 判断当前操作系统
	public static String getOS() {
		return System.getProperty("os.name");
	}
}
