/** Optional javadoc style comment */
grammar Plist;

options {
}

// import ;

tokens {
    LP, RP
}

/* lexer grammars only
channels { // lexer only
    WHITESPACE_SHANNEL,
    COMMENTS_CHANNEL
}
*/

@actionName {
}

// Lexer rules
LP:                       '(';
RP:                       ')';
LBRACE:                   '{';
RBRACE:                   '}';
// LBRACK:                   '[';
// RBRACK:                   ']';
SEMI:                     ';';
COMMA:                    ',';
//DOT:                      '.';
//STRUCTACCESS:             '->';
//AT:                       '@';
EQUALS:                     '=';
COLON:                    ':';
MINUS:                    '-';

WS:                       Ws+             -> skip;
LINECOMMENT:              '//' ~[\r\n]*   -> skip;  // see also https://stackoverflow.com/questions/23976617/parsing-single-line-comments
BLOCKCOMMENT:              '/*' (BLOCKCOMMENT | ('*' ~'/') | ~'*')* '*/' -> skip;

STRING
   : '"' (~["] | '\\"')* '"' { setText(getText().substring(1, getText().length()-1)); }
   | NameChar ~[= , ; ) \p{White_Space}]*
   ;
NUMBER
   : MINUS? '0'..'9'+ ('.' '0'..'9'*)?
   | MINUS? '.' '0'..'9'+
   ;

fragment Ws: [ \r\n\t\u000C];
fragment NameChar
   : NameStartChar
   | '0'..'9'
   | '_'
   | '\u00B7'
   | '\u0300'..'\u036F'
   | '\u203F'..'\u2040'
   ;
fragment NameStartChar
   : 'A'..'Z' | 'a'..'z'
   | '\u00C0'..'\u00D6'
   | '\u00D8'..'\u00F6'
   | '\u00F8'..'\u02FF'
   | '\u0370'..'\u037D'
   | '\u037F'..'\u1FFF'
   | '\u200C'..'\u200D'
   | '\u2070'..'\u218F'
   | '\u2C00'..'\u2FEF'
   | '\u3001'..'\uD7FF'
   | '\uF900'..'\uFDCF'
   | '\uFDF0'..'\uFFFD'
   ;

// parser rules
parse: dictionary | list;
dictionary: LBRACE (keyvaluepair (SEMI keyvaluepair?)*)? RBRACE;
keyvaluepair: STRING EQUALS value;
value: string | number | list | dictionary;
string: STRING;
number: NUMBER;
list: LP (value (COMMA value?)*)? RP;
