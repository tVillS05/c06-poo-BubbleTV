package modelo;

import excecao.DadoInvalidoException;

import java.time.Year;

public abstract class Midia implements Reproduzivel {

    public static final int ANO_MINIMO = 1888; // primeiro filme da história
    public static final int TITULO_MAXIMO = 100;

    protected int id;
    protected String titulo;
    protected int ano;

    public Midia(int id, String titulo, int ano) throws DadoInvalidoException {
        if (id <= 0) {
            throw new DadoInvalidoException("O ID deve ser maior que zero.");
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new DadoInvalidoException("O título não pode ser vazio.");
        }
        if (titulo.trim().length() > TITULO_MAXIMO) {
            throw new DadoInvalidoException("O título pode ter no máximo " + TITULO_MAXIMO + " caracteres.");
        }
        if (ano < ANO_MINIMO || ano > anoMaximo()) {
            throw new DadoInvalidoException("Ano inválido: use um valor entre " + ANO_MINIMO + " e " + anoMaximo() + ".");
        }
        this.id = id;
        this.titulo = titulo.trim();
        this.ano = ano;
    }

    // Permite lançamentos dos próximos anos
    public static int anoMaximo() {
        return Year.now().getValue() + 5;
    }

    // Abstratos: cada tipo de mídia faz do seu jeito (polimorfismo)
    public abstract int calcularDuracao();

    public abstract String getTipo();

    // Linha de descrição usada nas listagens
    public String getResumo() {
        return id + " - " + titulo + " (" + ano + ") [" + getTipo() + "] - " + calcularDuracao() + " min";
    }

    @Override
    public void reproduzir() {
        System.out.println("Reproduzindo: " + titulo);
    }

    @Override
    public void pausar() {
        System.out.println("Pausado: " + titulo);
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getAno() {
        return ano;
    }
}
