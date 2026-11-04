# Projeto de Compiladores — Lisp

Projeto da disciplina de Compiladores (UENF), desenvolvido em três etapas mensais na metodologia de aprendizagem baseada em projetos. Cada etapa constrói de um compilador para uma gramática de Lisp, em Java.

| Etapa | Mês | O que faz | Pasta |
|---|---|---|---|
| 1 | Setembro | Analisador **léxico** gerado com JFlex | `etapa1-lexico/` |
| 2 | Outubro | Analisador **sintático recursivo descendente** | `etapa2-descendente/` |
| 3 | Novembro | **Tabela de análise sintática LL(1)** e parser dirigido por tabela | `etapa3-ll1/` |

Cada etapa é um projeto Java independente: tem o próprio `src/`, os próprios testes
e pode ser compilada e executada sozinha.

## Equipe

- **Anna Cecília Garcia** ([folkcean](https://github.com/folkcean))
- **Iarlo Henrique D. P. Pascoal** ([iarloo](https://github.com/iarloo))

## Estrutura do repositório

```
projeto-compiladores/
├── README.md
├── .gitignore
│
├── etapa1-lexico/
│   ├── lib/
│   │   └── jflex-full-1.9.1.jar         biblioteca do JFlex
│   ├── src/
│   │   ├── Lexer.flex                   especificação do léxico
│   │   ├── Lexer.java                   gerado pelo JFlex
│   │   └── Main.java                    imprime os tokens
│   └── testes/
│       └── teste_completo.lisp
│
├── etapa2-descendente/
│   ├── src/
│   │   ├── lexer/
│   │   │   ├── Lexer.flex
│   │   │   ├── Lexer.java               gerado pelo JFlex
│   │   │   ├── LexicalException.java
│   │   │   ├── LexicoTeste.java
│   │   │   ├── Token.java
│   │   │   └── TokenType.java
│   │   ├── parser/
│   │   │   ├── Parser.java              parser recursivo descendente
│   │   │   └── SyntaxException.java
│   │   ├── Main.java
│   │   └── RodarTestes.java
│   └── testes/                          arquivos .lisp de teste
│
└── etapa3-ll1/
    ├── docs/
    │   └── etapa3-LL1.md                FIRST, FOLLOW, tabela e exemplo de execução
    ├── gramatica/
    │   ├── lisp-original.txt
    │   ├── lisp-ll1.txt                 gramática lida pelo gerador
    │   └── exemplo-com-conflito.txt
    ├── src/
    │   ├── lexer/                       cópia do léxico da etapa 2
    │   ├── ll1/                         parser LL(1) com a tabela escrita à mão
    │   │   ├── NaoTerminal.java
    │   │   ├── Simbolo.java
    │   │   ├── Producao.java
    │   │   ├── TabelaLL1.java
    │   │   ├── ParserLL1.java
    │   │   └── SyntaxException.java
    │   ├── gerador/                     gera a tabela a partir da gramática (prova LL(1))
    │   │   ├── Gramatica.java
    │   │   ├── LeitorGramatica.java
    │   │   ├── FirstFollow.java
    │   │   ├── GeradorTabela.java
    │   │   └── MainGerador.java
    │   ├── Main.java
    │   └── RodarTestes.java
    └── testes/                          arquivos .lisp de teste
```

### Convenção dos testes

O nome do arquivo diz o resultado esperado, e o `RodarTestes` confere isso sozinho:

| Prefixo | Resultado esperado |
|---|---|
| `valido_` | `ACEITO` |
| `erro_sintatico_` | `ERRO SINTÁTICO` |
| `erro_lexico_` | `ERRO LÉXICO` |

## Requisitos

- **Java JDK 17 ou mais novo** (ex.: [Temurin](https://adoptium.net)). Confira com `java -version`.
- **VS Code** com a extensão **Extension Pack for Java** (Microsoft), ou qualquer outra IDE Java.

## Como rodar

> **Abra uma etapa por vez** no VS Code (*Arquivo → Abrir Pasta → `etapaX-...`*).
> As três etapas têm classes com o mesmo nome; abrindo a pasta raiz, a IDE se confunde.
>
> Todos os comandos abaixo são executados **de dentro da pasta da etapa**: o terminal
> tem que mostrar `...\projeto-compiladores\etapaX-...>`.

### Etapa 1: Léxico

```
javac -encoding UTF-8 -d bin src/*.java
java -cp bin Main                              # usa o texto de teste embutido no Main
java -cp bin Main testes/teste_completo.lisp   # lê um arquivo
```

Saída esperada: um token por linha (`NUMBER: 0`, `SYMBOL: abc`, `LPAREN`...) e,
no fim, `FIM DOS TESTES`.

### Etapa 2: Sintático recursivo descendente

```
javac -encoding UTF-8 -d bin src/*.java src/lexer/*.java src/parser/*.java
java -cp bin RodarTestes                         # roda todos os testes
java -cp bin Main testes/valido_soma_simples.lisp
java -cp bin lexer.LexicoTeste testes/valido_soma_simples.lisp   # só os tokens
```

Saída esperada do `RodarTestes`: `18 de 18 testes passaram.`
O `Main` imprime a árvore de derivação (`P`, `S`, `L`...) e `ACEITO` ou a mensagem de erro.

## Se você mudar o `Lexer.flex`

O `Lexer.java` é **gerado** a partir do `Lexer.flex`. Toda vez que o `.flex` mudar,
gere o `Lexer.java` de novo e compile outra vez.

**Etapa 1** (de dentro de `etapa1-lexico`):

```
java -jar lib/jflex-full-1.9.1.jar -d src src/Lexer.flex
```

**Etapas 2 e 3** (de dentro de `etapa2-descendente` ou `etapa3-ll1`). O `.jar` fica na
etapa 1, por isso o `../`:

```
java -jar ../etapa1-lexico/lib/jflex-full-1.9.1.jar -d src/lexer src/lexer/Lexer.flex
```

Tem que aparecer `Writing code to "src\...\Lexer.java"`. O JFlex guarda o arquivo antigo
como `Lexer.java~`; ele pode ser apagado (o `.gitignore` já o ignora).

> O léxico da etapa 3 é uma cópia do da etapa 2. Se mudar um, copie o `Lexer.flex`
> para o outro e gere de novo nos dois.