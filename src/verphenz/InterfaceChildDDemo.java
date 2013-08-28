package verphenz;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author arno
 * @version 2013-4-24 обнГ5:23:22
 */

public class InterfaceChildDDemo extends InterfaceFatherDemo {

	@Override
	public void projectCreated(IProject iproject,
			IProgressMonitor iprogressmonitor) {
	}

	@Override
	public void test() {

		re = "reer";
	}

}
