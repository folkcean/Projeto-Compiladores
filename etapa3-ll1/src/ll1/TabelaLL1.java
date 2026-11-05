package ll1;

import lexer.TokenType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static ll1.NaoTerminal.*;
import static ll1.Simbolo.nt;
import static ll1.Simbolo.t;
import static lexer.TokenType.*;

/**
 * Gramática LL(1) de Lisp e a sua tabela de análise sintática M[não-terminal][token].
 *
 * A tabela foi calculada à mão com FIRST e FOLLOW (ver docs/etapa3-LL1.md):
 *   - para cada produção X → α, a produção vai em M[X, t] para cada t em FIRST(α);
 *   - se α pode virar ε, ela vai também em M[X, t] para cada t em FOLLOW(X).
 *
 *          (    )    NUMBER  SYMBOL  STRING  BOOLEAN  $
 *   P      1    -    1       1       1       1        2
 *   S      4    -    3       3       3       3        -
 *   L      5    -    -       -       -       -        -
 *   L'     6    7    6       6       6       6        -
 *   E      8    -    8       8       8       8        -
 *   E'     9    10   9       9       9       9        -
 *   A      -    -    11      12      13      14       -
 *
 * "-" = célula vazia = erro sintático.
 */
public final class TabelaLL1 {

    /*
     * As 14 produções. A numeração TEM que ser a mesma do gramatica/lisp-ll1.txt
     * (parte do Iarlo) e do docs/, porque o RodarTestes compara as duas tabelas.
     */
    public static final List<Producao> PRODUCOES = List.of(
            // programa: zero ou mais expressões (extensão: o professor usa só uma)
            new Producao(1, P, List.of(nt(S), nt(P))),                 // P  → S P
            new Producao(2, P, List.of()),                             // P  → ε
            // <s-expr> ::= <atom> | <list>
            new Producao(3, S, List.of(nt(A))),                        // S  → A
            new Producao(4, S, List.of(nt(L))),                        // S  → L
            // <list> ::= "(" <elements> ")" | "(" ")"   — fatorado pelo "("
            new Producao(5, L, List.of(t(LPAREN), nt(L2))),            // L  → ( L'
            new Producao(6, L2, List.of(nt(E), t(RPAREN))),            // L' → E )
            new Producao(7, L2, List.of(t(RPAREN))),                   // L' → )
            // <elements> ::= <s-expr> <elements> | <s-expr>   — fatorado pelo <s-expr>
            new Producao(8, E, List.of(nt(S), nt(E2))),                // E  → S E'
            new Producao(9, E2, List.of(nt(E))),                       // E' → E
            new Producao(10, E2, List.of()),                           // E' → ε
            // <atom> ::= <number> | <symbol> | <string> | <boolean>
            new Producao(11, A, List.of(t(NUMBER))),                   // A  → NUMBER
            new Producao(12, A, List.of(t(SYMBOL))),                   // A  → SYMBOL
            new Producao(13, A, List.of(t(STRING))),                   // A  → STRING
            new Producao(14, A, List.of(t(BOOLEAN)))                   // A  → BOOLEAN
    );

    /*
     * A tabela é um "mapa de mapas": para cada não-terminal (linha),
     * um mapa de token (coluna) para a produção que vai naquela célula.
     * Se o token não está no mapa da linha, a célula está vazia.
     */
    private final Map<NaoTerminal, Map<TokenType, Producao>> tabela = new EnumMap<>(NaoTerminal.class);

    public TabelaLL1() {
        // cria uma linha vazia para cada não-terminal
        for (NaoTerminal n : NaoTerminal.values()) {
            tabela.put(n, new EnumMap<>(TokenType.class));
        }

        // P → S P vai em FIRST(S); P → ε vai em FOLLOW(P) = { $ }
        celulas(P, 1, LPAREN, NUMBER, SYMBOL, STRING, BOOLEAN);
        celulas(P, 2, EOF);

        // S → A vai em FIRST(A); S → L vai em FIRST(L) = { ( }
        celulas(S, 3, NUMBER, SYMBOL, STRING, BOOLEAN);
        celulas(S, 4, LPAREN);

        // L → ( L' vai em { ( }
        celulas(L, 5, LPAREN);

        // L' → E ) vai em FIRST(E) = FIRST(S); L' → ) vai em { ) }
        celulas(L2, 6, LPAREN, NUMBER, SYMBOL, STRING, BOOLEAN);
        celulas(L2, 7, RPAREN);

        // E → S E' vai em FIRST(S)
        celulas(E, 8, LPAREN, NUMBER, SYMBOL, STRING, BOOLEAN);

        // E' → E vai em FIRST(E); E' → ε vai em FOLLOW(E') = { ) }
        celulas(E2, 9, LPAREN, NUMBER, SYMBOL, STRING, BOOLEAN);
        celulas(E2, 10, RPAREN);

        // cada átomo vai na coluna do seu próprio token
        celulas(A, 11, NUMBER);
        celulas(A, 12, SYMBOL);
        celulas(A, 13, STRING);
        celulas(A, 14, BOOLEAN);
    }

    /**
     * Coloca a produção "numero" nas células M[naoTerminal, token] de cada token.
     * Se uma célula já tiver outra produção, é um CONFLITO: a gramática não seria LL(1),
     * porque o parser não saberia qual das duas escolher olhando só 1 token.
     */
    private void celulas(NaoTerminal naoTerminal, int numero, TokenType... tokens) {
        for (TokenType token : tokens) {
            Producao anterior = tabela.get(naoTerminal).put(token, producao(numero));
            if (anterior != null) {
                throw new IllegalStateException("conflito LL(1) em M[" + naoTerminal + ", "
                        + Simbolo.mostrar(token) + "]: produções " + anterior.numero() + " e " + numero);
            }
        }
    }

    /** Devolve a produção pelo número (1 a 14). */
    public static Producao producao(int numero) {
        return PRODUCOES.get(numero - 1);
    }

    /** Consulta a célula M[naoTerminal, token]. Devolve null se ela estiver vazia (erro). */
    public Producao get(NaoTerminal naoTerminal, TokenType token) {
        return tabela.get(naoTerminal).get(token);
    }

    /**
     * Tokens que têm produção na linha do não-terminal, ou seja,
     * o que o parser aceitaria naquele ponto. Usado na mensagem de erro.
     */
    public List<String> esperados(NaoTerminal naoTerminal) {
        List<String> lista = new ArrayList<>();
        for (TokenType t : tabela.get(naoTerminal).keySet()) {
            lista.add(Simbolo.mostrar(t));
        }
        return lista;
    }

    /** Imprime a tabela no terminal, no mesmo formato do comentário lá em cima. */
    public void imprimir() {
        StringBuilder sb = new StringBuilder(String.format("%-5s", ""));
        for (TokenType t : TokenType.values()) {
            sb.append(String.format("%-9s", Simbolo.mostrar(t)));
        }
        System.out.println(sb);
        for (NaoTerminal n : NaoTerminal.values()) {
            sb = new StringBuilder(String.format("%-5s", n));
            for (TokenType t : TokenType.values()) {
                Producao p = get(n, t);
                sb.append(String.format("%-9s", p == null ? "-" : String.valueOf(p.numero())));
            }
            System.out.println(sb);
        }
    }
}