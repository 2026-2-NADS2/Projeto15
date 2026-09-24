using KFKA.ListagemAlunos.Models;

namespace KFKA.ListagemAlunos.Data;

public class AlunoRepository
{
    private readonly Database _database;

    public AlunoRepository(Database database) => _database = database;

    public List<Aluno> ListarTodos()
    {
        List<Aluno> alunos = new();
        using var conexao = _database.CriarConexao();
        conexao.Open();
        using var comando = conexao.CreateCommand();
        comando.CommandText = "SELECT Id, Nome, Matricula, Turma FROM Alunos ORDER BY Id";
        using var leitor = comando.ExecuteReader();

        while (leitor.Read())
        {
            Aluno aluno = new()
            {
                Id = leitor.GetInt32(0),
                Nome = leitor.GetString(1),
                Matricula = leitor.GetString(2),
                Turma = leitor.GetString(3)
            };
            alunos.Add(aluno);
        }
        return alunos;
    }
}
