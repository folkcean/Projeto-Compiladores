package lexer;

public class LexicalException extends RuntimeException {
    private final int linha;
    private final int coluna;

    public LexicalException(String mensagem, int linha, int coluna) {
        super(mensagem);
        this.linha = linha;
        this.coluna = coluna;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }
}