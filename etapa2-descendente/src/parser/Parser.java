package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

import java.io.IOException;

/*
 * Gramática (LL(1), sem while: repetição só por recursão):
 *
 * <program>       ::= <s-expr> <program> | ε                  P
 * <s-expr>        ::= <atom> | <list>                         S
 * <list>          ::= "(" <list-rest>                         L
 * <list-rest>     ::= <elements> ")" | ")"                    L'
 * <elements>      ::= <s-expr> <elements-rest>                E
 * <elements-rest> ::= <elements> | ε                          E'
 * <atom>          ::= NUMBER | SYMBOL | STRING | BOOLEAN      A
 */
public class Parser {
    private final Lexer lexer;
    private int profundidade = 0;
    private Token atual;

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        avancar();
    }

    private void avancar() throws IOException {
        atual = lexer.yylex();
    }

    private Token comparar(TokenType esperado) throws IOException {
        if (atual.getType() == esperado) {
            Token consumido = atual;
            avancar();
            return consumido;
        } else {
            throw new SyntaxException(esperado.toString(), atual);
        }
    }

    private boolean isAtom() {
        TokenType tipo = atual.getType();
        return tipo == TokenType.NUMBER
                || tipo == TokenType.SYMBOL
                || tipo == TokenType.STRING
                || tipo == TokenType.BOOLEAN;
    }

    // FIRST(<s-expr>) = { "(", NUMBER, SYMBOL, STRING, BOOLEAN }
    private boolean startsSExpr() {
        return atual.getType() == TokenType.LPAREN || isAtom();
    }

    // Ponto de entrada: o EOF fica fora da gramática, então é checado uma única vez aqui
    public void parseProgram() throws IOException {
        P();
        comparar(TokenType.EOF);
    }

    // <program> ::= <s-expr> <program> | ε
    private void P() throws IOException {
        imprime("P");
        profundidade++;
        if (startsSExpr()) {
            S();
            P();
        } else {
            imprime("ε");
        }
        profundidade--;
    }

    // <s-expr> ::= <atom> | <list>
    private void S() throws IOException {
        imprime("S");
        profundidade++;
        if (isAtom()) {
            A();
        } else if (atual.getType() == TokenType.LPAREN) {
            L();
        } else {
            throw new SyntaxException("uma expressão", atual);
        }
        profundidade--;
    }

    // <list> ::= "(" <list-rest>
    private void L() throws IOException {
        imprime("L");
        profundidade++;
        imprime(comparar(TokenType.LPAREN).toString());
        Llinha();
        profundidade--;
    }

    // <list-rest> ::= <elements> ")" | ")"
    // As duas alternativas terminam em ")", então só o <elements> é opcional
    private void Llinha() throws IOException {
        imprime("L'");
        profundidade++;
        if (startsSExpr()) {
            E();
        }
        imprime(comparar(TokenType.RPAREN).toString());
        profundidade--;
    }

    // <elements> ::= <s-expr> <elements-rest>
    private void E() throws IOException {
        imprime("E");
        profundidade++;
        S();
        Elinha();
        profundidade--;
    }

    // <elements-rest> ::= <elements> | ε
    private void Elinha() throws IOException {
        imprime("E'");
        profundidade++;
        if (startsSExpr()) {
            E();
        } else {
            imprime("ε");
        }
        profundidade--;
    }

    // <atom> ::= NUMBER | SYMBOL | STRING | BOOLEAN
    private void A() throws IOException {
        imprime("A");
        profundidade++;
        imprime(comparar(atual.getType()).toString());
        profundidade--;
    }

    private void imprime(String text) {
        System.out.println("  ".repeat(profundidade) + text);
    }

}