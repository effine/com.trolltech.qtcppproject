package com.trolltech.qtcppproject;

import com.trolltech.qtcppproject.launch.QtLaunchConfig;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IStartup;

public class QtStartup implements IStartup {
	public void earlyStartup() {
		new QtProjectMonitor();

		ILaunchManager launchManager = DebugPlugin.getDefault()
				.getLaunchManager();
		QtLaunchConfig qlc = new QtLaunchConfig();
		launchManager.addLaunchConfigurationListener(qlc);
	}
}