import excecao.DadoInvalidoException;
import excecao.MidiaDuplicadaException;
import excecao.MidiaNaoEncontradaException;
import modelo.Episodio;
import modelo.Filme;
import modelo.Midia;
import modelo.Serie;
import modelo.Usuario;
import servico.Catalogo;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Catalogo catalogo = new Catalogo();

        System.out.println("=================================");
        System.out.println("     Bem-vindo ao BubbleTV!");
        System.out.println("=================================");

        try {
            Usuario usuario = criarUsuario(sc);
            System.out.println();
            System.out.println("Olá, " + usuario.getNome() + "! Que bom ter você aqui.");

            carregarCatalogo(catalogo);

            int opcao;
            do {
                exibirMenu(usuario);
                opcao = lerInt(sc, "Escolha: ");

                switch (opcao) {
                    case 1:
                        cadastrarFilme(sc, catalogo);
                        break;
                    case 2:
                        cadastrarSerie(sc, catalogo);
                        break;
                    case 3:
                        listarCatalogo(catalogo);
                        break;
                    case 4:
                        buscarPorTitulo(sc, catalogo);
                        break;
                    case 5:
                        assistir(sc, catalogo, usuario);
                        break;
                    case 6:
                        Midia paraFavoritar = escolherMidia(sc, catalogo);
                        if (paraFavoritar != null) {
                            usuario.favoritar(paraFavoritar);
                        }
                        break;
                    case 7:
                        listarFavoritos(usuario);
                        break;
                    case 0:
                        salvar(catalogo);
                        System.out.println("Até a próxima, " + usuario.getNome() + "!");
                        break;
                    default:
                        System.out.println("Opção inválida. Escolha um número de 0 a 7.");
                }
            } while (opcao != 0);
        } catch (NoSuchElementException e) {
            // Entrada encerrada (Ctrl+D / Ctrl+Z): sai sem estourar erro
            System.out.println();
            System.out.println("Entrada encerrada. Salvando e saindo...");
            salvar(catalogo);
        } finally {
            sc.close();
        }
    }

    // Repete até nome e email serem válidos
    private static Usuario criarUsuario(Scanner sc) {
        while (true) {
            String nome = lerTexto(sc, "Seu nome: ");
            String email = lerTexto(sc, "Seu email: ");
            try {
                return new Usuario(nome, email);
            } catch (DadoInvalidoException e) {
                System.out.println("Erro: " + e.getMessage() + " Tente de novo.");
            }
        }
    }

    private static void carregarCatalogo(Catalogo catalogo) {
        try {
            catalogo.carregarDeArquivo();
        } catch (IOException e) {
            System.out.println("Não foi possível ler os arquivos do catálogo: " + e.getMessage());
        }

        if (catalogo.getQtdBase() == 0) {
            System.out.println("Aviso: catalogo-base.txt não encontrado (ou vazio).");
            System.out.println("Procurei em: " + catalogo.getCaminhoBase());
        } else {
            System.out.println("Catálogo carregado: " + catalogo.getMidias().length + " títulos.");
        }

        if (!catalogo.getAvisos().isEmpty()) {
            System.out.println("Atenção: algumas linhas dos arquivos foram ignoradas:");
            System.out.print(catalogo.getAvisos());
        }
    }

    private static void exibirMenu(Usuario usuario) {
        System.out.println();
        System.out.println("===== BubbleTV | Olá, " + usuario.getNome() + " =====");
        System.out.println("1 - Cadastrar filme");
        System.out.println("2 - Cadastrar série");
        System.out.println("3 - Listar catálogo");
        System.out.println("4 - Buscar por título");
        System.out.println("5 - Assistir");
        System.out.println("6 - Favoritar");
        System.out.println("7 - Ver favoritos");
        System.out.println("0 - Sair (salva automaticamente)");
    }

    // ---------- Cadastros ----------

    private static void cadastrarFilme(Scanner sc, Catalogo catalogo) {
        String titulo = lerTitulo(sc, "Título: ");
        if (tituloJaExiste(catalogo, titulo)) {
            return;
        }
        int ano = lerInt(sc, "Ano: ", Midia.ANO_MINIMO, Midia.anoMaximo());
        int duracao = lerInt(sc, "Duração (min): ", 1, Filme.DURACAO_MAXIMA);

        try {
            int id = catalogo.proximoId();
            catalogo.adicionar(new Filme(id, titulo, ano, duracao));
            System.out.println("Filme cadastrado com o ID " + id + "!");
            salvar(catalogo);
        } catch (DadoInvalidoException | MidiaDuplicadaException e) {
            System.out.println("Erro ao cadastrar: " + e.getMessage());
        }
    }

    private static void cadastrarSerie(Scanner sc, Catalogo catalogo) {
        String titulo = lerTitulo(sc, "Título da série: ");
        if (tituloJaExiste(catalogo, titulo)) {
            return;
        }
        int ano = lerInt(sc, "Ano: ", Midia.ANO_MINIMO, Midia.anoMaximo());
        int qtd = lerInt(sc, "Quantidade de episódios: ", 1, Serie.MAX_EPISODIOS);
        int duracaoEp = lerInt(sc, "Duração de cada episódio (min): ", 1, Episodio.DURACAO_MAXIMA);

        try {
            int id = catalogo.proximoId();
            Serie serie = new Serie(id, titulo, ano);
            // Episódios sem nome: ep 1, ep 2, ...
            for (int i = 1; i <= qtd; i++) {
                serie.adicionarEpisodio(new Episodio(i, duracaoEp));
            }
            catalogo.adicionar(serie);
            System.out.println("Série cadastrada com o ID " + id + " (" + qtd + " episódios)!");
            salvar(catalogo);
        } catch (DadoInvalidoException | MidiaDuplicadaException e) {
            System.out.println("Erro ao cadastrar: " + e.getMessage());
        }
    }

    // Avisa o usuário logo no começo, antes de pedir o resto dos dados
    private static boolean tituloJaExiste(Catalogo catalogo, String titulo) {
        try {
            catalogo.verificarDuplicado(titulo);
            return false;
        } catch (MidiaDuplicadaException e) {
            System.out.println(e.getMessage());
            return true;
        }
    }

    // ---------- Listagens e busca ----------

    private static void listarCatalogo(Catalogo catalogo) {
        Midia[] midias = catalogo.getMidias();
        if (midias.length == 0) {
            System.out.println("O catálogo está vazio.");
            return;
        }
        for (Midia m : midias) {
            // Polimorfismo: cada mídia monta o resumo do seu jeito
            System.out.println(m.getResumo());
        }
    }

    private static void listarFavoritos(Usuario usuario) {
        Midia[] favoritos = usuario.getFavoritos();
        if (favoritos.length == 0) {
            System.out.println(usuario.getNome() + ", você ainda não tem favoritos.");
            return;
        }
        System.out.println("Favoritos de " + usuario.getNome() + ":");
        for (Midia m : favoritos) {
            System.out.println(m.getResumo());
        }
    }

    private static void buscarPorTitulo(Scanner sc, Catalogo catalogo) {
        String termo = lerTextoObrigatorio(sc, "Título (ou parte dele): ");
        try {
            for (Midia m : catalogo.buscar(termo)) {
                System.out.println(m.getResumo());
            }
        } catch (MidiaNaoEncontradaException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------- Assistir ----------

    private static void assistir(Scanner sc, Catalogo catalogo, Usuario usuario) {
        Midia escolhida = escolherMidia(sc, catalogo);
        if (escolhida == null) {
            return;
        }

        if (escolhida instanceof Serie) {
            assistirSerie(sc, usuario, (Serie) escolhida);
        } else {
            usuario.assistir(escolhida); // o filme mostra a mensagem de fim sozinho
        }
    }

    private static void assistirSerie(Scanner sc, Usuario usuario, Serie serie) {
        int total = serie.getQtdEpisodios();
        if (total == 0) {
            System.out.println("A série \"" + serie.getTitulo() + "\" não tem episódios.");
            return;
        }

        int ep = lerInt(sc, "Começar em qual episódio? (1 a " + total + "): ", 1, total);
        System.out.println(usuario.getNome() + " está assistindo:");

        while (true) {
            try {
                serie.reproduzirEpisodio(ep);
            } catch (DadoInvalidoException e) {
                System.out.println(e.getMessage());
                return;
            }

            if (ep == total) {
                System.out.println("Você terminou \"" + serie.getTitulo() + "\"! Fim da série.");
                return;
            }

            int opcao = lerInt(sc, "1 - Ir para o episódio " + (ep + 1) + " | 0 - Sair: ", 0, 1);
            if (opcao == 0) {
                System.out.println("Você parou no episódio " + ep + " de \"" + serie.getTitulo() + "\".");
                return;
            }
            ep++;
        }
    }

    // Mostra o catálogo, pede o ID OU parte do título e devolve a mídia.
    // Devolve null se cancelar (0), se não achar ou se houver mais de um resultado.
    private static Midia escolherMidia(Scanner sc, Catalogo catalogo) {
        if (catalogo.getMidias().length == 0) {
            System.out.println("O catálogo está vazio.");
            return null;
        }

        listarCatalogo(catalogo);
        String entrada = lerTextoObrigatorio(sc, "Digite o ID ou o título (0 para cancelar): ");
        Integer id = comoInteiro(entrada);

        try {
            if (id != null) {
                if (id == 0) {
                    return null;
                }
                return catalogo.buscarPorId(id);
            }

            Midia[] achadas = catalogo.buscar(entrada);
            if (achadas.length == 1) {
                return achadas[0];
            }
            System.out.println("Mais de um resultado. Escolha pelo ID:");
            for (Midia m : achadas) {
                System.out.println(m.getResumo());
            }
            return null;
        } catch (MidiaNaoEncontradaException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // ---------- Arquivo ----------

    private static void salvar(Catalogo catalogo) {
        try {
            catalogo.salvarEmArquivo();
        } catch (IOException e) {
            System.out.println("Erro ao salvar: " + e.getMessage());
        }
    }

    // ---------- Leitura segura do teclado ----------

    private static Integer comoInteiro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Repete até o usuário digitar um número inteiro válido
    private static int lerInt(Scanner sc, String mensagem) {
        return lerInt(sc, mensagem, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    // Repete até o usuário digitar um inteiro dentro de [min, max]
    private static int lerInt(Scanner sc, String mensagem, int min, int max) {
        while (true) {
            System.out.print(mensagem);
            Integer valor = comoInteiro(sc.nextLine());
            if (valor == null) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            } else if (valor < min || valor > max) {
                System.out.println("Valor fora do intervalo. Digite um número de " + min + " a " + max + ".");
            } else {
                return valor;
            }
        }
    }

    // O ";" é trocado por "," porque ele separa os campos no arquivo
    private static String lerTexto(Scanner sc, String mensagem) {
        System.out.print(mensagem);
        return sc.nextLine().trim().replace(";", ",");
    }

    // Repete até digitar alguma coisa
    private static String lerTextoObrigatorio(Scanner sc, String mensagem) {
        while (true) {
            String texto = lerTexto(sc, mensagem);
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este campo não pode ficar vazio.");
        }
    }

    // Título: obrigatório e com tamanho máximo
    private static String lerTitulo(Scanner sc, String mensagem) {
        while (true) {
            String titulo = lerTextoObrigatorio(sc, mensagem);
            if (titulo.length() <= Midia.TITULO_MAXIMO) {
                return titulo;
            }
            System.out.println("Título muito longo (máximo " + Midia.TITULO_MAXIMO + " caracteres).");
        }
    }
}
