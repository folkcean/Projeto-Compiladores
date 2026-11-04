package lexer;
%%
%class Lexer
%unicode
%public
%type Token
%line
%column

%{

private Token token(TokenType type){
      return new Token(type, yytext(), yyline + 1, yycolumn + 1);
}

%}

%{
    // monta a mensagem de erro com linha e coluna (yyline/yycolumn começam em 0)
    private LexicalException erro(String motivo) {
        return new LexicalException(motivo + " '" + yytext() + "'", yyline + 1, yycolumn + 1);
    }
%}

DIGIT = [0-9]

INITIAL_CHAR = [a-zA-Z+*/=<>\!?-]
SYMBOL_CHAR = [a-zA-Z+*/=<>\!?0-9-]

DIGITS = {DIGIT}+

NUMBER = [+-]?{DIGITS}(\.{DIGITS})?

BOOLEAN = T|NIL|#t|#f

SYMBOL = {INITIAL_CHAR}{SYMBOL_CHAR}*

// strings não atravessam quebra de linha
STRING = \"[^\"\r\n]*\"

// inclui \f (form feed) e \u000B (tab vertical), que antes travavam o lexer
WHITESPACE = [ \t\r\n\f\u000B]+

// erros léxicos
UNCLOSED_STRING = \"[^\"\r\n]*
BAD_NUMBER = {DIGIT}({SYMBOL_CHAR}|\.)*
BAD_BOOLEAN = #{SYMBOL_CHAR}+

%%

"(" {
    return token(TokenType.LPAREN);
}

")" {
    return token(TokenType.RPAREN);
}

{BOOLEAN} {
    return token(TokenType.BOOLEAN);
}

{NUMBER} {
    return token(TokenType.NUMBER);
}

{STRING} {
    return token(TokenType.STRING);
}

{SYMBOL} {
    return token(TokenType.SYMBOL);
}

{WHITESPACE} {
    // ignora espaços, tabs e quebras de linha
}

// As regras de erro vêm depois das válidas: em empate de tamanho, a regra
// declarada primeiro vence. Assim "123" e "3.14" continuam NUMBER e "#t"
// continua BOOLEAN, mas "1abc", "123." e "#tx" casam mais caracteres aqui.

{UNCLOSED_STRING} {
    throw erro("string não fechada");
}

{BAD_NUMBER} {
    throw erro("número malformado");
}

{BAD_BOOLEAN} {
    throw erro("booleano inválido");
}

<<EOF>> {
    return new Token(TokenType.EOF, "", yyline + 1, yycolumn + 1);
}

// [^] casa QUALQUER caractere (o "." do JFlex não casa terminadores de linha)
[^] {
    throw erro("caractere inválido");
}