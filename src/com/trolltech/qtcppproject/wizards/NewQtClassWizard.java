package com.trolltech.qtcppproject.wizards;

import com.trolltech.qtcppproject.wizards.pages.UiFileWizardPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class NewQtClassWizard extends AbstractQtClassWizard
{
  private UiFileWizardPage m_uipage;

  public NewQtClassWizard()
  {
    setWindowTitle("New Qt Gui Class");
  }

  public void addPages() {
    super.addPages();

    this.m_uipage = new UiFileWizardPage(this.m_selection);
    addPage(this.m_uipage);
  }

  protected void createFiles(IProgressMonitor monitor) throws InterruptedException, CoreException {
    this.m_uipage.createFiles(monitor);
  }
}