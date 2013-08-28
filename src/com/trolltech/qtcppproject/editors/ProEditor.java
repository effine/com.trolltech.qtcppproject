package com.trolltech.qtcppproject.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class ProEditor extends TextEditor
{
  public static String ID = "com.trolltech.qtcppproject.editors.ProEditor";
  private ColorManager colorManager;

  public ProEditor()
  {
    this.colorManager = new ColorManager();
    setSourceViewerConfiguration(new ProFileViewerConfiguration(this.colorManager));
  }
  public void dispose() {
    this.colorManager.dispose();
    super.dispose();
  }
}