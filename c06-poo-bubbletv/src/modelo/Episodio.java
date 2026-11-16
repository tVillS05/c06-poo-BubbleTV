package modelo;

import excecao.DadoInvalidoException;

public class Episodio {

    public static final int DURACAO_MAXIMA = 600;

    private int numero;
    private int duracao;

    public Episodio(int numero, int duracao) throws DadoInvalidoException {
        if (numero <= 0) {
            throw new DadoInvalidoException("O número do episódio deve ser maior que zero.");
        }
        if (duracao <= 0 || duracao > DURACAO_MAXIMA) {
            throw new DadoInvalidoException("Duração do episódio inválida: use um valor entre 1 e "
                    + DURACAO_MAXIMA + " minutos.");
        }
        this.numero = numero;
        this.duracao = duracao;
    }

    public int getNumero() {
        return numero;
    }

    public int getDuracao() {
        return duracao;
    }
}
