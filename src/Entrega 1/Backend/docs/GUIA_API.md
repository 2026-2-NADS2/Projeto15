# Roteiro da API KFKA

Use a URL local `http://localhost:8080`, `Content-Type: application/json` para corpos
JSON e `Authorization: Bearer TOKEN` nas rotas protegidas. Guarde os IDs e tokens
retornados; os IDs abaixo são exemplos e precisam ser substituídos pelos reais.
As senhas de exemplo são marcadores: escolha senhas próprias fora do Git.

## 1. Administrador faz login

O administrador inicial deve ter sido criado conforme o README.
`POST /usuarios/logar`:

```json
{"email":"admin@example.org","senha":"SUA_SENHA_DE_ADMIN"}
```

Resposta 200:

```json
{"id":1,"nome":"Administrador inicial","email":"admin@example.org","perfil":"ADMINISTRADOR","token":"JWT_RETORNADO"}
```

As próximas operações de cadastro usam o token do administrador.

## 2. Cadastrar professor, responsável e aluno

Primeiro `POST /usuarios`, uma requisição para cada perfil:

```json
{"nome":"Professora Ana","email":"professor@example.org","senha":"SUA_SENHA_DE_PROFESSOR","perfil":"PROFESSOR","ativo":true}
```

```json
{"nome":"Responsável Maria","email":"responsavel@example.org","senha":"SUA_SENHA_DE_RESPONSAVEL","perfil":"RESPONSAVEL","ativo":true}
```

`POST /professores` com o ID do usuário PROFESSOR:

```json
{"usuarioId":2}
```

`POST /alunos`:

```json
{"nome":"João da Silva","ativo":true}
```

`POST /responsaveis` com o ID do usuário RESPONSAVEL e o ID do aluno:

```json
{"usuarioId":3,"alunosIds":[1]}
```

Um responsável pode receber vários IDs de alunos; outro responsável pode ser
cadastrado com o mesmo aluno. Para alterar essa associação, `PUT /responsaveis/{id}`
recebe o mesmo formato, com a lista completa de alunos. Usuário/perfil são
validados e não podem ser trocados em um cadastro de professor/responsável existente.

## 3. Cadastrar área e disciplina

`POST /areas`:

```json
{"nome":"Linguagens","ativo":true}
```

`POST /disciplinas`:

```json
{"nome":"Português","areaDisciplinaId":1,"ativo":true}
```

## 4. Criar turma

`POST /turmas`:

```json
{"nome":"5º A","serie":"5º ano","anoLetivo":2026,"ativo":true}
```

## 5. Matricular aluno

`POST /matriculas`:

```json
{"alunoId":1,"turmaId":1,"ativo":true}
```

A combinação aluno/turma é única. PUT permite alterar apenas `ativo`, preservando
os mesmos IDs; DELETE inativa. Reative a matrícula existente em vez de duplicá-la.

## 6. Vincular professor, disciplina e turma

`POST /vinculos`, usando o ID do cadastro Professor, e não o ID de Usuario:

```json
{"professorId":1,"disciplinaId":1,"turmaId":1,"ativo":true}
```

A combinação é única. PUT permite alterar apenas `ativo` com os mesmos IDs.
DELETE inativa o vínculo. Não há exclusão em cascata.

## 7. Configurar bimestre e tags

`POST /bimestres`, ajustando a janela para a data de utilização:

```json
{"numero":3,"anoLetivo":2026,"dataHoraAbertura":"2026-09-01T00:00:00","dataHoraEncerramento":"2026-10-31T23:59:59"}
```

As datas são locais no `FUSO_HORARIO`, padrão America/Sao_Paulo. A abertura é
inclusiva; o encerramento é exclusivo. O ano precisa ser o mesmo da turma.
PUT permite ajustar datas, preservando ano e número.

`POST /tags`:

```json
{"nome":"Participação","descricao":"Participação nas atividades propostas","ativo":true}
```

## 8. Professor faz login

`POST /usuarios/logar` com e-mail e senha do professor. Guarde seu token.
As próximas operações do professor usam esse token.

## 9. Consultar suas turmas e alunos

`GET /turmas`, `GET /disciplinas`, `GET /alunos`, `GET /vinculos`, `GET /matriculas`.
Todas as consultas do professor têm escopo no backend. IDs de outra turma,
disciplina ou aluno não podem ser consultados diretamente.
`GET /professores` retorna somente seu cadastro, quando acessado pelo professor.

## 10–11. Criar e salvar rascunho

`POST /acompanhamentos`:

```json
{"alunoId":1,"turmaId":1,"disciplinaId":1,"bimestreId":1,"descricao":"Evolução em leitura e resolução de problemas.","media":8.5,"tagsIds":[1]}
```

Retorna 201, com status `RASCUNHO`. Não envie `professorId` nem `status`.
O professor vem do token e o status é definido pelo serviço.
`tagsIds` pode ser uma lista vazia; os IDs informados devem existir e estar ativos.

Para salvar alterações, `PUT /acompanhamentos/{id}`:

```json
{"descricao":"Bom progresso em Português.","media":9.0,"tagsIds":[1]}
```

Somente o proprietário pode editar RASCUNHO ou DEVOLVIDO_AJUSTES, dentro do prazo.
Os relacionamentos acadêmicos não podem ser alterados por esse endpoint.

## 12. Enviar para revisão

`PATCH /acompanhamentos/{id}/enviar`, sem corpo, com token do professor.
Retorna 200 e status `ENVIADO_REVISAO`. O prazo e o vínculo são conferidos novamente.

## 13–14. Administrador recebe e revisa

Com token do administrador, consulte:
`GET /relatorios?status=ENVIADO_REVISAO`.

`PATCH /acompanhamentos/{id}/iniciar-revisao` retorna `EM_REVISAO`.
Se precisar alterar descrição/média/tags, `PUT /acompanhamentos/{id}/revisao`
recebe o formato de edição acima. O histórico registra antes/depois e o usuário.

Para devolver, `PATCH /acompanhamentos/{id}/devolver` retorna DEVOLVIDO_AJUSTES.
O professor ajusta e reenvia; o administrador inicia uma nova revisão.

## 15. Publicar

`PATCH /acompanhamentos/{id}/publicar`, com token do administrador.
Requer EM_REVISAO e retorna PUBLICADO. Rascunho não pode ser publicado diretamente.
PUBLICADO é terminal e não pode ser editado/cancelado por estes endpoints.

Para cancelar antes da publicação, use `PATCH /acompanhamentos/{id}/cancelar`.
CANCELADO é terminal e não aparece para responsáveis.

`GET /acompanhamentos/{id}/historico` é permitido ao administrador e ao professor
proprietário. O histórico não possui endpoints de edição/exclusão.

## 16–18. Responsável faz login e consulta

`POST /usuarios/logar` com suas credenciais, depois:

- `GET /alunos`: apenas os alunos vinculados ao responsável.
- `GET /responsaveis`: somente seu cadastro.
- `GET /alunos/{id}/acompanhamentos`: apenas publicados daquele aluno.
- `GET /acompanhamentos/{id}`: publicado, de aluno vinculado.
- `GET /relatorios?anoLetivo=2026&bimestre=3&alunoId=1&disciplinaId=1`.

Qualquer filtro é combinado com o escopo de propriedade, inclusive se um usuário
informar IDs de outra família. Estados não publicados nunca são expostos.

## 19. Registrar ciência

`POST /acompanhamentos/{id}/ciencia`, com token do responsável:

```json
{"observacao":"Li o acompanhamento."}
```

O corpo é opcional. Retorna 201 com ID, responsável, acompanhamento e data/hora.
Uma segunda ciência do mesmo responsável retorna 409. Responsáveis diferentes
podem registrar suas próprias ciências sobre o mesmo acompanhamento.

## 20. Baixar PDF

`GET /acompanhamentos/{id}/pdf`, com token do responsável vinculado ou administrador.
Retorna `application/pdf` e `Content-Disposition: attachment`. Só PUBLICADO gera
PDF. O documento inclui aluno, turma, série, ano, bimestre, disciplina, professor,
descrição, média e tags, com fonte Noto Sans incorporada e múltiplas páginas.
Caracteres sem glifo na fonte são representados por `?`.

## 21. Relatório administrativo

`GET /relatorios?turmaId=1&disciplinaId=1&professorId=1&bimestreId=1&alunoId=1&tagId=1`.
Todos os filtros são opcionais e combinados. `bimestre` é o número 1–4;
`bimestreId` é o ID do registro. `anoLetivo` e `status` também são aceitos.

Paginação: `page=0&size=20&sort=id,asc` (máximo 100 por página).
Professor só consulta seus próprios acompanhamentos; responsável só os publicados
de seus alunos. Administrador pode consultar qualquer status.

## 22. Exportar CSV

`GET /relatorios/exportar/csv?anoLetivo=2026&bimestre=3`, com token do administrador.
Aceita os mesmos filtros e exporta todos os resultados, sem limitar à primeira página.
Baixe a resposta como arquivo; não interprete como JSON. UTF-8/BOM, `;` e aspas
preservam acentuação e campos com separadores/quebras de linha no Excel.
Campos iniciados por indicadores de fórmulas recebem um apóstrofo preventivo.

## Atualização, inativação e erros

PUT dos cadastros usa o mesmo formato do POST, com o ID na rota.
PUT de usuário recebe senha e gera um novo hash; ela nunca é devolvida pelo GET.
DELETE inativa e retorna 204, preservando relacionamentos e histórico.

Erros seguem este formato, sem stack trace ou valores de senhas/tokens:

```json
{"timestamp":"2026-09-15T22:00:00Z","status":409,"erro":"Conflict","mensagem":"Período de digitação do bimestre está fechado","path":"/acompanhamentos"}
```

400: dados inválidos; 401: login/token ausente ou inválido; 403: perfil/ação sem
permissão; 404: recurso inexistente ou fora do escopo de consulta; 409: duplicidade,
período fechado ou transição inválida. Criação retorna 201, leitura/atualização 200
e inativação 204.
