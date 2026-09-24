# KFKA - Simulação de Paginação de Alunos

## Objetivo

Entrega 1 de Estrutura de Dados do Projeto Interdisciplinar da FECAP. Demonstra o uso de `List<Aluno>` populada com registros de um banco relacional SQLite e utilizada para paginar uma listagem no console. Esta aplicação representa apenas a atividade de listagem de alunos, não o sistema KFKA completo.

## Tecnologias

- C# e .NET 8
- SQLite e Microsoft.Data.Sqlite
- `List<T>` e LINQ (`Skip` e `Take`)

## Funcionamento

`SQLite → SELECT → List<Aluno> → paginação em memória → Console`

Na primeira execução, o programa cria `kfka.db` no diretório de saída da aplicação (`bin/Debug/net8.0/` ao usar `dotnet run`), cria a tabela `Alunos` e insere 20 alunos fictícios. Nas execuções seguintes, não repete a inserção se a tabela contiver registros. O repositório lê cada linha do banco e adiciona um objeto à `List<Aluno>`. O serviço usa a lista para mostrar cinco alunos por página. A consulta SQL não usa `LIMIT` nem `OFFSET`.

## Como executar

Instale o SDK .NET 8. No terminal, entre na pasta `KFKA.ListagemAlunos` e execute:

```bash
dotnet restore
dotnet build
dotnet run
```

Use `N` para avançar, `A` para voltar, `P` para escolher uma página e `S` para sair. Entradas inválidas permitem tentar novamente. Para reiniciar apenas os dados de exemplo, remova o arquivo `kfka.db` gerado no diretório de saída antes de executar novamente.

## Estrutura do projeto

- `Models/Aluno.cs`: representa os dados de cada aluno.
- `Data/Database.cs`: cria a conexão, tabela e dados iniciais.
- `Data/AlunoRepository.cs`: lê as linhas SQLite e monta a `List<Aluno>`.
- `Services/PaginacaoService.cs`: calcula o total de páginas e extrai uma página da lista.
- `Program.cs`: inicia a aplicação e controla a navegação no console.

O arquivo `kfka.db` é criado automaticamente na primeira execução, por isso não acompanha os arquivos fonte.
