package modelo;

import excecao.DadoInvalidoException;

import java.util.Arrays;

public class Serie extends Midia {

    public static final int MAX_EPISODIOS = 200;

    // Composição: os episódios pertencem à série
    // Array com contador: só as primeiras "qtdEpisodios" posições estão preenchidas
    private Episodio[] episodios = new Episodio[10];
    private int qtdEpisodios = 0;

    public Serie(int id, String titulo, int ano) throws DadoInvalidoException {
        super(id, titulo, ano);
    }

    // A duração da série é a soma da duração dos episódios
    @Override
    public int calcularDuracao() {
        int total = 0;
        for (int i = 0; i < qtdEpisodios; i++) {
            total += episodios[i].getDuracao();
        }
        return total;
    }

    @Override
    public String getTipo() {
        return "Série";
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " - " + qtdEpisodios + " episódios";
    }

    public void adicionarEpisodio(Episodio e) throws DadoInvalidoException {
        if (e == null) {
            throw new DadoInvalidoException("Episódio inválido.");
        }
        if (qtdEpisodios >= MAX_EPISODIOS) {
            throw new DadoInvalidoException("Uma série pode ter no máximo " + MAX_EPISODIOS + " episódios.");
        }
        // Array cheio: cria um com o dobro do tamanho e copia o conteúdo
        if (qtdEpisodios == episodios.length) {
            episodios = Arrays.copyOf(episodios, episodios.length * 2);
        }
        episodios[qtdEpisodios] = e;
        qtdEpisodios++;
    }

    // Reproduz um episódio (numerado de 1 até qtdEpisodios) e avisa quando ele termina
    public void reproduzirEpisodio(int numero) throws DadoInvalidoException {
        if (qtdEpisodios == 0) {
            throw new DadoInvalidoException("A série \"" + titulo + "\" não tem episódios.");
        }
        if (numero < 1 || numero > qtdEpisodios) {
            throw new DadoInvalidoException("O episódio " + numero + " não existe. A série tem "
                    + qtdEpisodios + " episódios.");
        }
        Episodio ep = episodios[numero - 1];
        System.out.println("Reproduzindo: " + titulo + " - Episódio " + ep.getNumero()
                + " (" + ep.getDuracao() + " min)");
        System.out.println("Fim do episódio " + ep.getNumero() + ".");
    }

    // Reproduzir "a série" = começar pelo primeiro episódio
    @Override
    public void reproduzir() {
        try {
            reproduzirEpisodio(1);
        } catch (DadoInvalidoException e) {
            System.out.println(e.getMessage());
        }
    }

    public int getQtdEpisodios() {
        return qtdEpisodios;
    }

    // Devolve um array do tamanho exato (sem posições vazias)
    public Episodio[] getEpisodios() {
        return Arrays.copyOf(episodios, qtdEpisodios);
    }
}
