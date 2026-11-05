package ll1;

import lexer.Token;
import lexer.TokenType;

/**
 * Erro sintático: o token encontrado não é o que a gramática permite naquele ponto.
 * Igual à da etapa 2, só com o package trocado para ll1.
 *
 * Mensagem: "esperado X, encontrado Y (linha L, coluna C)"
 */
public class SyntaxException extends RuntimeException {

    public SyntaxException(String esperado, Token encontrado) {
        super("esperado " + esperado
                + ", encontrado " + describe(encontrado)
                + " (linha " + encontrado.getLine()
                + ", coluna " + encontrado.getColumn() + ")");
    }

    // super(...) tem que ser a primeira instrução do construtor,
    // então a checagem de EOF fica num método static separado.
    private static String describe(Token token) {
        if (token.getType() == TokenType.EOF) {
            return "fim do arquivo";
        }
        return "'" + token.getLexeme() + "' (" + token.getType() + ")";
    }
}