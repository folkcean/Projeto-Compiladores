import lexer.Lexer;
import lexer.LexicalException;
import ll1.ParserLL1;
import ll1.SyntaxException;
import ll1.TabelaLL1;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Ponto de entrada da etapa 3 (parser LL(1)).
 *
 * Uso:
 *   java -cp bin Main --tabela        mostra a gramática e a tabela LL(1)
 *   java -cp bin Main arquivo.lisp    analisa o arquivo mostrando cada passo da pilha
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("uso: java -cp bin Main arquivo.lisp");
            System.out.println("     java -cp bin Main --tabela");
            System.exit(1);
        }

        if (args[0].equals("--tabela")) {
            System.out.println("GRAMÁTICA LL(1)");
            TabelaLL1.PRODUCOES.forEach(p -> System.out.println("  " + p));
            System.out.println();
            System.out.println("TABELA LL(1)");
            new TabelaLL1().imprimir();
            return;
        }

        System.out.println(analisar(args[0], true));
    }

    /**
     * Analisa um arquivo e DEVOLVE o resultado como texto:
     * "ACEITO", "ERRO LÉXICO: ..." ou "ERRO SINTÁTICO: ...".
     *
     * Fica separado do main porque o RodarTestes também usa,
     * com mostrarPassos = false, para não imprimir a pilha de cada teste.
     */
    public static String analisar(String arquivo, boolean mostrarPassos) throws IOException {
        try (FileReader leitor = new FileReader(arquivo, StandardCharsets.UTF_8)) {
            new ParserLL1(new Lexer(leitor), mostrarPassos).parse();
            return "ACEITO";
        } catch (LexicalException e) {
            // erro do lexer (caractere inválido, string sem fechar, número malformado...)
            return "ERRO LÉXICO: " + e.getMessage()
                    + " (linha " + e.getLinha() + ", coluna " + e.getColuna() + ")";
        } catch (SyntaxException e) {
            // erro do parser (terminal que não casou ou célula vazia da tabela)
            return "ERRO SINTÁTICO: " + e.getMessage();
        }
    }
}