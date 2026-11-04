%%
%class Lexer
%unicode
%public
%type String

DIGIT = [0-9]
LETTER = [a-zA-Z]

INITIAL_CHAR = [a-zA-Z+*/=<>\!?-]
SYMBOL_CHAR = [a-zA-Z+*/=<>\!?0-9-]

DIGITS = {DIGIT}+

NUMBER = [+-]?{DIGITS}(\.{DIGITS})?

BOOLEAN = T|NIL|#t|#f

SYMBOL = {INITIAL_CHAR}{SYMBOL_CHAR}*

STRING = \"[^\"]*\"

WHITESPACE = [ \t\r\n]+

%%

"(" {
    return "LPAREN";
}

")" {
    return "RPAREN";
}

{BOOLEAN} {
    return "BOOLEAN: " + yytext();
}

{NUMBER} {
    return "NUMBER: " + yytext();
}

{STRING} {
    return "STRING: " + yytext();
}

{SYMBOL} {
    return "SYMBOL: " + yytext();
}

{WHITESPACE} {
    // ignora espaços, tabs e quebras de linha
}

. {
    return "ERRO: " + yytext();
}