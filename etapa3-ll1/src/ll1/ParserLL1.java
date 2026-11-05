package ll1;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Analisador sintático LL(1) dirigido por tabela (parser preditivo não recursivo).
 *
 * Diferente da etapa 2 (recursivo), aqui não existe uma função por não-terminal.
 * Uma PILHA explícita guarda os símbolos que ainda faltam reconhecer, e a
 * TABELA LL(1) diz qual produção usar em cada momento.
 *
 * Algoritmo:
 *   pilha ← [$, P]                         ($ no fundo, P no topo)
 *   enquanto a pilha não estiver vazia:
 *     X ← topo da pilha;  a ← token atual
 *     se X é terminal (ou $):
 *       se X == a: desempilha X e avança para o próximo token ("casa")
 *       senão:     erro sintático
 *     se X é não-terminal:
 *       p ← M[X, a]
 *       se a célula está vazia: erro sintático
 *       senão: desempilha X e empilha o corpo de p DE TRÁS PARA FRENTE
 *   quando o $ casa com o EOF, a entrada é ACEITA.
 */
public class ParserLL1 {

    /** O "$" da teoria: no nosso lexer, é o token EOF. */
    private static final Simbolo FIM = Simbolo.t(TokenType.EOF);

    private final TabelaLL1 tabela = new TabelaLL1();
    private final List<Token> tokens = new ArrayList<>();
    private final boolean mostrarPassos;
    private int pos = 0; // índice do token atual dentro de "tokens"

    /**
     * Lê TODOS os tokens do lexer antes de começar a análise.
     * Assim dá para mostrar a "entrada restante" em cada passo.
     * Se houver erro léxico, a LexicalException já é lançada aqui.
     */
    public ParserLL1(Lexer lexer, boolean mostrarPassos) throws IOException {
        this.mostrarPassos = mostrarPassos;
        Token t;
        do {
            t = lexer.yylex();
            tokens.add(t);
        } while (t.getType() != TokenType.EOF);
    }

    /** Executa a análise. Termina normalmente se a entrada for aceita; senão lança SyntaxException. */
    public void parse() {
        // ArrayDeque é a pilha do Java: push empilha, pop desempilha, peek olha o topo
        Deque<Simbolo> pilha = new ArrayDeque<>();
        pilha.push(FIM);                          // $ no fundo
        pilha.push(Simbolo.nt(NaoTerminal.P));    // símbolo inicial no topo

        if (mostrarPassos) {
            System.out.printf("%-4s %-30s %-30s %s%n", "#", "PILHA (topo à direita)", "ENTRADA", "AÇÃO");
        }
        int passo = 1;

        while (!pilha.isEmpty()) {
            Simbolo topo = pilha.peek();
            Token token = tokens.get(pos);

            if (topo.ehTerminal()) {
                // --- topo é terminal: tem que ser igual ao token atual ---
                if (topo.terminal() != token.getType()) {
                    // erro tipo 1: esperava um terminal específico e veio outro
                    throw new SyntaxException(topo.toString(), token);
                }
                log(passo++, pilha, topo.equals(FIM) ? "ACEITA" : "casa '" + token.getLexeme() + "'");
                pilha.pop();
                if (!topo.equals(FIM)) {
                    pos++; // avança para o próximo token (depois do EOF não há mais nada)
                }
            } else {
                // --- topo é não-terminal: consulta a tabela M[topo, token] ---
                Producao p = tabela.get(topo.naoTerminal(), token.getType());
                if (p == null) {
                    // erro tipo 2: célula vazia, nenhuma produção serve para este token
                    throw new SyntaxException(String.join(" ou ", tabela.esperados(topo.naoTerminal())), token);
                }
                log(passo++, pilha, "M[" + topo + ", " + Simbolo.mostrar(token.getType()) + "] = " + p);
                pilha.pop();
                // Empilha o corpo DE TRÁS PARA FRENTE, para o primeiro símbolo ficar no topo.
                // Ex.: L → ( L'  empilha L' e depois (, e o ( fica no topo.
                // Se a produção for ε (corpo vazio), não empilha nada.
                for (int i = p.corpo().size() - 1; i >= 0; i--) {
                    pilha.push(p.corpo().get(i));
                }
            }
        }
    }

    /** Imprime uma linha do passo a passo: número | pilha | entrada restante | ação. */
    private void log(int passo, Deque<Simbolo> pilha, String acao) {
        if (!mostrarPassos) {
            return;
        }
        // descendingIterator percorre do fundo para o topo: "$ P ) E'" com o topo à direita
        List<String> simbolos = new ArrayList<>();
        pilha.descendingIterator().forEachRemaining(s -> simbolos.add(s.toString()));

        // tokens que ainda não foram consumidos (o EOF aparece como $)
        List<String> entrada = new ArrayList<>();
        for (int i = pos; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            entrada.add(t.getType() == TokenType.EOF ? "$" : t.getLexeme());
        }
        System.out.printf("%-4d %-30s %-30s %s%n", passo,
                cortar(String.join(" ", simbolos)), cortar(String.join(" ", entrada)), acao);
    }

    /** Se o texto passar de 30 caracteres, mostra só o final (a parte mais importante). */
    private static String cortar(String s) {
        return s.length() <= 30 ? s : "..." + s.substring(s.length() - 27);
    }
}