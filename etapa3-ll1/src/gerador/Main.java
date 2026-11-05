package gerador;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // sem argumentos: roda as duas gramáticas do projeto
        String[] arquivos = args.length > 0
                ? args
                : new String[]{"etapa3-ll1/docs/lisp-ll1.txt"};

        for (String arquivo : arquivos) {
            System.out.println("=".repeat(60));
            System.out.println("Gramática: " + arquivo);
            System.out.println("=".repeat(60));

            try {
                Gramatica gramatica = new Gramatica(arquivo);
                gramatica.carregar();

                FirstFollow ff = new FirstFollow(gramatica);

                GeradorTabela tabela = new GeradorTabela(gramatica, ff);

                tabela.imprimir();
                ff.imprimir();

            } catch (IOException e) {
                System.out.println("Erro ao ler " + arquivo + ": " + e.getMessage());
            }

            System.out.println();
        }
    }
}