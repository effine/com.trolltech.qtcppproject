package com.trolltech.qtcppproject.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class ProFileViewerConfiguration extends SourceViewerConfiguration {
	private ProFileScanner scanner;
	private ColorManager colorManager;

	public ProFileViewerConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}

	protected ProFileScanner getProFileScanner() {
		if (this.scanner == null) {
			this.scanner = new ProFileScanner(this.colorManager);
			this.scanner
					.setDefaultReturnToken(new Token(new TextAttribute(
							this.colorManager
									.getColor(IProFileColorConstants.DEFAULT))));
		}

		return this.scanner;
	}

	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(
				getProFileScanner());
		reconciler.setDamager(dr, "__dftl_partition_content_type");
		reconciler.setRepairer(dr, "__dftl_partition_content_type");
		return reconciler;
	}
}