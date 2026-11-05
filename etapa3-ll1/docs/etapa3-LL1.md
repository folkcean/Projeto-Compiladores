# Etapa 3 — Tabela de Análise Sintática LL(1) para Lisp

## 1. Objetivo

Construir a tabela de análise sintática LL(1) para a gramática de Lisp dada pelo
professor e implementar um analisador **preditivo não recursivo**: em vez das funções
recursivas da etapa 2, ele usa uma **pilha explícita** e consulta a **tabela** para
decidir qual produção aplicar.

**LL(1)** significa:

- **L**: a entrada é lida da esquerda para a direita (*Left-to-right*);
- **L**: constrói a derivação mais à esquerda (*Leftmost derivation*);
- **(1)**: cada decisão é tomada olhando apenas **1 token** à frente.

## 2. A gramática do professor

A gramática do Classroom tem duas partes.

**Parte léxica**: `<number>`, `<symbol>`, `<string>`, `<boolean>`, `<digits>`,
`<letter>`... Essas regras dizem como os **caracteres** formam um token e foram
implementadas no analisador léxico (etapa 1, `Lexer.flex`). Para o parser, cada uma
delas é um **único terminal**: `NUMBER`, `SYMBOL`, `STRING` e `BOOLEAN`.

**Parte sintática** (a que interessa para o LL(1)):

```
<s-expr>   ::= <atom> | <list>
<list>     ::= "(" <elements> ")" | "(" ")"
<elements> ::= <s-expr> <elements> | <s-expr>
<atom>     ::= <number> | <symbol> | <string> | <boolean>
```

Abreviando os nomes (S = s-expr, L = list, E = elements, A = atom):

```
S → A | L
L → ( E ) | ( )
E → S E | S
A → NUMBER | SYMBOL | STRING | BOOLEAN
```

Terminais: `(` `)` NUMBER SYMBOL STRING BOOLEAN e `$` (fim da entrada, o token `EOF`
do lexer).

## 3. A gramática do professor não é LL(1)

As duas alternativas de `L` começam com `(`, e as duas de `E` começam com `S`.
Isso é um **prefixo comum**: olhando só 1 token, o parser não tem como saber qual
alternativa escolher.

Numerando as produções (1. S → A, 2. S → L, 3. L → ( E ), 4. L → ( ), 5. E → S E,
6. E → S, 7 a 10. A → ...) e montando a tabela, aparecem conflitos:

|   | ( | ) | NUMBER | SYMBOL | STRING | BOOLEAN | $ |
|---|---|---|---|---|---|---|---|
| **S** | 2 | – | 1 | 1 | 1 | 1 | – |
| **L** | **3, 4** | – | – | – | – | – | – |
| **E** | **5, 6** | – | **5, 6** | **5, 6** | **5, 6** | **5, 6** | – |
| **A** | – | – | 7 | 8 | 9 | 10 | – |

Células com mais de uma produção = **conflito**. Portanto, a gramática, do jeito que
está escrita, **não é LL(1)**.

## 4. Transformações

### 4.1 Fatoração à esquerda

Quando duas alternativas de um não-terminal começam igual, a parte comum é colocada
em evidência e um não-terminal novo representa "o que vem depois":

```
X → α β₁ | α β₂        ⟹        X  → α X'
                                X' → β₁ | β₂
```

**Fatorando `L`** (prefixo comum: `(`):

```
L → ( E ) | ( )        ⟹        L  → ( L'
                                L' → E ) | )
```

L' é o "resto da lista": depois do `(`, ou vêm elementos e `)`, ou só `)`.
Agora a decisão é tomada **depois** de consumir o `(`, olhando o token seguinte.

**Fatorando `E`** (prefixo comum: `S`):

```
E → S E | S            ⟹        E  → S E'
                                E' → E | ε
```

E' é "o que vem depois de um elemento": mais elementos, ou nada (ε).

### 4.2 Extensão: várias expressões por arquivo

A gramática do professor tem como símbolo inicial `<s-expr>`, ou seja, reconhece
**uma** expressão. Como um arquivo Lisp normalmente tem várias expressões (e o parser
da etapa 2 já aceitava isso), foi acrescentado um símbolo inicial `P` (programa),
que significa "zero ou mais expressões":

```
P → S P | ε
```

A recursão é **à direita** (`S P`, e não `P S`): uma recursão à esquerda faria um
parser descendente expandir `P` infinitamente, sem consumir nenhum token.

### 4.3 Verificações

- **Sem recursão à esquerda:** nenhuma produção começa pelo próprio não-terminal.
- **Sem prefixo comum:** depois da fatoração, nenhum não-terminal tem duas alternativas
  que comecem pelo mesmo símbolo.

## 5. Gramática LL(1) final

| # | Produção | Origem |
|---|---|---|
| 1 | P → S P | extensão (várias expressões) |
| 2 | P → ε | extensão |
| 3 | S → A | `<s-expr> ::= <atom>` |
| 4 | S → L | `<s-expr> ::= <list>` |
| 5 | L → ( L' | `<list>`, fatorado |
| 6 | L' → E ) | `<list>`, fatorado |
| 7 | L' → ) | `<list>`, fatorado |
| 8 | E → S E' | `<elements>`, fatorado |
| 9 | E' → E | `<elements>`, fatorado |
| 10 | E' → ε | `<elements>`, fatorado |
| 11 | A → NUMBER | `<atom>` |
| 12 | A → SYMBOL | `<atom>` |
| 13 | A → STRING | `<atom>` |
| 14 | A → BOOLEAN | `<atom>` |

Não-terminais: P, S, L, L', E, E', A. Símbolo inicial: P.
No código, L' e E' se chamam `L2` e `E2`, porque nomes em Java não podem ter apóstrofo.

## 6. Conjuntos FIRST

FIRST(α) = conjunto dos terminais que podem aparecer **no início** de α
(mais ε, se α puder ser vazio).

- **FIRST(A)** = { NUMBER, SYMBOL, STRING, BOOLEAN }: produções 11 a 14.
- **FIRST(L)** = { ( }: a produção 5 começa com `(`.
- **FIRST(S)** = FIRST(A) ∪ FIRST(L) = { (, NUMBER, SYMBOL, STRING, BOOLEAN }.
- **FIRST(E)** = FIRST(S): a produção 8 começa com S, que nunca é vazio.
- **FIRST(E')** = FIRST(E) ∪ { ε } = { (, NUMBER, SYMBOL, STRING, BOOLEAN, ε }:
  produções 9 e 10.
- **FIRST(L')** = FIRST(E) ∪ { ) } = { (, ), NUMBER, SYMBOL, STRING, BOOLEAN }:
  a produção 6 começa com E; a 7 começa com `)`.
- **FIRST(P)** = FIRST(S) ∪ { ε } = { (, NUMBER, SYMBOL, STRING, BOOLEAN, ε }:
  produções 1 e 2.

## 7. Conjuntos FOLLOW

FOLLOW(X) = conjunto dos terminais que podem aparecer **logo depois** de X.

Regras:

1. `$` ∈ FOLLOW(símbolo inicial).
2. Em `A → α B β`: FIRST(β) − { ε } ⊆ FOLLOW(B).
3. Em `A → α B` (B no fim), ou em `A → α B β` com ε ∈ FIRST(β): FOLLOW(A) ⊆ FOLLOW(B).

Aplicando:

- **FOLLOW(P)** = { $ }: P é o símbolo inicial (regra 1). Em `P → S P`, P está no fim,
  o que só repete FOLLOW(P).
- **FOLLOW(E)** = { ) }: em `L' → E )`, E é seguido de `)` (regra 2). Em `E' → E`,
  E está no fim, o que traz FOLLOW(E'), que (veja abaixo) também é { ) }.
- **FOLLOW(E')** = FOLLOW(E) = { ) }: em `E → S E'`, E' está no fim (regra 3).
- **FOLLOW(S)**:
  - em `P → S P`: FIRST(P) − ε (regra 2) e, como P pode ser ε, também FOLLOW(P) = { $ } (regra 3);
  - em `E → S E'`: FIRST(E') − ε (regra 2) e, como E' pode ser ε, também FOLLOW(E) = { ) } (regra 3);
  - **resultado:** { (, ), NUMBER, SYMBOL, STRING, BOOLEAN, $ }.
- **FOLLOW(L)** = FOLLOW(S): em `S → L`, L está no fim.
- **FOLLOW(L')** = FOLLOW(L): em `L → ( L'`, L' está no fim.
- **FOLLOW(A)** = FOLLOW(S): em `S → A`, A está no fim.

### Resumo

| Não-terminal | FIRST | FOLLOW |
|---|---|---|
| P | ( NUMBER SYMBOL STRING BOOLEAN ε | $ |
| S | ( NUMBER SYMBOL STRING BOOLEAN | ( ) NUMBER SYMBOL STRING BOOLEAN $ |
| L | ( | ( ) NUMBER SYMBOL STRING BOOLEAN $ |
| L' | ( ) NUMBER SYMBOL STRING BOOLEAN | ( ) NUMBER SYMBOL STRING BOOLEAN $ |
| E | ( NUMBER SYMBOL STRING BOOLEAN | ) |
| E' | ( NUMBER SYMBOL STRING BOOLEAN ε | ) |
| A | NUMBER SYMBOL STRING BOOLEAN | ( ) NUMBER SYMBOL STRING BOOLEAN $ |

## 8. Construção da tabela

Para cada produção `X → α`:

1. para cada terminal `t` em FIRST(α), colocar a produção em M[X, t];
2. se ε ∈ FIRST(α), colocar a produção em M[X, t] para cada `t` em FOLLOW(X).

| # | Produção | FIRST(α) | Células |
|---|---|---|---|
| 1 | P → S P | FIRST(S) | M[P, (], M[P, NUMBER], M[P, SYMBOL], M[P, STRING], M[P, BOOLEAN] |
| 2 | P → ε | { ε } → FOLLOW(P) = { $ } | M[P, $] |
| 3 | S → A | FIRST(A) | M[S, NUMBER], M[S, SYMBOL], M[S, STRING], M[S, BOOLEAN] |
| 4 | S → L | { ( } | M[S, (] |
| 5 | L → ( L' | { ( } | M[L, (] |
| 6 | L' → E ) | FIRST(E) | M[L', (], M[L', NUMBER], M[L', SYMBOL], M[L', STRING], M[L', BOOLEAN] |
| 7 | L' → ) | { ) } | M[L', )] |
| 8 | E → S E' | FIRST(S) | M[E, (], M[E, NUMBER], M[E, SYMBOL], M[E, STRING], M[E, BOOLEAN] |
| 9 | E' → E | FIRST(E) | M[E', (], M[E', NUMBER], M[E', SYMBOL], M[E', STRING], M[E', BOOLEAN] |
| 10 | E' → ε | { ε } → FOLLOW(E') = { ) } | M[E', )] |
| 11 | A → NUMBER | { NUMBER } | M[A, NUMBER] |
| 12 | A → SYMBOL | { SYMBOL } | M[A, SYMBOL] |
| 13 | A → STRING | { STRING } | M[A, STRING] |
| 14 | A → BOOLEAN | { BOOLEAN } | M[A, BOOLEAN] |

## 9. Tabela LL(1)

|   | ( | ) | NUMBER | SYMBOL | STRING | BOOLEAN | $ |
|---|---|---|---|---|---|---|---|
| **P** | 1 | – | 1 | 1 | 1 | 1 | 2 |
| **S** | 4 | – | 3 | 3 | 3 | 3 | – |
| **L** | 5 | – | – | – | – | – | – |
| **L'** | 6 | 7 | 6 | 6 | 6 | 6 | – |
| **E** | 8 | – | 8 | 8 | 8 | 8 | – |
| **E'** | 9 | 10 | 9 | 9 | 9 | 9 | – |
| **A** | – | – | 11 | 12 | 13 | 14 | – |

"–" = célula vazia = erro sintático.

**Conclusão:** nenhuma célula tem mais de uma produção, portanto a gramática
transformada **é LL(1)**. A mesma tabela foi gerada automaticamente pelo programa
`gerador.MainGerador` a partir de `gramatica/lisp-ll1.txt`, e o `RodarTestes` confirma
que ela é idêntica à tabela escrita à mão em `TabelaLL1.java`.

## 10. Algoritmo do analisador

```
pilha ← [$, P]                      ($ no fundo, P no topo)
enquanto a pilha não estiver vazia:
    X ← topo da pilha;  a ← token atual
    se X é terminal (ou $):
        se X = a: desempilha X e avança para o próximo token
        senão:    ERRO
    se X é não-terminal:
        se M[X, a] está vazia: ERRO
        senão, sendo M[X, a] = X → Y₁ Y₂ … Yₖ:
            desempilha X e empilha Yₖ, …, Y₂, Y₁   (Y₁ fica no topo)
quando $ casa com o fim da entrada: ACEITA
```

- O corpo é empilhado **de trás para frente** porque a pilha é LIFO: assim o primeiro
  símbolo do corpo fica no topo e é o próximo a ser reconhecido.
- Numa produção ε, o não-terminal é desempilhado e nada é empilhado.
- Há dois tipos de erro: (1) o topo é um terminal diferente do token atual;
  (2) o topo é um não-terminal e a célula M[topo, token] está vazia.

## 11. Exemplo de execução: `(+ 1 (* 2 3))`

Saída de `java -cp bin Main testes/valido_exemplo_docs.lisp` (pilha com o topo à direita):

| # | Pilha | Entrada | Ação |
|---|---|---|---|
| 1 | $ P | ( + 1 ( * 2 3 ) ) $ | M[P, (] = 1. P → S P |
| 2 | $ P S | ( + 1 ( * 2 3 ) ) $ | M[S, (] = 4. S → L |
| 3 | $ P L | ( + 1 ( * 2 3 ) ) $ | M[L, (] = 5. L → ( L' |
| 4 | $ P L' ( | ( + 1 ( * 2 3 ) ) $ | casa ( |
| 5 | $ P L' | + 1 ( * 2 3 ) ) $ | M[L', SYMBOL] = 6. L' → E ) |
| 6 | $ P ) E | + 1 ( * 2 3 ) ) $ | M[E, SYMBOL] = 8. E → S E' |
| 7 | $ P ) E' S | + 1 ( * 2 3 ) ) $ | M[S, SYMBOL] = 3. S → A |
| 8 | $ P ) E' A | + 1 ( * 2 3 ) ) $ | M[A, SYMBOL] = 12. A → SYMBOL |
| 9 | $ P ) E' SYMBOL | + 1 ( * 2 3 ) ) $ | casa + |
| 10 | $ P ) E' | 1 ( * 2 3 ) ) $ | M[E', NUMBER] = 9. E' → E |
| 11 | $ P ) E | 1 ( * 2 3 ) ) $ | M[E, NUMBER] = 8. E → S E' |
| 12 | $ P ) E' S | 1 ( * 2 3 ) ) $ | M[S, NUMBER] = 3. S → A |
| 13 | $ P ) E' A | 1 ( * 2 3 ) ) $ | M[A, NUMBER] = 11. A → NUMBER |
| 14 | $ P ) E' NUMBER | 1 ( * 2 3 ) ) $ | casa 1 |
| 15 | $ P ) E' | ( * 2 3 ) ) $ | M[E', (] = 9. E' → E |
| 16 | $ P ) E | ( * 2 3 ) ) $ | M[E, (] = 8. E → S E' |
| 17 | $ P ) E' S | ( * 2 3 ) ) $ | M[S, (] = 4. S → L |
| 18 | $ P ) E' L | ( * 2 3 ) ) $ | M[L, (] = 5. L → ( L' |
| 19 | $ P ) E' L' ( | ( * 2 3 ) ) $ | casa ( |
| 20 | $ P ) E' L' | * 2 3 ) ) $ | M[L', SYMBOL] = 6. L' → E ) |
| 21 | $ P ) E' ) E | * 2 3 ) ) $ | M[E, SYMBOL] = 8. E → S E' |
| 22 | $ P ) E' ) E' S | * 2 3 ) ) $ | M[S, SYMBOL] = 3. S → A |
| 23 | $ P ) E' ) E' A | * 2 3 ) ) $ | M[A, SYMBOL] = 12. A → SYMBOL |
| 24 | $ P ) E' ) E' SYMBOL | * 2 3 ) ) $ | casa * |
| 25 | $ P ) E' ) E' | 2 3 ) ) $ | M[E', NUMBER] = 9. E' → E |
| 26 | $ P ) E' ) E | 2 3 ) ) $ | M[E, NUMBER] = 8. E → S E' |
| 27 | $ P ) E' ) E' S | 2 3 ) ) $ | M[S, NUMBER] = 3. S → A |
| 28 | $ P ) E' ) E' A | 2 3 ) ) $ | M[A, NUMBER] = 11. A → NUMBER |
| 29 | $ P ) E' ) E' NUMBER | 2 3 ) ) $ | casa 2 |
| 30 | $ P ) E' ) E' | 3 ) ) $ | M[E', NUMBER] = 9. E' → E |
| 31 | $ P ) E' ) E | 3 ) ) $ | M[E, NUMBER] = 8. E → S E' |
| 32 | $ P ) E' ) E' S | 3 ) ) $ | M[S, NUMBER] = 3. S → A |
| 33 | $ P ) E' ) E' A | 3 ) ) $ | M[A, NUMBER] = 11. A → NUMBER |
| 34 | $ P ) E' ) E' NUMBER | 3 ) ) $ | casa 3 |
| 35 | $ P ) E' ) E' | ) ) $ | M[E', )] = 10. E' → ε |
| 36 | $ P ) E' ) | ) ) $ | casa ) |
| 37 | $ P ) E' | ) $ | M[E', )] = 10. E' → ε |
| 38 | $ P ) | ) $ | casa ) |
| 39 | $ P | $ | M[P, $] = 2. P → ε |
| 40 | $ | $ | **ACEITA** |

Observe os passos 35 e 37: o próximo token é `)`, que está em FOLLOW(E'), e por isso
E' vira ε. É assim que o parser descobre que os elementos da lista acabaram.

### Lista vazia: `()`

| # | Pilha | Entrada | Ação |
|---|---|---|---|
| 1 | $ P | ( ) $ | M[P, (] = 1. P → S P |
| 2 | $ P S | ( ) $ | M[S, (] = 4. S → L |
| 3 | $ P L | ( ) $ | M[L, (] = 5. L → ( L' |
| 4 | $ P L' ( | ( ) $ | casa ( |
| 5 | $ P L' | ) $ | M[L', )] = 7. L' → ) |
| 6 | $ P ) | ) $ | casa ) |
| 7 | $ P | $ | M[P, $] = 2. P → ε |
| 8 | $ | $ | **ACEITA** |

No passo 5 aparece o efeito da fatoração: o `(` já foi consumido, e só então o
parser escolhe entre `L' → E )` e `L' → )`, olhando o token seguinte.

## 12. Exemplo de erro: `(a b`

Os passos 1 a 14 são análogos ao exemplo anterior. Depois de casar `a` e `b`:

| # | Pilha | Entrada | Ação |
|---|---|---|---|
| 14 | $ P ) E' SYMBOL | b $ | casa b |
| — | $ P ) E' | $ | M[E', $] está **vazia** → ERRO |

Saída:

```
ERRO SINTÁTICO: esperado ( ou ) ou NUMBER ou SYMBOL ou STRING ou BOOLEAN,
encontrado fim do arquivo (linha 1, coluna 5)
```

A mensagem lista os tokens que têm produção na linha E' da tabela.

## 13. Comparação com a etapa 2

O parser recursivo da etapa 2 e o parser LL(1) tomam **as mesmas decisões**; a
diferença é onde elas ficam:

| | Etapa 2 (recursivo descendente) | Etapa 3 (LL(1) por tabela) |
|---|---|---|
| Decisões | `if` e `while` dentro das funções | células da tabela M |
| "O que falta reconhecer" | pilha de chamadas do Java | pilha explícita |
| Prefixo comum de `( E ) \| ( )` | resolvido no código: `parseList` consome o `(` antes de decidir | resolvido na gramática: fatoração (L → ( L') |

Os dois parsers foram executados sobre os mesmos 18 arquivos de teste e aceitam e
rejeitam exatamente os mesmos arquivos. As mensagens de erro podem ser diferentes,
porque cada um detecta o erro num momento diferente (a etapa 3 detecta assim que
encontra uma célula vazia).
