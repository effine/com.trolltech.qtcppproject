package com.trolltech.qtcppproject.wizards.pages;

import com.trolltech.qtcppproject.utils.QtUtils;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class QtVersionWizardPage extends WizardPage
{
  Text versionName;
  DirectoryFieldEditor binPath;
  DirectoryFieldEditor includePath;

  public QtVersionWizardPage(String pageName)
  {
    super(pageName);
    setTitle("Qt Version");
    setDescription("Specify the Name and Bin + Include Pathes of the Qt version.");
  }

  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, 0);

    GridLayout gl = new GridLayout();
    int ncol = 3;
    gl.numColumns = ncol;
    composite.setLayout(gl);

    Label versionNameLabel = new Label(composite, 0);
    versionNameLabel.setText("Version Name:");
    this.versionName = new Text(composite, 2052);
    this.versionName.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        QtVersionWizardPage.this.dialogChanged();
      }
    });
    GridData versionNameGridData = new GridData(768);
    this.versionName.setLayoutData(versionNameGridData);
    new Label(composite, 0);
    new Label(composite, 0);
    new Label(composite, 0);
    new Label(composite, 0);

    this.binPath = new DirectoryFieldEditor("bin", "Bin Path:", composite);

    this.binPath.getTextControl(composite).addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        QtVersionWizardPage.this.dialogChanged();
      }
    });
    this.binPath.setPropertyChangeListener(new IPropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
        QtVersionWizardPage.this.pathSelected(0);
      }
    });
    new Label(composite, 0);
    this.versionName.setLayoutData(versionNameGridData);
    Label binPathDescription = new Label(composite, 0);
    binPathDescription.setText("Path containing tools 'qmake', 'uic', 'rcc', etc.\n");
    new Label(composite, 0);
    new Label(composite, 0);
    new Label(composite, 0);
    new Label(composite, 0);

    this.includePath = new DirectoryFieldEditor("include", "Include Path:", composite);

    this.includePath.getTextControl(composite).addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        QtVersionWizardPage.this.dialogChanged();
      }
    });
    this.includePath.setPropertyChangeListener(new IPropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
        QtVersionWizardPage.this.pathSelected(1);
      }
    });
    new Label(composite, 0);
    Label includePathDescription = new Label(composite, 0);
    includePathDescription.setText("Path containing the include pathes 'QtCore', 'QtGui', etc.");
    new Label(composite, 0);

    setControl(composite);
    setPageComplete(isValid());
  }

  private boolean isVersionNameValid() {
    return this.versionName.getText().length() > 0;
  }

  private boolean isBinPathValid() {
    return QtUtils.isValidQtPath(this.binPath.getStringValue(), 0);
  }

  private boolean isIncludePathValid() {
    return QtUtils.isValidQtPath(this.includePath.getStringValue(), 1);
  }

  private boolean isValid() {
    return (isVersionNameValid()) && (isBinPathValid()) && (isIncludePathValid());
  }

  public void dialogChanged() {
    boolean isValid = isValid();

    if (!isValid) {
      String errorMessage = "";
      if (!isVersionNameValid())
        errorMessage = errorMessage + "Version Name is empty. ";
      if (!isBinPathValid())
        errorMessage = errorMessage + "Bin Path is invalid. ";
      if (!isIncludePathValid())
        errorMessage = errorMessage + "Include Path is invalid. ";
      setErrorMessage(errorMessage);
    } else {
      setErrorMessage(null);
    }

    setPageComplete(isValid);
  }

  public void pathSelected(int qtPathType) {
    int siblingPathType = qtPathType == 0 ? 1 : 0;
    String selectedPathEntry = (qtPathType == 0 ? this.binPath : this.includePath).getStringValue();

    String siblingPathEntry = (qtPathType == 0 ? this.includePath : this.binPath).getStringValue();

    boolean selectedPathEntryIsValid = QtUtils.isValidQtPath(selectedPathEntry, qtPathType);
    boolean siblingPathEntryIsEmpty = siblingPathEntry.length() == 0;

    if (!selectedPathEntryIsValid) {
      String investigatedPath = QtUtils.getQtSubPathUnderQtPath(selectedPathEntry, qtPathType);
      if (investigatedPath.length() > 0) {
        selectedPathEntry = investigatedPath;
        selectedPathEntryIsValid = true;
      }
    }
    if ((selectedPathEntryIsValid) && (siblingPathEntryIsEmpty)) {
      siblingPathEntry = QtUtils.getSiblingQtPath(selectedPathEntry, qtPathType, siblingPathType);
    }
    (qtPathType == 0 ? this.binPath : this.includePath).setStringValue(selectedPathEntry);
    (siblingPathType == 0 ? this.binPath : this.includePath).setStringValue(siblingPathEntry);
  }

  public String getVersionName() {
    return this.versionName.getText();
  }

  public void setVersionName(String versionName) {
    this.versionName.setText(versionName);
    dialogChanged();
  }

  public String getBinPath() {
    return this.binPath.getStringValue();
  }

  public void setBinPath(String binPath) {
    this.binPath.setStringValue(binPath);
    dialogChanged();
  }

  public String getIncludePath() {
    return this.includePath.getStringValue();
  }

  public void setIncludePath(String includePath) {
    this.includePath.setStringValue(includePath);
    dialogChanged();
  }
}