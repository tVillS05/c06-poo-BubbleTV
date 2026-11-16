package excecao;

public class MidiaNaoEncontradaException extends Exception {

    // Busca por título
    public MidiaNaoEncontradaException(String titulo) {
        super("Mídia não encontrada: " + titulo);
    }

    // Busca por ID
    public MidiaNaoEncontradaException(int id) {
        super("Nenhuma mídia encontrada com o ID " + id);
    }
}
