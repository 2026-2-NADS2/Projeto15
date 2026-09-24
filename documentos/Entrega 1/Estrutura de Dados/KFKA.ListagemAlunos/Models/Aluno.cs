namespace KFKA.ListagemAlunos.Models;

public class Aluno
{
    public int Id { get; set; }
    public string Nome { get; set; } = string.Empty;
    public string Matricula { get; set; } = string.Empty;
    public string Turma { get; set; } = string.Empty;
}
