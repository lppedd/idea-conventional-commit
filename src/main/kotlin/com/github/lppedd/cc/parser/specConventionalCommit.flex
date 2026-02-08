package com.github.lppedd.cc.parser;

// Note that while the name includes "spec", the lexer does not exactly adhere
// to what the Conventional Commits spec mandates. This lexer *is* stricter
// than the IDE language one, but it still expects cooperation from whatever
// parser is going to consume tokens to actually respect (or not respect)
// the specification.

%%

%class SpecConventionalCommitFlexLexer
%type CCToken
%function advance
%public
%unicode
%ignorecase

%{

  private final StringBuilder sb = new StringBuilder();

  private void appendText(final CharSequence value) {
    sb.append(value);
  }

  private String getTextAndReset() {
    final String value = sb.toString();
    sb.setLength(0);
    return value;
  }

  private int getFooterTypePushback() {
    int i = yylength() - 1;
    int pushback = 0;

    // Push back trailing whitespace
    for (; i >= 0 && isWhitespace(yycharat(i)); i--, pushback++);

    // Check whether we now have a trailing '#'
    while (i >= 0 && yycharat(i) == '#') {
      // If '#' is attached (e.g., 'Something#'), keep it as part of the type and stop
      if (i - 1 >= 0 && !isWhitespace(yycharat(i - 1))) {
        break;
      }

      // Since the '#' char is preceeded by a whitespace, push it back
      i--;
      pushback++;

      // Push back any whitespace before the '#' we have just encountered
      for (; i >= 0 && isWhitespace(yycharat(i)); i--, pushback++);
    }

    return pushback;
  }

  private boolean isWhitespace(final char ch) {
    return Character.isWhitespace(ch) || Character.isSpaceChar(ch);
  }

  private CCToken token(final CCToken.Type type) {
    return token(type, yytext());
  }

  private CCToken token(final CCToken.Type type, final CharSequence value) {
    final int start = getTokenStart();
    final int end = getTokenEnd();
    return new CCToken(type, value.toString(), new CCTextRange(start, end));
  }

%}

NL          = \r\n | \r | \n
WS          = [^\S\r\n]

// We allow footer types with spaces inside
FooterType  = [^\s:][^:\r\n]*

%state TYPE
%state SCOPE
%state SUMMARY_SEPARATOR
%state SUBJECT
%state BODY_OR_FOOTERS
%state BODY
%state FOOTERS
%state FOOTER_HASH_SEPARATOR
%state FOOTER_VALUE

%%

<YYINITIAL> {
      [^] {
        yypushback(yylength());
        yybegin(TYPE);
      }
}

<TYPE> {
      // The commit type must be at the beginning of the line, and cannot start with a '!'
      ^[^!(:\r\n][^(:\r\n]*\!? {
        if (yycharat(yylength() - 1) == '!') {
          yypushback(1);
        }

        return token(CCToken.Type.TYPE);
      }

      "(" {
        yybegin(SCOPE);
        return token(CCToken.Type.SCOPE_OPEN_PAREN);
      }

      "!" {
        return token(CCToken.Type.BREAKING_CHANGE);
      }

      ":" {
        yybegin(SUBJECT);
        return token(CCToken.Type.SEPARATOR);
      }
}

<SCOPE> {
      [^)\r\n]+ {
        return token(CCToken.Type.SCOPE);
      }

      ")" {
        yybegin(TYPE);
        return token(CCToken.Type.SCOPE_CLOSE_PAREN);
      }
}

<SUBJECT> {
      // A subject is any text up to a newline character
      [^\r\n]+ {
        return token(CCToken.Type.SUBJECT);
      }

      {NL} {
        // Discard newline
        yybegin(BODY_OR_FOOTERS);
      }
}

<BODY_OR_FOOTERS> {
      // Closes: #16
      ^{FooterType}: {
        // The ':' char should not be part of the footer type
        yypushback(1);
        yybegin(FOOTERS);
        return token(CCToken.Type.FOOTER_TYPE);
      }

      // Closes #16
      ^{FooterType}{WS} / #.* {
        // Push back any trailing whitespace or '#'
        yypushback(getFooterTypePushback());
        yybegin(FOOTER_HASH_SEPARATOR);
        return token(CCToken.Type.FOOTER_TYPE);
      }

      ^\s+ {
        // Discard whitespace after the subject
      }

      [^] {
        yypushback(yylength());
        yybegin(BODY);
      }
}

<BODY> {
      // Body until a footer starts (footer is NOT consumed)
      [^] / {NL}+({FooterType}: | {FooterType}{WS}#) {
        appendText(yytext());
        yybegin(FOOTERS);
        return token(CCToken.Type.BODY, getTextAndReset());
      }

      [^] {
        appendText(yytext());
      }

      // Match body until EOF
      <<EOF>> {
        yybegin(YYINITIAL);
        return token(CCToken.Type.BODY, getTextAndReset());
      }
}

<FOOTERS> {
      // Closes: #16
      ^{FooterType}: {
        // The ':' char should not be part of the footer type
        yypushback(1);
        return token(CCToken.Type.FOOTER_TYPE);
      }

      // Closes #16
      ^{FooterType}{WS} / #.* {
        // Push back any trailing whitespace or '#'
        yypushback(getFooterTypePushback());
        yybegin(FOOTER_HASH_SEPARATOR);
        return token(CCToken.Type.FOOTER_TYPE);
      }

      // A footer type without an associated value.
      // Closes
      // [Closes]
      ^{FooterType} {
        yybegin(FOOTER_VALUE);
        return token(CCToken.Type.FOOTER_TYPE);
      }

      ":" {
        yybegin(FOOTER_VALUE);
        return token(CCToken.Type.SEPARATOR);
      }
}

<FOOTER_HASH_SEPARATOR> {
      // Lex exactly one whitespace as the footer separator token ' '.
      // Additional whitespace will be lexed as part of the footer value.
      {WS} {
        yybegin(FOOTER_VALUE);
        return token(CCToken.Type.SEPARATOR);
      }
}

<FOOTER_VALUE> {
      // Closes #16           | .+
      //  multiline footer    | ({NewLine}{Space}+([^\s]+{Space}*)+)*
      //  value               | ({NewLine}{Space}+([^\s]+{Space}*)+)*
      .+ ({NL}{WS}+([^\s]+{WS}*)+)* {
        return token(CCToken.Type.FOOTER_VALUE);
      }

      {NL} {
        yybegin(FOOTERS); // Discard whitespace
      }
}

\s {
  // Discard whitespace
}

[^] {
  return token(CCToken.Type.ERROR);
}
