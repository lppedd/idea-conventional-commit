package com.github.lppedd.cc.language.lexer;

import com.intellij.psi.PlainTextTokenTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

%%

%class ConventionalCommitFlexLexer
%implements EofCapableFlexLexer
%function advance
%type IElementType
%public
%unicode
%ignorecase

%{

  private boolean isEof;

  @Override
  public boolean isEof() {
    return isEof;
  }

  @Override
  public void setEof(final boolean isEof) {
    this.isEof = isEof;
  }

  private IElementType getFooterType() {
    final String text = yytext().toString().trim();
    return "BREAKING CHANGE".equals(text) || "BREAKING-CHANGE".equals(text)
      ? ConventionalCommitTokenType.FOOTER_TYPE_BREAKING_CHANGE
      : ConventionalCommitTokenType.FOOTER_TYPE;
  }

%}

NL          = \r\n | \r | \n
WS          = [ \t]

FooterType  = [^:\s]+ | BREAKING\ CHANGE

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

<TYPE, SCOPE, SUMMARY_SEPARATOR, SUBJECT> {
      {NL} {
        yybegin(BODY_OR_FOOTERS);
        return TokenType.WHITE_SPACE;
      }

      "!" / ":" {
        return ConventionalCommitTokenType.BREAKING_CHANGE;
      }

      ":" {
        yybegin(SUBJECT);
        return ConventionalCommitTokenType.SEPARATOR;
      }
}

<TYPE> {
      // The commit type must be at the beginning of the line, and cannot start with a '!'.
      // Note that we allow types with spaces inside, so that inspections can report issues.
      ^[^!(:\r\n][^(:\r\n]*\!? {
        if (yycharat(yylength() - 1) == '!') {
          yypushback(1);
        }

        return ConventionalCommitTokenType.TYPE;
      }

      // This catches the case where we have a lone '!'.
      // We lex it as BREAKING_CHANGE so that we do not fail lexing
      // and then let the parser or inspections reason about it.
      // Note that this rule must have lower priority than '\! / :'.
      "!" {
        return ConventionalCommitTokenType.BREAKING_CHANGE;
      }

      "(" {
        yybegin(SCOPE);
        return ConventionalCommitTokenType.SCOPE_OPEN_PAREN;
      }
}

<SCOPE> {
      [^)\r\n]+ {
        return ConventionalCommitTokenType.SCOPE;
      }

      ")" {
        yybegin(SUMMARY_SEPARATOR);
        return ConventionalCommitTokenType.SCOPE_CLOSE_PAREN;
      }
}

<SUBJECT> {
      // A subject is any text up to a newline character
      [^\r\n]+ {
        yybegin(TYPE);
        return ConventionalCommitTokenType.SUBJECT;
      }
}

<BODY_OR_FOOTERS> {
      // Closes: #16
      ^{FooterType}{WS}*: {
        // The ':' char should not be part of the footer type
        yypushback(1);
        yybegin(FOOTERS);
        return getFooterType();
      }

      // Closes #16
      ^{FooterType}{WS}+ / #.* {
        yybegin(FOOTER_VALUE);
        return getFooterType();
      }

      // Skip all blank lines after the summary
      ^{WS}*{NL} {
        return TokenType.WHITE_SPACE;
      }

      ^{WS}+ {
        yypushback(yylength());
        yybegin(BODY);
      }

      [^] {
        yypushback(yylength());
        yybegin(BODY);
      }
}

<BODY> {
      {NL}{WS}*{NL} ({FooterType}{WS}*: | {FooterType}{WS}+#) {
        yypushback(yylength());
        yybegin(FOOTERS);
        return ConventionalCommitTokenType.BODY;
      }

      [^] | {WS}+ {
        // Skip
      }

      // Match body until EOF
      <<EOF>> {
        if (isEof()) {
          return null;
        }

        setEof(true);
        return ConventionalCommitTokenType.BODY;
      }
}

<FOOTERS> {
      // Closes: #16
      ^{FooterType}{WS}*: {
        yypushback(1);
        return getFooterType();
      }

      // Closes #16
      ^{FooterType}{WS}+ / #.* {
        yybegin(FOOTER_VALUE);
        return getFooterType();
      }

      ":" {
        yybegin(FOOTER_VALUE);
        return ConventionalCommitTokenType.FOOTER_SEPARATOR;
      }
}

<FOOTER_VALUE> {
      // Closes #16           | .+
      //  multiline footer    | ({NewLine}{Space}+([^\s]+{Space}*)+)*
      //  value               | ({NewLine}{Space}+([^\s]+{Space}*)+)*
      .+ ({NL}{WS}+([^\s]+{WS}*)+)* {
        return ConventionalCommitTokenType.FOOTER_VALUE;
      }

      {NL} {
        yybegin(FOOTERS);
        return TokenType.WHITE_SPACE;
      }
}

{WS} {
  return TokenType.WHITE_SPACE;
}

{NL} {
  return TokenType.WHITE_SPACE;
}

[^] {
  return PlainTextTokenTypes.PLAIN_TEXT;
}
