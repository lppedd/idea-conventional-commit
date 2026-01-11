package com.github.lppedd.cc.language.lexer;

import org.jetbrains.annotations.NotNull;

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

  @NotNull
  private IElementType getFooterType() {
    final String text = yytext().toString().trim();
    return "BREAKING CHANGE".equals(text) || "BREAKING-CHANGE".equals(text)
      ? ConventionalCommitTokenType.FOOTER_TYPE_BREAKING_CHANGE
      : ConventionalCommitTokenType.FOOTER_TYPE;
  }

%}

NewLine     = \r\n | \r | \n
Space       = [ \t]
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
      {NewLine} {
        yybegin(BODY_OR_FOOTERS);
        return TokenType.WHITE_SPACE;
      }

      \! / : {
        return ConventionalCommitTokenType.BREAKING_CHANGE;
      }

      : {
        yybegin(SUBJECT);
        return ConventionalCommitTokenType.SEPARATOR;
      }
}

<TYPE> {
      ^[^(:\r\n]+\!? {
        if (yycharat(yylength() - 1) == '!') {
          yypushback(1);
        }

        return ConventionalCommitTokenType.TYPE;
      }

      \( {
        yybegin(SCOPE);
        return ConventionalCommitTokenType.SCOPE_OPEN_PAREN;
      }
}

<SCOPE> {
      [^)\r\n]+ {
        return ConventionalCommitTokenType.SCOPE;
      }

      \) {
        yybegin(SUMMARY_SEPARATOR);
        return ConventionalCommitTokenType.SCOPE_CLOSE_PAREN;
      }
}

<SUBJECT> {
      [^\r\n]+ {
        yybegin(TYPE);
        return ConventionalCommitTokenType.SUBJECT;
      }
}

<BODY_OR_FOOTERS> {
      // Closes: #16
      ^{FooterType}{Space}*: {
        // The ':' char should not be part of the footer type
        yypushback(1);
        yybegin(FOOTERS);
        return getFooterType();
      }

      // Closes #16
      ^{FooterType}{Space}+ / #.* {
        yybegin(FOOTER_VALUE);
        return getFooterType();
      }

      // Skip all blank lines after the summary
      ^{Space}*{NewLine} {
        return TokenType.WHITE_SPACE;
      }

      ^{Space}+ {
        yypushback(yylength());
        yybegin(BODY);
      }

      [^] {
        yypushback(yylength());
        yybegin(BODY);
      }
}

<BODY> {
      {NewLine}{Space}*{NewLine} ({FooterType}{Space}*: | {FooterType}{Space}+#) {
        yypushback(yylength());
        yybegin(FOOTERS);
        return ConventionalCommitTokenType.BODY;
      }

      [^] | {Space}+ {
        // Skip
      }

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
      ^{FooterType}{Space}*: {
        yypushback(1);
        return getFooterType();
      }

      // Closes #16
      ^{FooterType}{Space}+ / #.* {
        yybegin(FOOTER_VALUE);
        return getFooterType();
      }

      : {
        yybegin(FOOTER_VALUE);
        return ConventionalCommitTokenType.FOOTER_SEPARATOR;
      }
}

<FOOTER_VALUE> {
      // Closes #16           | .+
      //  multiline footer    | ({NewLine}{Space}+([^\s]+{Space}*)+)*
      //  value               | ({NewLine}{Space}+([^\s]+{Space}*)+)*
      .+ ({NewLine}{Space}+([^\s]+{Space}*)+)* {
        return ConventionalCommitTokenType.FOOTER_VALUE;
      }

      {NewLine} {
        yybegin(FOOTERS);
        return TokenType.WHITE_SPACE;
      }
}

{Space} {
  return TokenType.WHITE_SPACE;
}

{NewLine} {
  return TokenType.WHITE_SPACE;
}

[^] {
  return PlainTextTokenTypes.PLAIN_TEXT;
}
