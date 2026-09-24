using KFKA.ListagemAlunos.Data;
using KFKA.ListagemAlunos.Models;
using KFKA.ListagemAlunos.Services;
using Microsoft.Data.Sqlite;

try
{
    Database banco = new();
    banco.Inicializar();

    // Os registros vêm do SQLite e passam a ser uma coleção em memória.
    List<Aluno> alunos = new AlunoRepository(banco).ListarTodos();
    if (alunos.Count == 0)
    {
        Console.WriteLine("Nenhum aluno encontrado no banco de dados.");
        return;
    }

    PaginacaoService paginacao = new();
    const int tamanhoPagina = 5;
    int totalPaginas = paginacao.CalcularTotalPaginas(alunos, tamanhoPagina);
    int paginaAtual = 1;
    string mensagem = "";

    while (true)
    {
        Console.WriteLine("========================================");
        Console.WriteLine(" KFKA - LISTAGEM DE ALUNOS");
        Console.WriteLine("========================================");
        Console.WriteLine($"Página {paginaAtual} de {totalPaginas}");
        Console.WriteLine($"Total de alunos: {alunos.Count}\n");
        Console.WriteLine($"{"ID",-4} {"MATRÍCULA",-11} {"NOME",-23} TURMA");
        Console.WriteLine(new string('-', 52));

        foreach (Aluno aluno in paginacao.ObterPagina(alunos, paginaAtual, tamanhoPagina))
            Console.WriteLine($"{aluno.Id,-4} {aluno.Matricula,-11} {aluno.Nome,-23} {aluno.Turma}");

        Console.WriteLine(new string('-', 52));
        if (mensagem.Length > 0) Console.WriteLine(mensagem);
        mensagem = "";
        Console.WriteLine("\n[N] Próxima página\n[A] Página anterior\n[P] Escolher página\n[S] Sair");
        Console.Write("\nEscolha uma opção: ");
        string? opcao = Console.ReadLine()?.Trim().ToUpperInvariant();

        switch (opcao)
        {
            case "N":
                if (paginaAtual < totalPaginas) paginaAtual++;
                else mensagem = "Você já está na última página.";
                break;
            case "A":
                if (paginaAtual > 1) paginaAtual--;
                else mensagem = "Você já está na primeira página.";
                break;
            case "P":
                while (true)
                {
                    Console.Write($"Digite uma página de 1 a {totalPaginas}: ");
                    string? entrada = Console.ReadLine();
                    if (entrada is null) return;
                    if (int.TryParse(entrada, out int pagina) && pagina >= 1 && pagina <= totalPaginas)
                    {
                        paginaAtual = pagina;
                        break;
                    }
                    Console.WriteLine("Página inexistente. Tente novamente.");
                }
                break;
            case "S":
                Console.WriteLine("Programa encerrado.");
                return;
            case null:
                return; // Entrada encerrada (por exemplo, stdin fechado).
            default:
                mensagem = "Opção inválida. Escolha N, A, P ou S.";
                break;
        }
        Console.WriteLine();
    }
}
catch (SqliteException erro)
{
    Console.Error.WriteLine($"Erro ao acessar ou carregar o banco SQLite: {erro.Message}");
}
catch (Exception erro)
{
    Console.Error.WriteLine($"Erro ao iniciar ou carregar alunos: {erro.Message}");
}
