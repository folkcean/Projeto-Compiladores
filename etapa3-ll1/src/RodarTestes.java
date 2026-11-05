import gerador.FirstFollow;
import gerador.GeradorTabela;
import gerador.Gramatica;
import lexer.TokenType;
import ll1.NaoTerminal;
import ll1.Producao;
import ll1.Simbolo;
import ll1.TabelaLL1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Testes da etapa 3, em duas partes.
 *
 * 1. Parser LL(1): roda cada arquivo de testes/ e confere o resultado pelo nome
 *      valido_*.lisp          -> tem que dar ACEITO
 *      erro_sintatico_*.lisp  -> tem que dar ERRO SINTÁTICO
 *      erro_lexico_*.lisp     -> tem que dar ERRO LÉXICO
 *
 * 2. Tabela à mão × tabela gerada: passa o docs/lisp-ll1.txt pelo gerador e confere que
 *      - a tabela gerada não tem conflitos;
 *      - cada uma das 14 produções é igual à do TabelaLL1 (mesmo número, mesmos símbolos);
 *      - as 49 células (7 não-terminais × 7 colunas) são iguais às do TabelaLL1.
 *
 * Saída esperada: "34 de 34 testes passaram." (18 arquivos + 1 + 14 + 1)
 */
public class RodarTestes {

    private static final String GRAMATICA = "docs/lisp-ll1.txt";

    // o gerador usa os nomes do .txt; o TabelaLL1 usa o enum NaoTerminal
    private static final Map<String, NaoTerminal> NOMES = Map.of(
            "<program>", NaoTerminal.P,
            "<s-expr>", NaoTerminal.S,
            "<list>", NaoTerminal.L,
            "<list-rest>", NaoTerminal.L2,
            "<elements>", NaoTerminal.E,
            "<elements-rest>", NaoTerminal.E2,
            "<atom>", NaoTerminal.A
    );

    private static int total = 0;
    private static int passou = 0;

    public static void main(String[] args) throws IOException {
        testarParser();
        testarTabela();

        System.out.println();
        System.out.println(passou + " de " + total + " testes passaram.");
        if (passou != total) {
            System.exit(1);
        }
    }

    // ---------- parte 1: os .lisp no parser LL(1) ----------

    private static void testarParser() throws IOException {
        System.out.println("PARSER LL(1): arquivos de testes/");

        File[] arquivos = achar("testes").toFile().listFiles((d, nome) -> nome.endsWith(".lisp"));
        if (arquivos == null || arquivos.length == 0) {
            resultado(false, "testes/", "nenhum .lisp encontrado (rode de etapa3-ll1 ou da raiz do repositório)");
            return;
        }
        Arrays.sort(arquivos);

        for (File f : arquivos) {
            String esperado = esperado(f.getName());
            // o Main.analisar é o mesmo do "java Main arquivo.lisp", sem mostrar a pilha
            String obtido = Main.analisar(f.getPath(), false);
            resultado(obtido.startsWith(esperado), f.getName(), obtido);
        }
    }

    // o prefixo do nome diz o resultado esperado; um nome fora do padrão sempre falha
    private static String esperado(String nome) {
        if (nome.startsWith("valido_")) {
            return "ACEITO";
        }
        if (nome.startsWith("erro_sintatico_")) {
            return "ERRO SINTÁTICO";
        }
        if (nome.startsWith("erro_lexico_")) {
            return "ERRO LÉXICO";
        }
        return "?";
    }

    // ---------- parte 2: tabela à mão × tabela gerada ----------

    private static void testarTabela() {
        Path caminho = achar(GRAMATICA);
        System.out.println();
        System.out.println("TABELA À MÃO × TABELA GERADA (" + caminho + ")");

        Gramatica gramatica;
        try {
            gramatica = new Gramatica(caminho.toString());
            gramatica.carregar();
        } catch (IOException e) {
            resultado(false, "leitura da gramática", "não foi possível ler " + caminho);
            return;
        }
        GeradorTabela gerada = new GeradorTabela(gramatica, new FirstFollow(gramatica));
        TabelaLL1 manual = new TabelaLL1();

        // 1. a tabela gerada não pode ter conflitos
        List<String> conflitos = gerada.getConflitos();
        resultado(conflitos.isEmpty(), "tabela gerada sem conflitos",
                conflitos.isEmpty() ? "a gramática é LL(1)" : String.join("; ", conflitos));

        // 2. as produções, uma por uma, pelo número
        List<Gramatica.Producao> geradas = new ArrayList<>(gramatica.getProducoes());
        for (Producao p : TabelaLL1.PRODUCOES) {
            String nome = "produção " + p.numero();
            int i = p.numero() - 1;
            if (i >= geradas.size()) {
                resultado(false, nome, "à mão " + p + ", gerada: não existe");
                continue;
            }
            String traduzida = traduzir(geradas.get(i));
            boolean ok = traduzida.equals(p.toString());
            resultado(ok, nome, ok ? p.toString() : "à mão " + p + ", gerada " + traduzida);
        }

        // 3. as células, uma por uma
        List<String> diferencas = new ArrayList<>();
        int celulas = 0;
        for (NaoTerminal n : NaoTerminal.values()) {
            for (TokenType t : TokenType.values()) {
                celulas++;
                Producao p = manual.get(n, t);
                String aMao = p == null ? "-" : String.valueOf(p.numero());
                String gerado = GeradorTabela.numeros(gerada.getCelula(nomeNoGerador(n), Simbolo.mostrar(t)));
                if (!aMao.equals(gerado)) {
                    diferencas.add("M[" + n + ", " + Simbolo.mostrar(t) + "]: à mão " + aMao + ", gerada " + gerado);
                }
            }
        }
        boolean ok = diferencas.isEmpty();
        resultado(ok, "células da tabela",
                ok ? celulas + " de " + celulas + " iguais" : diferencas.size() + " de " + celulas + " diferentes");
        for (String d : diferencas) {
            System.out.println("        " + d);
        }
    }

    // "6: <list-rest> -> <elements> )" vira "6. L' → E )", o formato do ll1.Producao,
    // para dar para comparar com o TabelaLL1 direto pelo texto
    private static String traduzir(Gramatica.Producao p) {
        List<String> direita = new ArrayList<>();
        for (String simbolo : p.direita()) {
            direita.add(traduzirSimbolo(simbolo));
        }
        String lado = direita.isEmpty() ? "ε" : String.join(" ", direita);
        return p.numero() + ". " + traduzirSimbolo(p.esquerda()) + " → " + lado;
    }

    // não-terminal: <list-rest> vira L'. Terminal: já tem o mesmo nome nos dois ( (, ), NUMBER, ... )
    private static String traduzirSimbolo(String simbolo) {
        NaoTerminal n = NOMES.get(simbolo);
        return n == null ? simbolo : n.toString();
    }

    // o caminho inverso: L2 vira <list-rest>
    private static String nomeNoGerador(NaoTerminal n) {
        for (Map.Entry<String, NaoTerminal> e : NOMES.entrySet()) {
            if (e.getValue() == n) {
                return e.getKey();
            }
        }
        return null;
    }

    // ---------- utilitários ----------

    private static void resultado(boolean ok, String nome, String detalhe) {
        total++;
        if (ok) {
            passou++;
        }
        System.out.printf("%-7s %-45s %s%n", ok ? "PASSOU" : "FALHOU", nome, detalhe);
    }

    // O IntelliJ roda a partir da raiz do repositório; o terminal, de dentro de etapa3-ll1.
    // Se o caminho não existir onde o programa está rodando, tenta dentro de etapa3-ll1/.
    private static Path achar(String caminho) {
        Path aqui = Path.of(caminho);
        if (Files.exists(aqui)) {
            return aqui;
        }
        return Path.of("etapa3-ll1", caminho);
    }
}