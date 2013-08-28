package verphenz;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author arno
 * @version 2013-4-23 ионГ10:56:32
 */

public class PreferencePageDemo extends PreferencePage implements
		IWorkbenchPreferencePage {
	public PreferencePageDemo() {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, 0);
		return null;
	}

	@Override
	public void init(IWorkbench arg0) {
	}

}
