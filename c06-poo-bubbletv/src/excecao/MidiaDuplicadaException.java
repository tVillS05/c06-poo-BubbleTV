package excecao;

// Lançada ao tentar cadastrar uma mídia com um título que já existe
public class MidiaDuplicadaException extends Exception {

    public MidiaDuplicadaException(String titulo) {
        super("Já existe uma mídia cadastrada com o título: " + titulo);
    }
}
