package gerador;

import gerador.Gramatica.Producao;

import java.util.*;

public class FirstFollow {

    public static final String FIM = "$";

    private final Gramatica gramatica;
    private final Set<String> anulaveis = new LinkedHashSet<>();
    private final Map<String, Set<String>> first = new LinkedHashMap<>();
    private final Map<String, Set<String>> follow = new LinkedHashMap<>();

    public FirstFollow(Gramatica gramatica) {
        this.gramatica = gramatica;
        calcularAnulaveis();   // a ordem importa: FIRST usa anuláveis,
        calcularFirst();       // FOLLOW usa FIRST e anuláveis
        calcularFollow();
    }
    
    private void calcularAnulaveis() {
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (Producao p : gramatica.getProducoes()) {
                if (anulavelSequencia(p.direita()) && anulaveis.add(p.esquerda())) {
                    mudou = true;
                }
            }
        }
    }

    public boolean anulavelSequencia(List<String> simbolos) {
        boolean todosAnulaveis = true;
        for (String simbolo : simbolos) {
            if (!this.anulaveis.contains(simbolo)) {
                todosAnulaveis = false;
                break;
            }
        }
        return todosAnulaveis;
    }

    private void calcularFirst() {
        for (String nao_terminal : gramatica.getNao_terminais()) {
            first.put(nao_terminal, new LinkedHashSet<>());
        }

        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (Producao p : gramatica.getProducoes()) {
                Set<String> firstDireita = firstSequencia(p.direita());
                if (first.get(p.esquerda()).addAll(firstDireita)) {
                    mudou = true;
                }
            }
        }
    }

    public Set<String> firstSequencia(List<String> simbolos) {
        Set<String> resultado = new LinkedHashSet<>();
        for (String s : simbolos) {
            if (!ehNaoTerminal(s)) {
                resultado.add(s);          // terminal: ele é o começo
                break;
            }

            resultado.addAll(first.get(s));   // não-terminal: junta o FIRST dele

            if (!anulaveis.contains(s)) {
                break;                     // não pode sumir: ocupa o começo
            }
            // é anulável: pode sumir, então o próximo símbolo também pode começar
        }
        return resultado;
    }

    private void calcularFollow() {
        for (String simbolo_nao_terminal : gramatica.getNao_terminais()) {
            follow.put(simbolo_nao_terminal, new LinkedHashSet<>());
        }
        follow.get(gramatica.getInicial()).add(FIM);

        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (Producao p : gramatica.getProducoes()) {
                List<String> dir = p.direita();
                for (int i = 0; i < dir.size(); i++) {

                    String atual = dir.get(i);
                    if (!ehNaoTerminal(atual)) {
                        continue;
                    }
                    // S -> ( E )
                    List<String> resto = dir.subList(i + 1, dir.size());
                    Set<String> followAtual = follow.get(atual);

                    // regra 1: o que começa o resto vem logo depois de atual
                    if (followAtual.addAll(firstSequencia(resto))) {
                        mudou = true;
                    }

                    // regra 2: se o resto pode sumir, o que vem depois do
                    // lado esquerdo também vem depois de atual
                    if (anulavelSequencia(resto) && followAtual.addAll(follow.get(p.esquerda()))) {
                        mudou = true;
                    }
                }
            }
        }
    }

    private boolean ehNaoTerminal(String s) {
        return s.startsWith("<");
    }

    public Set<String> getAnulaveis() {
        return anulaveis;
    }

    public Map<String, Set<String>> getFirst() {
        return first;
    }

    public Map<String, Set<String>> getFollow() {
        return follow;
    }

    public void imprimir() {
        System.out.println("Anuláveis: " + anulaveis);
        System.out.println("\nFIRST:");
        first.forEach((nt, c) -> System.out.println("  " + nt + " = " + c));
        System.out.println("\nFOLLOW:");
        follow.forEach((nt, c) -> System.out.println("  " + nt + " = " + c));
    }
}