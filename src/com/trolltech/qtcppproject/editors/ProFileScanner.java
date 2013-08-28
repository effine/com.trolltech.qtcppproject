package com.trolltech.qtcppproject.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class ProFileScanner extends RuleBasedScanner {
	private String[] variables = { "CONFIG", "DEFINES", "DEF_FILE",
			"DEPENDPATH", "DESTDIR", "DESTDIR_TARGET", "DISTFILES",
			"DLLDESTDIR", "FORMS", "HEADERS", "INCLUDEPATH", "LEXSOURCES",
			"LIBS", "MAKEFILE", "MOC_DIR", "OBJECTS", "OBJECTS_DIR", "OBJMOC",
			"PKGCONFIG", "POST_TARGETDEPS", "PRECOMPILED_HEADER",
			"PRE_TARGETDEPS", "QMAKE", "QMAKESPEC", "QT", "RCC_DIR", "RC_FILE",
			"REQUIRES", "RESOURCES", "RES_FILE", "SOURCES", "SRCMOC",
			"SUBDIRS", "TARGET", "TARGET_EXT", "TARGET_x", "TARGET_x.y.z",
			"TEMPLATE", "TRANSLATIONS", "UI_DIR", "UI_HEADERS_DIR",
			"UI_SOURCES_DIR", "VER_MAJ", "VER_MIN", "VER_PAT", "VERSION",
			"VPATH", "YACCSOURCES" };

	private String[] functions = { "basename", "CONFIG", "contains", "count",
			"dirname", "error", "exists", "find", "for", "include", "infile",
			"isEmpty", "join", "member", "message", "prompt", "quote",
			"sprintf", "system", "unique", "warning" };

	public ProFileScanner(ColorManager manager) {
		IToken commentToken = new Token(new TextAttribute(
				manager.getColor(IProFileColorConstants.COMMENT)));

		IToken functionToken = new Token(new TextAttribute(
				manager.getColor(IProFileColorConstants.FUNCTION)));

		IToken variableToken = new Token(new TextAttribute(
				manager.getColor(IProFileColorConstants.VARIABLE)));

		IToken defaultToken = new Token(new TextAttribute(
				manager.getColor(IProFileColorConstants.DEFAULT)));

		IRule[] rules = new IRule[3];
		rules[0] = new EndOfLineRule("#", commentToken);
		IWordDetector wd = new IWordDetector() {
			public boolean isWordStart(char c) {
				return (Character.isLetter(c)) || (c == '_') || (c == '.');
			}

			public boolean isWordPart(char c) {
				return isWordStart(c);
			}
		};
		FunctionRule fr = new FunctionRule(wd, Token.UNDEFINED);
		for (String s : this.functions) {
			fr.addWord(s, functionToken);
		}

		WordRule wr = new WordRule(wd, defaultToken);
		for (String s : this.functions) {
			wr.addWord(s, functionToken);
		}
		for (String s : this.variables) {
			wr.addWord(s, variableToken);
		}
		rules[1] = fr;
		rules[2] = wr;

		setRules(rules);
	}
}