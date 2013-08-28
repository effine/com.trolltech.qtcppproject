package verphenz;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author arno
 * @version 2013-4-24 обнГ5:22:07
 */

public interface ITest {
	public void projectCreated(IProject iproject,
			IProgressMonitor iprogressmonitor);

	public void test();
}
