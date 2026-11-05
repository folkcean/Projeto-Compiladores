import lexer.Lexer;
import lexer.LexicalException;
import parser.Parser;
import parser.SyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Roda todos os arquivos de testes/ e confere o resultado pelo nome:
 *   valido_*.lisp          -> tem que dar ACEITO
 *   erro_sintatico_*.lisp  -> tem que dar ERRO SINTÁTICO
 *   erro_lexico_*.lisp     -> tem que dar ERRO LÉXICO
 */
public class RodarTestes {

    public static void main(String[] args) throws IOException {
        File[] arquivos = new File("etapa2-descendente/testes").listFiles((d, nome) -> nome.endsWith(".lisp"));
        if (arquivos == null || arquivos.length == 0) {
            System.out.println("Nenhum teste encontrado. Rode a partir da pasta da etapa (etapa2-descendente).");
            System.exit(1);
        }
        Arrays.sort(arquivos);

        int passou = 0;
        for (File f : arquivos) {
            String nome = f.getName();
            String esperado;
            if (nome.startsWith("valido_")) {
                esperado = "ACEITO";
            } else if (nome.startsWith("erro_sintatico_")) {
                esperado = "ERRO SINTÁTICO";
            } else if (nome.startsWith("erro_lexico_")) {
                esperado = "ERRO LÉXICO";
            } else {
                esperado = "?";
            }
            String obtido = analisar(f.getPath());
            boolean ok = obtido.startsWith(esperado);
            if (ok) passou++;
            System.out.printf("%-7s %-45s %s%n", ok ? "PASSOU" : "FALHOU", f.getName(), obtido);
        }

        System.out.println();
        System.out.println(passou + " de " + arquivos.length + " testes passaram.");
        if (passou != arquivos.length) {
            System.exit(1);
        }
    }

    private static String analisar(String arquivo) throws IOException {
        // o Parser imprime a árvore no System.out; aqui ela é descartada
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try (FileReader leitor = new FileReader(arquivo, StandardCharsets.UTF_8)) {
            new Parser(new Lexer(leitor)).parseProgram();
            return "ACEITO";
        } catch (LexicalException e) {
            return "ERRO LÉXICO: " + e.getMessage()
                    + " (linha " + e.getLinha() + ", coluna " + e.getColuna() + ")";
        } catch (SyntaxException e) {
            return "ERRO SINTÁTICO: " + e.getMessage();
        } finally {
            System.setOut(original);
        }
    }
}