package modelo;

import excecao.DadoInvalidoException;

public class Filme extends Midia {

    public static final int DURACAO_MAXIMA = 1000;

    private int duracao;

    public Filme(int id, String titulo, int ano, int duracao) throws DadoInvalidoException {
        super(id, titulo, ano);
        if (duracao <= 0 || duracao > DURACAO_MAXIMA) {
            throw new DadoInvalidoException("Duração inválida: use um valor entre 1 e " + DURACAO_MAXIMA + " minutos.");
        }
        this.duracao = duracao;
    }

    @Override
    public int calcularDuracao() {
        return duracao;
    }

    @Override
    public String getTipo() {
        return "Filme";
    }

    @Override
    public void reproduzir() {
        super.reproduzir();
        System.out.println("Fim do filme \"" + titulo + "\". Esperamos que você tenha gostado!");
    }
}
