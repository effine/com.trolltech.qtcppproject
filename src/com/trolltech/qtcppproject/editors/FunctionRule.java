package com.trolltech.qtcppproject.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class FunctionRule extends WordRule {
	public FunctionRule(IWordDetector detector, IToken defaultToken) {
		super(detector, defaultToken);
	}

	public IToken evaluate(ICharacterScanner scanner) {
		IToken token = super.evaluate(scanner);
		if (!token.isUndefined()) {
			int c = scanner.read();
			scanner.unread();
			if (c == 40) {
				return token;
			}
			unreadBuffer(scanner);
		}
		return Token.UNDEFINED;
	}
}