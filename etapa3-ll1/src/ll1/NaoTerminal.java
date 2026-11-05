package ll1;

/**
 * Não-terminais da gramática LL(1) de Lisp.
 *
 * Vêm da gramática do professor (Classroom), depois da fatoração à esquerda:
 *   <s-expr>   -> S
 *   <list>     -> L   e  L' (o "resto da lista", criado na fatoração)
 *   <elements> -> E   e  E' (os "outros elementos", criado na fatoração)
 *   <atom>     -> A
 * mais o P (programa), que permite várias expressões num mesmo arquivo.
 *
 * Em Java um nome não pode ter apóstrofo, então L' e E' se chamam L2 e E2
 * no código; o toString() devolve "L'" e "E'" para a saída ficar igual à teoria.
 */
public enum NaoTerminal {
    P("P"),    // programa: zero ou mais expressões
    S("S"),    // <s-expr>: uma expressão (átomo ou lista)
    L("L"),    // <list>: começa sempre com "("
    L2("L'"),  // resto da lista, depois do "(": elementos e ")" ou só ")"
    E("E"),    // <elements>: uma ou mais expressões
    E2("E'"),  // depois de uma expressão: mais elementos ou nada (ε)
    A("A");    // <atom>: número, símbolo, string ou booleano

    private final String nome;

    NaoTerminal(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}