package com.github.lppedd.cc.language.lexer;

import org.jetbrains.annotations.NotNull;
import com.github.lppedd.cc.parser.Token;
import com.intellij.psi.PlainTextTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.intellij.lexer.FlexLexer;

%%

%class ConventionalCommitFlexLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%ignorecase
%line
%column

%{

  private int yyline = 0;
  private int yycolumn = 0;

  @NotNull
  private IElementType getFooterType() {
    final String text = yytext().toString().trim();
    return "BREAKING CHANGE".equals(text) || "BREAKING-CHANGE".equals(text)
      ? ConventionalCommitTokenType.FOOTER_TYPE_BREAKING_CHANGE
      : ConventionalCommitTokenType.FOOTER_TYPE;
  }

%}

Body       = .+\n?(.*\n?)*
FooterType = [^:\s]+ | BREAKING\ CHANGE

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
      [^ ] {
        yypushback(yylength());
        yybegin(TYPE);
      }
}

<TYPE, SCOPE, SUMMARY_SEPARATOR, SUBJECT> {
      \n {
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
      ^[^(:\n]+\!? {
        if (yycharat(yylength() - 1) == '!') {
          yypushback(1);
        }

        return ConventionalCommitTokenType.TYPE;
      }

      \( {
        yybegin(SCOPE);
        return ConventionalCommitTokenType.PAREN_LEFT;
      }
}

<SCOPE> {
      [^)\n]+ {
        return ConventionalCommitTokenType.SCOPE;
      }

      \) {
        yybegin(SUMMARY_SEPARATOR);
        return ConventionalCommitTokenType.PAREN_RIGHT;
      }
}

<SUBJECT> {
      [^\n]+ {
        yybegin(TYPE);
        return ConventionalCommitTokenType.SUBJECT;
      }
}

<BODY_OR_FOOTERS> {
      \n {
        return TokenType.WHITE_SPACE;
      }

      // Closes: #16
      ^{FooterType}\ *: {
        // The ':' char should not be part of the footer type
        yypushback(1);
        yybegin(FOOTERS);
        return getFooterType();
      }

      // Closes #16
      ^{FooterType} / (\ +#.*) {
        yybegin(FOOTER_VALUE);
        return getFooterType();
      }

      [^] {
        yypushback(yylength());
        yybegin(BODY);
      }
}

<BODY> {
      // My body which spawns
      // multiple lines.
      //
      // Closes: 16
      {Body} / \n(\ *\n\ *)+{FooterType}\ *:[^]* | \n(\ *\n\ *)+{FooterType}\ +#[^]* {
        yybegin(FOOTERS);
        return ConventionalCommitTokenType.BODY;
      }

      {Body} {
        return ConventionalCommitTokenType.BODY;
      }
}

<FOOTERS> {
      // Closes: #16
      ^{FooterType}\ * {
        return getFooterType();
      }

      // Closes #16
      ^{FooterType} / \ +#.* {
        yybegin(FOOTER_VALUE);
        return getFooterType();
      }

      : {
        yybegin(FOOTER_VALUE);
        return ConventionalCommitTokenType.SEPARATOR;
      }
}

<FOOTER_VALUE> {
      .+(\n\ +.+)* {
        return ConventionalCommitTokenType.FOOTER_VALUE;
      }

      \n {
        yybegin(FOOTERS);
        return TokenType.WHITE_SPACE;
      }
}

\ + {
  return TokenType.WHITE_SPACE;
}

\n {
  return TokenType.WHITE_SPACE;
}

[^] {
   return PlainTextTokenTypes.PLAIN_TEXT;
}
