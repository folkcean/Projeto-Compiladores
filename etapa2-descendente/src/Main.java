import lexer.Lexer;
import lexer.LexicalException;
import parser.Parser;
import parser.SyntaxException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws IOException {


        if (args.length != 1) {
            System.out.println("uso: java Main arquivo.lisp");
            System.exit(1);
        }

        Lexer lexer = new Lexer(new FileReader(args[0], StandardCharsets.UTF_8));

        try {
            // o construtor já lê o primeiro token, então ele também fica no try
            Parser parser = new Parser(lexer);
            parser.parseProgram();
            System.out.println("ACEITO");
        } catch (LexicalException e) {
            System.out.println("ERRO LÉXICO: " + e.getMessage()
                    + " (linha " + e.getLinha() + ", coluna " + e.getColuna() + ")");
        } catch (SyntaxException e) {
            // a mensagem do SyntaxException já traz linha e coluna
            System.out.println("ERRO SINTÁTICO: " + e.getMessage());
        }
    }
}