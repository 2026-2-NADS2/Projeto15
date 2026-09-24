using Microsoft.Data.Sqlite;

namespace KFKA.ListagemAlunos.Data;

public class Database
{
    private readonly string _connectionString;

    public Database()
    {
        string caminhoBanco = Path.Combine(AppContext.BaseDirectory, "kfka.db");
        _connectionString = new SqliteConnectionStringBuilder { DataSource = caminhoBanco }.ToString();
    }

    public SqliteConnection CriarConexao() => new(_connectionString);

    public void Inicializar()
    {
        using var conexao = CriarConexao();
        conexao.Open();

        using var criarTabela = conexao.CreateCommand();
        criarTabela.CommandText = """
            CREATE TABLE IF NOT EXISTS Alunos (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                Nome TEXT NOT NULL,
                Matricula TEXT NOT NULL,
                Turma TEXT NOT NULL
            );
            """;
        criarTabela.ExecuteNonQuery();

        using var contar = conexao.CreateCommand();
        contar.CommandText = "SELECT COUNT(*) FROM Alunos";
        if (Convert.ToInt64(contar.ExecuteScalar()) != 0)
            return;

        (string Nome, string Turma)[] alunos =
        [
            ("Ana Souza", "6º A"), ("Bruno Santos", "6º A"),
            ("Carla Oliveira", "6º B"), ("Daniel Lima", "7º A"),
            ("Eduarda Ferreira", "7º B"), ("Felipe Costa", "7º B"),
            ("Gabriela Martins", "8º A"), ("Henrique Almeida", "8º A"),
            ("Isabela Rocha", "8º B"), ("João Pereira", "9º A"),
            ("Larissa Ribeiro", "6º A"), ("Marcos Carvalho", "6º B"),
            ("Natália Gomes", "7º A"), ("Otávio Barbosa", "7º B"),
            ("Paula Nascimento", "8º A"), ("Rafael Teixeira", "8º B"),
            ("Sofia Fernandes", "9º A"), ("Thiago Moreira", "9º B"),
            ("Vitória Correia", "9º B"), ("William Araújo", "6º B")
        ];

        using var transacao = conexao.BeginTransaction();
        using var inserir = conexao.CreateCommand();
        inserir.Transaction = transacao;
        inserir.CommandText = "INSERT INTO Alunos (Nome, Matricula, Turma) VALUES ($nome, $matricula, $turma)";
        var nome = inserir.Parameters.Add("$nome", SqliteType.Text);
        var matricula = inserir.Parameters.Add("$matricula", SqliteType.Text);
        var turma = inserir.Parameters.Add("$turma", SqliteType.Text);

        for (int i = 0; i < alunos.Length; i++)
        {
            nome.Value = alunos[i].Nome;
            matricula.Value = $"2026{i + 1:000}";
            turma.Value = alunos[i].Turma;
            inserir.ExecuteNonQuery();
        }
        transacao.Commit();
    }
}
