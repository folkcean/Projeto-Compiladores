package gerador;

import gerador.Gramatica.Producao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeradorTabela {

    private final Gramatica gramatica;
    private final FirstFollow ff;

    // linha (não-terminal) → coluna (terminal ou $) → produções naquela célula
    private final Map<String, Map<String, List<Producao>>> tabela = new LinkedHashMap<>();
    private final List<String> colunas = new ArrayList<>();

    public GeradorTabela(Gramatica gramatica, FirstFollow ff) {
        this.gramatica = gramatica;
        this.ff = ff;
        prepararTabela();
        montar();
    }

    private void prepararTabela() {
        colunas.addAll(gramatica.getTerminais());
        colunas.add(FirstFollow.FIM);

        for (String naoTerminal : gramatica.getNao_terminais()) {
            Map<String, List<Producao>> linha = new LinkedHashMap<>();
            for (String coluna : colunas) {
                linha.put(coluna, new ArrayList<>());
            }
            tabela.put(naoTerminal, linha);
        }
    }

    private void montar() {
        for (Producao p : gramatica.getProducoes()) {
            // regra 1: a produção serve quando o token começa o lado direito
            for (String terminal : ff.firstSequencia(p.direita())) {
                adicionar(p.esquerda(), terminal, p);
            }

            if (ff.anulavelSequencia(p.direita())) {
                for (String simbolo : ff.getFollow().get(p.esquerda())) {
                    adicionar(p.esquerda(), simbolo, p);
                }
            }
        }
    }

    private void adicionar(String naoTerminal, String terminal, Producao p) {
        List<Producao> celula = tabela.get(naoTerminal).get(terminal);
        if (!celula.contains(p)) {
            celula.add(p);
        }
    }

    public List<String> getConflitos() {
        List<String> conflitos = new ArrayList<>();
        for (String naoTerminal : tabela.keySet()) {
            for (String coluna : colunas) {
                List<Producao> celula = tabela.get(naoTerminal).get(coluna);
                if (celula.size() > 1) {
                    conflitos.add("M[" + naoTerminal + ", " + coluna + "] = " + numeros(celula));
                }
            }
        }
        return conflitos;
    }

    public boolean ehLL1() {
        return getConflitos().isEmpty();
    }

    // produções da célula M[naoTerminal, coluna]; lista vazia se a linha ou a coluna não existir.
    // O RodarTestes usa para comparar com a tabela feita à mão.
    public List<Producao> getCelula(String naoTerminal, String coluna) {
        Map<String, List<Producao>> linha = tabela.get(naoTerminal);
        if (linha == null || !linha.containsKey(coluna)) {
            return List.of();
        }
        return linha.get(coluna);
    }

    // mostra tudo na ordem em que a tabela é construída:
    // produções → FIRST → FOLLOW → tabela (com os conflitos marcados) → lista de conflitos
    public void imprimir() {
        int largura = larguraNaoTerminais();
        imprimirProducoes(largura);
        imprimirFirst(largura);
        imprimirFollow(largura);
        imprimirTabela(largura);
        imprimirConflitos();
    }

    private void imprimirProducoes(int largura) {
        System.out.println("\nPRODUÇÕES");
        for (Producao p : gramatica.getProducoes()) {
            String direita = p.direita().isEmpty() ? "ε" : String.join(" ", p.direita());
            System.out.printf("  %3s %-" + largura + "s -> %s%n", p.numero() + ":", p.esquerda(), direita);
        }
    }

    private void imprimirFirst(int largura) {
        System.out.println("\nFIRST  (ε = o não-terminal é anulável)");
        for (String naoTerminal : gramatica.getNao_terminais()) {
            boolean anulavel = ff.getAnulaveis().contains(naoTerminal);
            String nome = "FIRST(" + naoTerminal + ")";
            System.out.printf("  %-" + (largura + 7) + "s = %s%n", nome, conjunto(ff.getFirst().get(naoTerminal), anulavel));
        }
    }

    private void imprimirFollow(int largura) {
        System.out.println("\nFOLLOW  ($ = fim da entrada)");
        for (String naoTerminal : gramatica.getNao_terminais()) {
            String nome = "FOLLOW(" + naoTerminal + ")";
            System.out.printf("  %-" + (largura + 8) + "s = %s%n", nome, conjunto(ff.getFollow().get(naoTerminal), false));
        }
    }

    private void imprimirTabela(int largura) {
        int larguraLinha = largura + 2;

        // largura de cada coluna: o maior entre o nome e o texto das células
        Map<String, Integer> larguras = new LinkedHashMap<>();
        for (String coluna : colunas) {
            int larguraColuna = coluna.length();
            for (String naoTerminal : tabela.keySet()) {
                larguraColuna = Math.max(larguraColuna, textoCelula(tabela.get(naoTerminal).get(coluna)).length());
            }
            larguras.put(coluna, larguraColuna + 2);
        }

        // cabeçalho
        System.out.println("\nTABELA LL(1)");
        StringBuilder cabecalho = new StringBuilder("  " + String.format("%-" + larguraLinha + "s", "M"));
        for (String coluna : colunas) {
            cabecalho.append(String.format("%-" + larguras.get(coluna) + "s", coluna));
        }
        System.out.println(cabecalho.toString().stripTrailing());

        // uma linha por não-terminal
        for (String naoTerminal : tabela.keySet()) {
            StringBuilder linha = new StringBuilder("  " + String.format("%-" + larguraLinha + "s", naoTerminal));
            for (String coluna : colunas) {
                String texto = textoCelula(tabela.get(naoTerminal).get(coluna));
                linha.append(String.format("%-" + larguras.get(coluna) + "s", texto));
            }
            System.out.println(linha.toString().stripTrailing());
        }

        // legenda
        String legenda = "\n  - = célula vazia (erro sintático)";
        if (!ehLL1()) {
            legenda += "    * = conflito (mais de uma produção na célula)";
        }
        System.out.println(legenda);
    }

    private void imprimirConflitos() {
        List<String> conflitos = getConflitos();
        if (conflitos.isEmpty()) {
            System.out.println("\nSem conflitos: nenhuma célula tem mais de uma produção, então a gramática é LL(1).");
            return;
        }
        System.out.println("\nCONFLITOS: " + conflitos.size() + " célula(s) com mais de uma produção, então a gramática NÃO é LL(1).");
        for (String conflito : conflitos) {
            System.out.println("  " + conflito);
        }
    }

    // largura do maior nome de não-terminal (no mínimo a do "M" do cabeçalho)
    private int larguraNaoTerminais() {
        int largura = "M".length();
        for (String naoTerminal : tabela.keySet()) {
            largura = Math.max(largura, naoTerminal.length());
        }
        return largura;
    }

    // { (, NUMBER, ... } na ordem das colunas da tabela, e não na ordem em que os símbolos entraram no conjunto
    private String conjunto(Set<String> simbolos, boolean comVazio) {
        List<String> ordenados = new ArrayList<>();
        for (String coluna : colunas) {
            if (simbolos.contains(coluna)) {
                ordenados.add(coluna);
            }
        }
        if (comVazio) {
            ordenados.add("ε");
        }
        return ordenados.isEmpty() ? "{ }" : "{ " + String.join(", ", ordenados) + " }";
    }

    // o que aparece na tabela: "-", "5" ou "3,4*" (o * marca o conflito)
    private String textoCelula(List<Producao> celula) {
        return celula.size() > 1 ? numeros(celula) + "*" : numeros(celula);
    }

    // "2,3" para uma célula com as produções 2 e 3; "-" para célula vazia
    // (public static porque o RodarTestes usa o mesmo formato)
    public static String numeros(List<Producao> celula) {
        if(celula.isEmpty()){
            return "-";
        }
        List<String> nums = new ArrayList<>();
        for (Producao p : celula) {
            nums.add(String.valueOf(p.numero()));
        }
        return String.join(",", nums);
    }
}