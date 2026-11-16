package excecao;

// Lançada quando um dado não respeita as regras (título vazio, ano absurdo, duração negativa...)
public class DadoInvalidoException extends Exception {

    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
