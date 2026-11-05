package gerador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LeitorGramatica {
    private final String filePath;

    public LeitorGramatica(String caminho) throws IOException {
        this.filePath = caminho;
    }

    public List<String> getLinhas() throws IOException {
        List<String> linhas = Files.readAllLines(Paths.get(filePath));
        return linhas;
    }
    
}
