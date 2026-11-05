package ll1;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Uma produção da gramática:  cabeca → corpo
 *
 *   numero: a posição da produção na gramática (1 a 14); é o que vai na tabela
 *   cabeca: o não-terminal do lado esquerdo
 *   corpo:  a sequência de símbolos do lado direito
 *
 * A produção vazia (ε) é representada com o corpo vazio (lista sem símbolos).
 * Por isso, quando o parser aplica uma produção ε, ele não empilha nada.
 */
public record Producao(int numero, NaoTerminal cabeca, List<Simbolo> corpo) {

    public boolean ehVazia() {
        return corpo.isEmpty();
    }

    /** Ex.: "6. L' → E )"  ou  "10. E' → ε" */
    @Override
    public String toString() {
        String lado = ehVazia() ? "ε"
                : corpo.stream().map(Simbolo::toString).collect(Collectors.joining(" "));
        return numero + ". " + cabeca + " → " + lado;
    }
}