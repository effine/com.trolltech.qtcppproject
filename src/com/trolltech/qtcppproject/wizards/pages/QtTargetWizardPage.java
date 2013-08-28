package com.trolltech.qtcppproject.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.trolltech.qtcppproject.utils.TargetPlatformType;

/**
 * @author arno
 * @version 2013-4-15 ÉÏÎç9:48:06
 */

public class QtTargetWizardPage extends WizardPage {

	private String fatherTarget;
	private String childTarget;
	TargetPlatformType type = new TargetPlatformType();

	public String getFatherTarget() {
		return fatherTarget;
	}

	public void setFatherTarget(String fatherTarget) {
		this.fatherTarget = fatherTarget;
	}

	public String getChildTarget() {
		return childTarget;
	}

	public void setChildTarget(String childTarget) {
		this.childTarget = childTarget;
	}

	public QtTargetWizardPage(String pageName) {
		super(pageName);
		setTitle("Qt Target Platform");
		setDescription("Select compile target platform .");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);

		Label lblTargetsPlatform = new Label(container, SWT.NONE);
		lblTargetsPlatform.setBounds(37, 36, 113, 17);
		lblTargetsPlatform.setText("Targets Platform :");

		final Combo fatherCmb = new Combo(container, SWT.READ_ONLY);
		fatherCmb.setBounds(157, 33, 278, 25);
		String[] item = type.FATHERNAME;
		fatherCmb.setItems(item);

		final Combo childCmb = new Combo(container, SWT.READ_ONLY);
		childCmb.setBounds(157, 85, 278, 25);
		final String[] wItem = type.wPlatform;
		final String[] lItem = type.lPlatform;
		final String[] vItem = type.vPlatform;

		if ("Windows 7".equals(type.getOS())) {
			fatherCmb.select(3);
			childCmb.setItems(wItem);
			childCmb.select(0);
		}

		fatherCmb.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fatherTarget = fatherCmb.getText();
				if ("win32".equals(fatherTarget)) {
					childCmb.setItems(wItem);
					childCmb.select(0);
				} else if ("linux".equals(fatherTarget)) {
					childCmb.setItems(lItem);
					childCmb.select(0);
				} else if ("VxWorks".equals(fatherTarget)) {
					childCmb.setItems(vItem);
					childCmb.select(0);
				}
			}
		});

		childCmb.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				childTarget = childCmb.getText();
			}
		});
	}
}
