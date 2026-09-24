using KFKA.ListagemAlunos.Models;

namespace KFKA.ListagemAlunos.Services;

public class PaginacaoService
{
    public int CalcularTotalPaginas(List<Aluno> alunos, int tamanhoPagina)
    {
        if (tamanhoPagina <= 0) throw new ArgumentOutOfRangeException(nameof(tamanhoPagina));
        return (int)Math.Ceiling((double)alunos.Count / tamanhoPagina);
    }

    public List<Aluno> ObterPagina(List<Aluno> alunos, int paginaAtual, int tamanhoPagina)
    {
        int totalPaginas = CalcularTotalPaginas(alunos, tamanhoPagina);
        if (paginaAtual < 1 || paginaAtual > totalPaginas)
            throw new ArgumentOutOfRangeException(nameof(paginaAtual), "Página inexistente.");

        return alunos.Skip((paginaAtual - 1) * tamanhoPagina)
                     .Take(tamanhoPagina)
                     .ToList();
    }
}
