import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws IOException {

        String entrada = """
                0
                1
                123
                999999

                +1
                -1
                +123
                -123

                3.14
                0.5
                123.456
                +3.14
                -3.14
                +0.5
                -0.5

                +
                -
                *
                /
                =
                <
                >
                !
                ?

                abc
                ABC
                hello
                HELLO
                soma
                resultado
                teste123

                soma+
                soma-
                soma*
                soma/
                soma=
                soma<
                soma>
                soma!
                soma?
                x+y
                x-y
                x*y
                x/y
                x=10
                a1b2

                T
                NIL
                #t
                #f

                "hello"
                "Olá mundo"
                "123"
                "uma string com espaços"
                "T"
                "NIL"
                "#t"

                ()

                (+ 10 20)
                (- 10 5)
                (* 5 3)
                (/ 10 2)
                (= 10 10)
                (< 5 10)
                (> 10 5)

                (soma 10 20)
                (resultado +1)
                (teste abc)
                (x y z)

                (T NIL)
                (#t #f)
                (T #t NIL #f)

                ("hello")
                ("Olá mundo" "teste")
                (nome "Anna")

                (+ (* 2 3) (- 10 4))
                (soma (+ 1 2) (* 3 4))
                ((+ 1 2) (- 5 3))
                (a (b (c 123)))

                +
                -
                +1
                -1
                +123
                -123
                +3.14
                -3.14
                abc
                abc123
                +abc
                -abc
                +a
                -a
                +1abc
                -1abc

                123.
                .123
                .
                +.
                -.
                +.5
                -.5

                a
                Z
                a1
                1a
                abc123
                +a
                -a
                *a
                /a
                =a
                <a
                >a
                !a
                ?a

                @
                $
                %
                &
                _
                :
                ;
                ,
                """;

        // Sem argumento: usa o texto acima. Com argumento: lê o arquivo (ex.: testes/teste_completo.lisp).
        Reader leitor = args.length > 0
                ? new FileReader(args[0], StandardCharsets.UTF_8)
                : new StringReader(entrada);

        Lexer lexer = new Lexer(leitor);

        String token;

        System.out.println("==========================================");
        System.out.println("      TESTE COMPLETO DO ANALISADOR");
        System.out.println("==========================================");
        System.out.println();

        while ((token = lexer.yylex()) != null) {
            System.out.println(token);
        }

        System.out.println();
        System.out.println("==========================================");
        System.out.println("              FIM DOS TESTES");
        System.out.println("==========================================");
    }
}