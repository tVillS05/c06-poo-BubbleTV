package servico;

import excecao.DadoInvalidoException;
import excecao.MidiaDuplicadaException;
import excecao.MidiaNaoEncontradaException;
import modelo.Episodio;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Catalogo {

    // Catálogo fixo: filmes e séries que sempre estão disponíveis
    private static final String ARQUIVO_BASE = "catalogo-base.txt";
    // Tudo que o usuário cadastra pelo programa é salvo aqui
    private static final String ARQUIVO = "catalogo.txt";

    // Array com contador: só as primeiras "qtdMidias" posições estão preenchidas
    private Midia[] midias = new Midia[50];
    private int qtdMidias = 0;

    // Quantas mídias vieram do catálogo base (ocupam as primeiras posições do array)
    private int qtdBase = 0;

    // Caminho completo dos dois arquivos no computador
    private Path caminhoBase;
    private Path caminhoUsuario;

    // Problemas encontrados ao ler os arquivos (linhas ignoradas)
    private String avisos = "";

    public Catalogo() {
        caminhoBase = localizar(ARQUIVO_BASE);

        caminhoUsuario = localizar(ARQUIVO);
        // Se o catalogo.txt ainda não existe, ele será criado ao lado do catalogo-base.txt
        if (!Files.exists(caminhoUsuario) && Files.exists(caminhoBase)) {
            caminhoUsuario = caminhoBase.toAbsolutePath().getParent().resolve(ARQUIVO);
        }
    }

    // Procura o arquivo 1) na pasta onde o programa foi executado e
    // 2) subindo pelas pastas a partir de onde o programa está compilado.
    // Se não achar, devolve o caminho da pasta atual.
    private Path localizar(String nome) {
        Path naPastaAtual = Paths.get(nome).toAbsolutePath();
        if (Files.exists(naPastaAtual)) {
            return naPastaAtual;
        }

        try {
            Path pasta = Paths.get(Catalogo.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            for (int i = 0; i < 6 && pasta != null; i++) {
                Path candidato = pasta.resolve(nome);
                if (Files.exists(candidato)) {
                    return candidato;
                }
                pasta = pasta.getParent();
            }
        } catch (Exception e) {
            // Não conseguiu descobrir onde o programa está: usa a pasta atual
        }

        return naPastaAtual;
    }

    // Cadastro "oficial": recusa título repetido
    public void adicionar(Midia m) throws MidiaDuplicadaException {
        verificarDuplicado(m.getTitulo());
        inserir(m);
    }

    // Lança exceção se já existir uma mídia com esse título (ignora maiúsculas/minúsculas)
    public void verificarDuplicado(String titulo) throws MidiaDuplicadaException {
        for (int i = 0; i < qtdMidias; i++) {
            if (midias[i].getTitulo().equalsIgnoreCase(titulo.trim())) {
                throw new MidiaDuplicadaException(titulo.trim());
            }
        }
    }

    // Coloca no array sem checar nada (usado na leitura dos arquivos)
    private void inserir(Midia m) {
        // Array cheio: cria um com o dobro do tamanho e copia o conteúdo
        if (qtdMidias == midias.length) {
            midias = Arrays.copyOf(midias, midias.length * 2);
        }
        midias[qtdMidias] = m;
        qtdMidias++;
    }

    // Busca por parte do título (ignora maiúsculas/minúsculas).
    // Se existir um título exatamente igual, devolve só ele.
    public Midia[] buscar(String termo) throws MidiaNaoEncontradaException {
        String t = termo.trim().toLowerCase();
        Midia[] achadas = new Midia[qtdMidias];
        int qtd = 0;

        for (int i = 0; i < qtdMidias; i++) {
            String titulo = midias[i].getTitulo().toLowerCase();
            if (titulo.equals(t)) {
                return new Midia[]{midias[i]};
            }
            if (!t.isEmpty() && titulo.contains(t)) {
                achadas[qtd++] = midias[i];
            }
        }

        if (qtd == 0) {
            throw new MidiaNaoEncontradaException(termo.trim());
        }
        return Arrays.copyOf(achadas, qtd);
    }

    public Midia buscarPorId(int id) throws MidiaNaoEncontradaException {
        for (int i = 0; i < qtdMidias; i++) {
            if (midias[i].getId() == id) {
                return midias[i];
            }
        }
        throw new MidiaNaoEncontradaException(id);
    }

    // Próximo ID livre: maior ID existente + 1 (não repete mesmo se algo for removido)
    public int proximoId() {
        int maior = 0;
        for (int i = 0; i < qtdMidias; i++) {
            if (midias[i].getId() > maior) {
                maior = midias[i].getId();
            }
        }
        return maior + 1;
    }

    private boolean idExiste(int id) {
        for (int i = 0; i < qtdMidias; i++) {
            if (midias[i].getId() == id) {
                return true;
            }
        }
        return false;
    }

    // Devolve um array do tamanho exato (sem posições vazias)
    public Midia[] getMidias() {
        return Arrays.copyOf(midias, qtdMidias);
    }

    public int getQtdBase() {
        return qtdBase;
    }

    public Path getCaminhoBase() {
        return caminhoBase;
    }

    public Path getCaminhoUsuario() {
        return caminhoUsuario;
    }

    public String getAvisos() {
        return avisos;
    }

    // Formato dos arquivos (uma linha por item):
    // FILME;id;titulo;ano;duracao
    // SERIE;id;titulo;ano
    // EPISODIO;numero;duracao   (pertence à última SERIE acima)
    //
    // Salva só o que o usuário cadastrou: o catálogo base nunca é reescrito
    public void salvarEmArquivo() throws IOException {
        StringBuilder sb = new StringBuilder();
        String quebra = System.lineSeparator();

        for (int i = qtdBase; i < qtdMidias; i++) {
            Midia m = midias[i];
            if (m instanceof Filme) {
                sb.append("FILME;").append(m.getId()).append(";").append(m.getTitulo()).append(";")
                        .append(m.getAno()).append(";").append(m.calcularDuracao()).append(quebra);
            } else if (m instanceof Serie) {
                Serie s = (Serie) m;
                sb.append("SERIE;").append(s.getId()).append(";").append(s.getTitulo()).append(";")
                        .append(s.getAno()).append(quebra);
                for (Episodio e : s.getEpisodios()) {
                    sb.append("EPISODIO;").append(e.getNumero()).append(";")
                            .append(e.getDuracao()).append(quebra);
                }
            }
        }

        Files.write(caminhoUsuario, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    // Carrega primeiro o catálogo base e depois o que o usuário cadastrou
    public void carregarDeArquivo() throws IOException {
        midias = new Midia[50];
        qtdMidias = 0;
        avisos = "";

        lerArquivo(caminhoBase);
        qtdBase = qtdMidias;

        lerArquivo(caminhoUsuario);
    }

    // Linhas com problema NÃO derrubam o programa: são ignoradas e viram um aviso
    private void lerArquivo(Path caminho) throws IOException {
        // Se o arquivo ainda não existe, não há nada para carregar
        if (!Files.exists(caminho)) {
            return;
        }

        String nomeArquivo = caminho.getFileName().toString();
        String conteudo = new String(Files.readAllBytes(caminho), StandardCharsets.UTF_8);
        // Remove o BOM que alguns editores do Windows colocam no começo do arquivo
        if (conteudo.startsWith("\uFEFF")) {
            conteudo = conteudo.substring(1);
        }

        String[] linhas = conteudo.split("\\r?\\n");
        Serie serieAtual = null;

        for (int n = 0; n < linhas.length; n++) {
            String linha = linhas[n];
            if (linha.trim().isEmpty()) {
                continue;
            }

            String[] p = linha.split(";");
            if (p.length == 0) { // ex.: linha só com ";;;"
                avisar(nomeArquivo, n + 1, "linha sem conteúdo válido");
                continue;
            }

            try {
                String tipo = p[0].trim();

                if (tipo.equals("FILME")) {
                    exigirCampos(p, 5);
                    int id = idLivre(Integer.parseInt(p[1].trim()));
                    inserir(new Filme(id, p[2], Integer.parseInt(p[3].trim()),
                            Integer.parseInt(p[4].trim())));
                } else if (tipo.equals("SERIE")) {
                    serieAtual = null; // se esta linha falhar, os episódios dela serão ignorados
                    exigirCampos(p, 4);
                    int id = idLivre(Integer.parseInt(p[1].trim()));
                    Serie nova = new Serie(id, p[2], Integer.parseInt(p[3].trim()));
                    inserir(nova);
                    serieAtual = nova;
                } else if (tipo.equals("EPISODIO")) {
                    if (serieAtual == null) {
                        throw new DadoInvalidoException("episódio sem uma série válida acima dele");
                    }
                    exigirCampos(p, 3);
                    serieAtual.adicionarEpisodio(new Episodio(
                            Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim())));
                } else {
                    throw new DadoInvalidoException("tipo desconhecido \"" + tipo + "\"");
                }
            } catch (NumberFormatException e) {
                avisar(nomeArquivo, n + 1, "número inválido");
            } catch (DadoInvalidoException e) {
                avisar(nomeArquivo, n + 1, e.getMessage());
            }
        }
    }

    private void exigirCampos(String[] p, int minimo) throws DadoInvalidoException {
        if (p.length < minimo) {
            throw new DadoInvalidoException("linha incompleta (esperado " + minimo + " campos)");
        }
    }

    private void avisar(String arquivo, int linha, String motivo) {
        avisos += "  " + arquivo + ", linha " + linha + " ignorada: " + motivo + System.lineSeparator();
    }

    // Se o ID do arquivo já estiver em uso (ex.: catalogo.txt antigo, de antes do
    // catálogo base existir), gera um novo para não haver dois itens com o mesmo ID
    private int idLivre(int idLido) {
        if (idLido <= 0 || idExiste(idLido)) {
            return proximoId();
        }
        return idLido;
    }
}
