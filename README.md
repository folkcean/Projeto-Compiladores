# Projeto de Compiladores — Lisp

Projeto da disciplina de Compiladores (UENF), desenvolvido em três etapas mensais na
metodologia de aprendizagem baseada em projetos. Cada etapa constrói uma parte de um
compilador para uma gramática de Lisp, em Java.

| Etapa | Mês | O que faz | Pasta | Situação |
|---|---|---|---|---|
| 1 | Setembro | Analisador **léxico** gerado com JFlex | `etapa1-lexico/` | ✅ concluída |
| 2 | Outubro | Analisador **sintático recursivo descendente** | `etapa2-descendente/` | ✅ concluída |
| 3 | Novembro | **Tabela de análise sintática LL(1)** e parser dirigido por tabela | `etapa3-ll1/` | 🚧 em desenvolvimento |

Cada etapa é um projeto Java independente: tem o próprio `src/`, os próprios testes
e pode ser compilada e executada sozinha.

## Equipe

- **Anna Cecília Garcia** ([folkcean](https://github.com/folkcean))
- **Iarlo Henrique D. P. Pascoal** ([iarloo](https://github.com/iarloo))

## Gramática

A gramática de Lisp dada pelo professor tem uma parte **léxica** e uma parte **sintática**.

A parte léxica (`<number>`, `<symbol>`, `<string>`, `<boolean>`, `<digits>`, `<letter>`...)
diz como os caracteres formam um token e foi implementada no analisador léxico (etapa 1).
Para o parser, cada uma dessas regras é um único terminal: `NUMBER`, `SYMBOL`, `STRING`
e `BOOLEAN`.

A parte sintática é:

```
<s-expr>   ::= <atom> | <list>
<list>     ::= "(" <elements> ")" | "(" ")"
<elements> ::= <s-expr> <elements> | <s-expr>
<atom>     ::= <number> | <symbol> | <string> | <boolean>
```

Os parsers das etapas 2 e 3 aceitam também **várias expressões no mesmo arquivo**
(e o arquivo vazio), com um símbolo inicial `P` = "zero ou mais expressões".

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
│   └── testes/                          18 arquivos .lisp de teste
│
└── etapa3-ll1/
    ├── docs/
    │   └── etapa3-LL1.md                gramática, FIRST, FOLLOW, tabela e exemplos   ✅
    ├── gramatica/                                                                     ⏳
    │   ├── lisp-original.txt            gramática do Classroom
    │   ├── lisp-ll1.txt                 gramática fatorada, lida pelo gerador
    │   └── exemplo-com-conflito.txt     gramática que não é LL(1), para teste
    ├── src/
    │   ├── lexer/                       cópia do léxico da etapa 2                    ✅
    │   ├── ll1/                         parser LL(1) com a tabela escrita à mão       ✅
    │   │   ├── NaoTerminal.java
    │   │   ├── Simbolo.java
    │   │   ├── Producao.java
    │   │   ├── TabelaLL1.java
    │   │   ├── ParserLL1.java
    │   │   └── SyntaxException.java
    │   ├── gerador/                     gera a tabela a partir da gramática           ⏳
    │   │   ├── Gramatica.java
    │   │   ├── LeitorGramatica.java
    │   │   ├── FirstFollow.java
    │   │   ├── GeradorTabela.java
    │   │   └── MainGerador.java
    │   ├── Main.java                    analisa um .lisp passo a passo                ✅
    │   └── RodarTestes.java             testes + compara as duas tabelas              ⏳
    └── testes/                          os mesmos 18 .lisp da etapa 2                 ✅
```

✅ feito · ⏳ a fazer

### Convenção dos testes

O nome do arquivo diz o resultado esperado, e o `RodarTestes` confere isso sozinho:

| Prefixo | Resultado esperado |
|---|---|
| `valido_` | `ACEITO` |
| `erro_sintatico_` | `ERRO SINTÁTICO` |
| `erro_lexico_` | `ERRO LÉXICO` |

## Requisitos

- **Java JDK 17 ou mais novo** (ex.: [Temurin](https://adoptium.net)). Confira com `java -version`.
- **Git** ([git-scm.com](https://git-scm.com)).
- **VS Code** com a extensão **Extension Pack for Java** (Microsoft), ou qualquer outra IDE Java.

## Como rodar

> **Abra uma etapa por vez** no VS Code (*Arquivo → Abrir Pasta → `etapaX-...`*).
> As etapas têm classes com o mesmo nome; abrindo a pasta raiz, a IDE se confunde.
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
java -cp bin RodarTestes                                         # roda todos os testes
java -cp bin Main testes/valido_soma_simples.lisp                # árvore de derivação
java -cp bin lexer.LexicoTeste testes/valido_soma_simples.lisp   # só os tokens
```

Saída esperada do `RodarTestes`: `18 de 18 testes passaram.`
O `Main` imprime a árvore de derivação (`P`, `S`, `L`...) e `ACEITO` ou a mensagem de erro.

### Etapa 3: LL(1)

Parte do parser LL(1) (já disponível):

```
javac -encoding UTF-8 -d bin src/Main.java src/lexer/*.java src/ll1/*.java
java -cp bin Main --tabela                              # gramática e tabela LL(1)
java -cp bin Main testes/valido_exemplo_docs.lisp       # passo a passo da pilha (40 passos)
java -cp bin Main testes/valido_lista_vazia.lisp        # mostra a fatoração: L' → )
```

Parte do gerador (quando estiver pronta):

```
javac -encoding UTF-8 -d bin src/gerador/*.java
java -cp bin gerador.MainGerador                                      # FIRST, FOLLOW e tabela
java -cp bin gerador.MainGerador gramatica/exemplo-com-conflito.txt   # detecta conflito
```

Tudo junto (quando as duas partes e o `RodarTestes` estiverem prontos):

```
javac -encoding UTF-8 -d bin src/*.java src/lexer/*.java src/ll1/*.java src/gerador/*.java
java -cp bin RodarTestes
```

Saída esperada: `34 de 34 testes passaram.` (18 arquivos `.lisp`, mais a verificação de
conflitos, das 14 produções e das células da tabela).

## Etapa 3: LL(1)

### Objetivo

Construir a **tabela de análise sintática LL(1)** da gramática do professor e um parser
que use essa tabela com uma **pilha explícita**, em vez das funções recursivas da etapa 2.
A explicação completa está em [`etapa3-ll1/docs/etapa3-LL1.md`](etapa3-ll1/docs/etapa3-LL1.md).

### Da gramática do professor à gramática LL(1)

A gramática do professor **não é LL(1)** do jeito que está escrita: as duas alternativas
de `<list>` começam com `(`, e as duas de `<elements>` começam com `<s-expr>`
(**prefixo comum**). Olhando só 1 token, o parser não sabe qual escolher.

A solução é a **fatoração à esquerda**: a parte comum é colocada em evidência e um
não-terminal novo representa o que vem depois.

```
L → ( E ) | ( )     ⟹    L  → ( L'          (L' = resto da lista)
                         L' → E ) | )

E → S E | S         ⟹    E  → S E'          (E' = mais elementos ou nada)
                         E' → E | ε
```

Com o `P` (várias expressões) no topo, a gramática LL(1) final tem 14 produções:

```
1. P  → S P         6. L' → E )         11. A → NUMBER
2. P  → ε           7. L' → )           12. A → SYMBOL
3. S  → A           8. E  → S E'        13. A → STRING
4. S  → L           9. E' → E           14. A → BOOLEAN
5. L  → ( L'        10. E' → ε
```

No código, `L'` e `E'` se chamam `L2` e `E2`, porque nomes em Java não podem ter apóstrofo.

### Tabela LL(1)

|   | ( | ) | NUMBER | SYMBOL | STRING | BOOLEAN | $ |
|---|---|---|---|---|---|---|---|
| **P** | 1 | – | 1 | 1 | 1 | 1 | 2 |
| **S** | 4 | – | 3 | 3 | 3 | 3 | – |
| **L** | 5 | – | – | – | – | – | – |
| **L'** | 6 | 7 | 6 | 6 | 6 | 6 | – |
| **E** | 8 | – | 8 | 8 | 8 | 8 | – |
| **E'** | 9 | 10 | 9 | 9 | 9 | 9 | – |
| **A** | – | – | 11 | 12 | 13 | 14 | – |

"–" = célula vazia = erro sintático. Nenhuma célula tem mais de uma produção, então a
gramática transformada **é LL(1)**.

### Divisão de tarefas

As duas partes não dependem uma da outra; só o `RodarTestes.java` usa as duas e é
feito no final.

**Anna: parser LL(1) e documentação (`src/ll1/`, `src/Main.java` e `docs/`)**
- [x] Representar não-terminais, símbolos e produções
- [x] Escrever a tabela LL(1) à mão em `TabelaLL1.java`
- [x] Implementar o parser com pilha (`ParserLL1.java`), mostrando pilha / entrada / ação a cada passo
- [x] Mensagens de erro com linha e coluna
- [x] `Main.java`: `--tabela` mostra a tabela; com um arquivo, mostra o passo a passo
- [x] Escrever `docs/etapa3-LL1.md`: gramática do professor, fatoração, FIRST, FOLLOW, tabela e exemplos

**Iarlo: gerador e prova (`src/gerador/` e `gramatica/`)**
- [ ] `gramatica/lisp-original.txt` com a gramática do Classroom
- [ ] `gramatica/lisp-ll1.txt` com as 14 produções **na mesma ordem** acima, usando os nomes `L2` e `E2`
- [ ] Ler uma gramática de um arquivo `.txt`
- [ ] Calcular FIRST e FOLLOW
- [ ] Montar a tabela e detectar conflitos (provar que a gramática é LL(1))
- [ ] Testar com uma gramática que tem conflito (`exemplo-com-conflito.txt`)

**Os dois, no final**
- [ ] `RodarTestes.java`: roda os `.lisp` no parser LL(1) e compara a tabela à mão com a gerada
- [ ] Rodar a etapa 2 e a etapa 3 com os mesmos testes e conferir que os resultados batem

---


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


## Ciclo de desenvolvimento (Git e GitHub)

A regra principal: **ninguém trabalha direto na `main`**. Cada tarefa é feita numa
*branch* (uma cópia paralela do projeto), enviada para o GitHub e juntada à `main`
por um *Pull Request* (PR), que o outro revisa.

```
clone (1 vez) → pull da main → cria branch → edita → add → commit → push → PR → revisão → merge → pull da main
```

### 1. Baixar o projeto (só uma vez)

```
git config --global user.name "seu-usuario"
git config --global user.email "seu-email-do-github"
git clone https://github.com/folkcean/Projeto-Compiladores.git
cd Projeto-Compiladores
```

O `clone` cria a pasta do projeto já ligada ao GitHub.

### 2. Começar uma tarefa

Sempre a partir da `main` atualizada:

```
git checkout main
git pull
git checkout -b etapa3/parser-ll1
```

- `checkout main`: vai para a linha principal.
- `pull`: baixa o que o outro já juntou.
- `checkout -b nome`: cria a sua branch e entra nela.

Sugestão de nomes: `etapa3/parser-ll1` (Anna), `etapa3/gerador` (Iarlo), `readme/ajustes`, `etapa2/corrige-lexer`.

### 3. Trabalhar e salvar versões

Edite os arquivos normalmente. A cada pedaço que funcionar:

```
git status
git add .
git commit -m "Mensagem descritiva, com verbos no imperativo"
```

### 4. Enviar a branch para o GitHub

Na primeira vez:

```
git push -u origin etapa3/parser-ll1
```

Nas próximas, só:

```
git push
```

### 5. Abrir o Pull Request

1. Abra o repositório no GitHub. Vai aparecer uma faixa amarela com o nome da sua branch → **Compare & pull request**.
2. Confira se está **base: `main`** ← **compare: `sua-branch`**.
3. Escreva um título e descreva o que foi feito e como testar (ex.: "rodei o `RodarTestes`, 34 de 34").
4. Em **Reviewers**, à direita, escolha o outro colaborador.
5. **Create pull request**.

### 6. Revisar o PR do outro

1. Aba **Pull requests** → abra o PR.
2. Aba **Files changed**: veja as mudanças linha a linha e comente se precisar.
3. Para testar no seu computador:
   ```
   git fetch
   git checkout etapa3/gerador
   ```
   compile, rode os testes e depois volte com `git checkout main`.
4. **Review changes** → **Approve** (aprovar) ou **Request changes** (pedir ajustes).

Se pedirem ajustes, quem abriu o PR corrige **na mesma branch**, faz `add`, `commit` e `push`.
O PR atualiza sozinho.

### 7. Juntar (merge) e limpar

1. Com o PR aprovado: **Merge pull request** → **Confirm merge** → **Delete branch**.
2. No computador dos dois:
   ```
   git checkout main
   git pull
   git branch -d etapa3/parser-ll1
   ```

### 8. Quando a `main` andou enquanto você trabalhava

Se o outro juntou algo na `main` antes de você terminar, traga essas mudanças para a sua branch:

```
git checkout main
git pull
git checkout etapa3/parser-ll1
git merge main
```

Se aparecer **conflito** (os dois mexeram na mesma linha), o VS Code marca o trecho com
`<<<<<<<`, `=======` e `>>>>>>>` e oferece os botões *Accept Current*, *Accept Incoming*
ou *Accept Both*. Escolha, salve, e finalize:

```
git add .
git commit -m "Resolve conflito com a main"
git push
```

### Resumo dos comandos

| Comando | Para quê |
|---|---|
| `git clone URL` | baixar o projeto pela primeira vez |
| `git status` | ver o que mudou |
| `git pull` | baixar as novidades da branch atual |
| `git checkout -b nome` | criar uma branch e entrar nela |
| `git checkout nome` | trocar de branch |
| `git add .` | marcar as mudanças |
| `git commit -m "msg"` | salvar uma versão |
| `git push` | enviar para o GitHub |
| `git merge main` | trazer a `main` para a sua branch |
| `git log --oneline` | ver o histórico de commits |
