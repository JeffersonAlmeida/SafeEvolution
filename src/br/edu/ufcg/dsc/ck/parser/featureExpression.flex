package br.ufpe.cin.lps.ck.parser;

import java_cup.runtime.*;

/**
 * This class is a simple example lexer.
 */
%%

%class scanner
%unicode
%cup
%line
%column
%public

%{
  StringBuilder string = new StringBuilder();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
  
  public int yycolumn() {
  	return this.yycolumn;
  }
  
%}
WhiteSpace     = [ ]

FeatureName = [:jletter:] [:jletterdigit:]*

%%

<YYINITIAL> "OR"           { return symbol(sym.OR); }
<YYINITIAL> "AND"          { return symbol(sym.AND); }
<YYINITIAL> "NOT"          { return symbol(sym.NOT); }

<YYINITIAL> "or"           { return symbol(sym.OR); }
<YYINITIAL> "and"          { return symbol(sym.AND); }
<YYINITIAL> "not"          { return symbol(sym.NOT); }

<YYINITIAL> {
  /* feature names */ 
  {FeatureName}                   { return symbol(sym.FEATURE, yytext()); }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
  "("		{return symbol(sym.LPAREN); }
  ")"		{return symbol(sym.RPAREN); }
}

/* error fallback */
.|\n                             { throw new Error("Illegal character <"+yytext()+">"); }
