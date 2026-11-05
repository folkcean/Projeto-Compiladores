package gerador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Gramatica {

    private final Set<String> terminais = new LinkedHashSet<>();
    private final LeitorGramatica leitor;
    private final Set<String> nao_terminais = new LinkedHashSet<>();
    private final Set<Producao> producoes = new LinkedHashSet<>();
    private String inicial;

    public Gramatica(String caminho) throws IOException {
        this.leitor = new LeitorGramatica(caminho);
    }

    public void carregar() throws IOException {
        List<String> linhas = leitor.getLinhas();

        for (String linha : linhas) {
            if (linha.isBlank()) {
                continue;
            }
            String[] lados = linha.split("::=", 2);
            if (lados.length < 2) {
                throw new IllegalArgumentException("Linha sem :==" + linha);
            }


            String esquerda = (lados[0].trim());
            this.nao_terminais.add(esquerda);
            if (this.inicial == null) {
                inicial = esquerda;
            }

            String[] alternativas = lados[1].split(Pattern.quote("|"));

            for (String alternativa : alternativas) {
                alternativa = (alternativa.trim());
                List<String> direita = new ArrayList<>();

                if (!alternativa.equals("ε")) {
                    for (String simbolo : alternativa.split("\\s+")) {
                        if (simbolo.startsWith("<")) {
                            direita.add(simbolo);
                        } else {
                            String terminal = tirarAspas(simbolo);
                            this.terminais.add(terminal);
                            direita.add(terminal);
                        }
                    }
                }
                producoes.add(new Producao((producoes.size() + 1), esquerda, direita));
            }
        }
    }

    public Set<String> getTerminais() {
        return terminais;
    }

    public LeitorGramatica getLeitor() {
        return leitor;
    }

    public Set<String> getNao_terminais() {
        return nao_terminais;
    }

    public Set<Producao> getProducoes() {
        return producoes;
    }

    public String getInicial() {
        return inicial;
    }

    private String tirarAspas(String s) {
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public record Producao(int numero, String esquerda, List<String> direita) {
        @Override
        public String toString() {
            String dir = direita.isEmpty() ? "ε" : String.join(" ", direita);
            return numero + ": " + esquerda + " -> " + dir;
        }
    }


}
