package modelo;

import excecao.DadoInvalidoException;

import java.util.Arrays;

public class Usuario {

    private String nome;
    private String email;
    private Midia[] favoritos = new Midia[10];
    private int qtdFavoritos = 0;

    public Usuario(String nome, String email) throws DadoInvalidoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DadoInvalidoException("O nome não pode ser vazio.");
        }
        if (nome.trim().length() > 50) {
            throw new DadoInvalidoException("O nome pode ter no máximo 50 caracteres.");
        }
        if (email == null || !email.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new DadoInvalidoException("Email inválido. Exemplo: nome@dominio.com");
        }
        this.nome = nome.trim();
        this.email = email.trim();
    }

    public void assistir(Midia m) {
        System.out.println(nome + " está assistindo:");
        m.reproduzir();
    }

    public void favoritar(Midia m) {
        if (jaEhFavorito(m)) {
            System.out.println(m.getTitulo() + " já está nos favoritos.");
            return;
        }

        // Array cheio: cria um com o dobro do tamanho e copia o conteúdo
        if (qtdFavoritos == favoritos.length) {
            favoritos = Arrays.copyOf(favoritos, favoritos.length * 2);
        }
        favoritos[qtdFavoritos] = m;
        qtdFavoritos++;
        System.out.println(m.getTitulo() + " adicionado aos favoritos!");
    }

    // Compara pelo ID, que é único no catálogo
    private boolean jaEhFavorito(Midia m) {
        for (int i = 0; i < qtdFavoritos; i++) {
            if (favoritos[i].getId() == m.getId()) {
                return true;
            }
        }
        return false;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    // Devolve um array do tamanho exato (sem posições vazias)
    public Midia[] getFavoritos() {
        return Arrays.copyOf(favoritos, qtdFavoritos);
    }
}
