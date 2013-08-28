package verphenz;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author arno
 * @version 2013-4-26 …œŒÁ10:36:55
 */

public class EasyStartup implements IStartup {
	public void earlyStartup() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Shell[] shells = Display.getCurrent().getShells();
				System.out.println(shells.length);
				for (Shell _shell : shells) {
					if (_shell.getData() instanceof IWorkbenchWindow) {
						System.out.println("’“µΩ¡À£°");
					}
				}

				System.out.println("hello start");
			}
		});
	}

}
