package lexer;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LexicoTeste {

    public static void main(String[] args) throws IOException {

        Lexer lexer = new Lexer(new FileReader(args[0], StandardCharsets.UTF_8));

        Token token;

        System.out.println("==========================================");
        System.out.println("      TESTE COMPLETO DO ANALISADOR");
        System.out.println("==========================================");
        System.out.println();

        while (true) {
            try {
                token = lexer.yylex();
            } catch (LexicalException e) {
                System.out.println("ERRO: " + e.getMessage()
                        + " (linha " + e.getLinha() + ", coluna " + e.getColuna() + ")");
                continue;
            }

            System.out.println(token);

            if (token.getType() == TokenType.EOF) {
                break;
            }
        }

        System.out.println();
        System.out.println("==========================================");
        System.out.println("              FIM DOS TESTES");
        System.out.println("==========================================");
    }
}