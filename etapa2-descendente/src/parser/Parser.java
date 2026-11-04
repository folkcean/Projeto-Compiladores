package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

import java.io.IOException;

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

    private boolean startsSExpr() {
        return atual.getType() == TokenType.LPAREN || isAtom();
    }

    public void parseProgram() throws IOException {
        imprime("P");
        profundidade++;
        while (startsSExpr()) {
            parseSExpr();
        }
        comparar(TokenType.EOF);
        profundidade--;
    }

    private void parseSExpr() throws IOException {
        imprime("S");
        profundidade++;
        if (atual.getType() == TokenType.LPAREN) {
            parseList();
        } else if (isAtom()) {
            parseAtom();
        } else {
            throw new SyntaxException("uma expressão", atual);
        }
        profundidade--;
    }

    private void parseList() throws IOException {
        imprime("L");
        profundidade++;
        imprime(comparar(TokenType.LPAREN).toString());
        parseItems();
        imprime(comparar(TokenType.RPAREN).toString());
        profundidade--;
    }

    private void parseItems() throws IOException {
        imprime("I");
        profundidade++;
        while (startsSExpr()) {
            parseSExpr();
        }
        profundidade--;
    }

    private void parseAtom() throws IOException {
        imprime("A");
        profundidade++;
        imprime(comparar(atual.getType()).toString());
        profundidade--;
    }

    private void imprime(String text) {
        System.out.println("  ".repeat(profundidade) + text);
    }

}