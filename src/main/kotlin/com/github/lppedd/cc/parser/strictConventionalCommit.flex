package com.github.lppedd.cc.parser;

// Note that while the name includes "strict", the strictness is only related
// to how tokens are looked for: a message is conventional commits compliant
// only if it has at least a type and a subject.
// However, valid characters for tokens are not as restrictive as what the
// specification mandates.

%%

%class StrictConventionalCommitFlexLexer
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

  private int getWsPushback(final CharSequence value) {
    final int length = value.length();

    for (int i = length - 1, count = 0; i >= 0; i--, count++) {
      final char c = value.charAt(i);

      if (!Character.isWhitespace(c)) {
        return count;
      }
    }

    return length;
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
%state FOOTER_VALUE

%%

<YYINITIAL> {
      [^] {
        yypushback(yylength());
        yybegin(TYPE);
      }
}

<TYPE> {
      // The commit type must be at the start of the line
      ^[^(:\s]+\!? {
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
        yybegin(BODY_OR_FOOTERS); // Discard newline
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
        // Push back any terminating whitespace, which should be part of the footer value instead
        yypushback(getWsPushback(yytext()));
        yybegin(FOOTER_VALUE);
        return token(CCToken.Type.FOOTER_TYPE);
      }

      ^[\s]+ {
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
        // Push back any terminating whitespace, which should be part of the footer value instead
        yypushback(getWsPushback(yytext()));
        yybegin(FOOTER_VALUE);
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
