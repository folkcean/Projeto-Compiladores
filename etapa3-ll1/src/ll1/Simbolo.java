package ll1;

import lexer.TokenType;

/**
 * Um símbolo da gramática.
 *
 * A pilha do parser LL(1) guarda terminais e não-terminais misturados
 * (por exemplo: $ P ) E'), então precisamos de um tipo que represente os dois.
 * Em cada Simbolo, só UM dos campos fica preenchido:
 *   - terminal:    um tipo de token do lexer (LPAREN, NUMBER, EOF...)
 *   - naoTerminal: um não-terminal da gramática (P, S, L...)
 *
 * "record" é uma classe só de dados: o Java cria sozinho o construtor,
 * os métodos terminal() e naoTerminal(), e o equals().
 */
public record Simbolo(TokenType terminal, NaoTerminal naoTerminal) {

    /** Cria um símbolo terminal. Ex.: t(LPAREN) */
    public static Simbolo t(TokenType terminal) {
        return new Simbolo(terminal, null);
    }

    /** Cria um símbolo não-terminal. Ex.: nt(S) */
    public static Simbolo nt(NaoTerminal naoTerminal) {
        return new Simbolo(null, naoTerminal);
    }

    public boolean ehTerminal() {
        return terminal != null;
    }

    /**
     * Nome de um token como ele aparece na teoria:
     * LPAREN vira "(", RPAREN vira ")" e EOF vira "$" (fim da entrada).
     */
    public static String mostrar(TokenType tipo) {
        return switch (tipo) {
            case LPAREN -> "(";
            case RPAREN -> ")";
            case EOF -> "$";
            default -> tipo.name();
        };
    }

    @Override
    public String toString() {
        return ehTerminal() ? mostrar(terminal) : naoTerminal.toString();
    }
}