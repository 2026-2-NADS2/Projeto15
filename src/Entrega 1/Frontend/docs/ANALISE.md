# Análise anterior à implementação

Fonte: controllers, models, services, security, repositories e configurações em `backend/src/main`. Nenhum arquivo do backend será alterado.

## 1–7. Contratos, controllers, modelos, DTOs, enums e autenticação

| Controller | Endpoints reais |
|---|---|
| UsuarioController | POST /usuarios/logar (público); GET /usuarios?nome; GET /usuarios/{id}; POST /usuarios e /usuarios/cadastrar; PUT e DELETE /usuarios/{id} (admin) |
| AlunoController | GET /alunos?nome; GET /alunos/{id}; POST /alunos; PUT/DELETE /alunos/{id}; GET /alunos/{id}/acompanhamentos |
| ProfessorController | GET/POST /professores; GET/PUT/DELETE /professores/{id} |
| ResponsavelController | GET/POST /responsaveis; GET/PUT/DELETE /responsaveis/{id} |
| TurmaController | GET/POST /turmas; GET/PUT/DELETE /turmas/{id} |
| AreaDisciplinaController | GET/POST /areas; GET/PUT/DELETE /areas/{id} |
| DisciplinaController | GET/POST /disciplinas; GET/PUT/DELETE /disciplinas/{id} |
| BimestreController | GET/POST /bimestres; GET/PUT /bimestres/{id}; sem DELETE |
| TagController | GET/POST /tags; GET/PUT/DELETE /tags/{id} |
| MatriculaController | GET/POST /matriculas; GET/PUT/DELETE /matriculas/{id} |
| VinculoTurmaController | GET/POST /vinculos; GET/PUT/DELETE /vinculos/{id} |
| AcompanhamentoController | GET/POST /acompanhamentos; GET/PUT /acompanhamentos/{id}; PATCH /{id}/enviar, iniciar-revisao, devolver, publicar, cancelar; PUT /{id}/revisao; GET /{id}/historico |
| CienciaController | POST /acompanhamentos/{id}/ciencia; observacao opcional até 1000 caracteres |
| PdfController | GET /acompanhamentos/{id}/pdf, somente publicado, admin/responsável |
| RelatorioController | GET /relatorios; GET /relatorios/exportar/csv (admin) |
| StatusController | GET /status (público), {status: UP} |

Models: Usuario, Aluno, Professor, Responsavel, Turma, AreaDisciplina, Disciplina, Matricula, VinculoTurma, Bimestre, Tag, Acompanhamento, HistoricoAcompanhamento, CienciaResponsavel.

DTOs: UsuarioCadastro, UsuarioLogin, UsuarioLoginResposta; DadosProfessor(usuarioId), DadosResponsavel(usuarioId, alunosIds), DadosDisciplina(nome, areaDisciplinaId, ativo), DadosMatricula(alunoId, turmaId, ativo), DadosVinculo(professorId, disciplinaId, turmaId, ativo); AcompanhamentoCadastro(alunoId, turmaId, disciplinaId, bimestreId, descricao, media, tagsIds); AcompanhamentoEdicao(descricao, media, tagsIds); AcompanhamentoResposta com relações, ProfessorResumo(id,nome), datas e status; FiltroRelatorio; HistoricoResposta; CienciaCadastro; CienciaResposta; ErroResposta.

Enums: PerfilUsuario = ADMINISTRADOR, PROFESSOR, RESPONSAVEL. StatusAcompanhamento = RASCUNHO, ENVIADO_REVISAO, EM_REVISAO, DEVOLVIDO_AJUSTES, PUBLICADO, CANCELADO.

Login retorna {id,nome,email,perfil,token}. JWT assinado HMAC, issuer kfka, subject email, claims id/perfil/iat/exp. BCrypt, sessão stateless, Bearer, sem refresh token ou logout servidor. Frontend centraliza sessão e remove token em 401. Recursos fora do escopo retornam 404. Preferência de tema persiste; token fica no sessionStorage.

Listas são Spring Page: content, number, size, totalElements, totalPages, first, last. page base zero, máximo 100. Busca nome apenas em alunos/usuários. Relatórios: turmaId, disciplinaId, professorId, bimestreId, alunoId, tagId, anoLetivo (2000–2100), bimestre (1–4), status. Erros: {timestamp,status,erro,mensagem,path}. 201 criação, 200 leitura/edição, 204 inativação; 400,401,403,404,409,500 tratados sem stack trace.

Services analisados: CadastroService, RelacionamentoService, UsuarioService, AcessoService, AcompanhamentoService, MediaService, HistoricoService, CienciaService, PdfService, RelatorioService. Professor consulta somente vínculos ativos próprios; responsável somente alunos vinculados e acompanhamentos PUBLICADO. Escrita administrativa exige admin. Criação/envio/edição exige professor proprietário, vínculo e matrícula ativos, ano compatível, janela aberta (abertura inclusiva e encerramento exclusivo, America/Sao_Paulo por padrão). Rascunho/devolvido podem ser editados; enviado inicia revisão; em revisão pode devolver/publicar/editar. Publicado e cancelado são terminais. DELETE inativa. Matrícula/vínculo só permitem modificar ativo. Ano da turma e ano/número do bimestre são imutáveis. Usuário do professor/responsável é imutável. Edição do usuário exige nova senha.

## 8–14. Estado atual e suporte acadêmico

Não havia frontend, package.json ou configuração React. Backend implementa o fluxo completo de acompanhamentos descrito acima. Ausentes: dashboards agregados, pendências de acompanhamentos não iniciados, notificações, edição da própria conta, consulta de ciências, notas individuais, avaliações, faltas/presenças, frequência e aproveitamento.

Média é um BigDecimal informado pelo professor, até duas casas decimais; MediaService valida escala configurável MEDIA_MINIMA/MEDIA_MAXIMA (padrão 0–10). Não calcula média de notas ou média geral. Frontend exibe cada média real sem inventar agregação, limiar de aprovação ou percentual. Configuração frontend da escala deve acompanhar o servidor, pois não existe endpoint de parâmetros.

## 15–21. Páginas, painéis e menus

Admin: painel gerencial com totais cadastrados (sem chamar de ativos), contagens por status via totalElements de relatórios, fila paginada de revisão; Acadêmico (alunos, professores, responsáveis, turmas, áreas, disciplinas); Organização (matrículas, vínculos, bimestres, tags); Acompanhamentos/Revisões por status; Relatórios e CSV; Usuários; Minha conta somente leitura.

Professor: painel operacional com suas turmas/alunos, contagens de rascunhos/devolvidos/enviados, períodos cadastrados; Minhas turmas, Meus alunos, acompanhamento novo/rascunhos/enviados/devolvidos/publicados, relatórios, conta. Formulário filtra aluno por matrícula e disciplina por vínculo da turma, ano do bimestre compatível. Página da turma mostra matrículas/vínculos e permite consultar os acompanhamentos dessa turma.

Responsável: painel simples com seletor de filho proveniente de GET /alunos, filtro ano/bimestre suportado em relatórios, médias por acompanhamento/disciplina e cartões publicados; Meus filhos, Acompanhamentos, Relatórios/PDF, conta. Detalhe mostra descrição, média, tags e professor; confirma ciência e mostra resposta real. Não haverá falsas rotas de notas/frequência nem contagem inventada de ciências pendentes.

## 22–24. Design e componentes

Claro: background #F8FAFC; secundário #F1F5F9; surface #FFFFFF; texto #0F172A/#475569/#64748B; primary #2563EB; secondary #1E3A8A; success #16A34A; warning #D97706; danger #DC2626; borda #E2E8F0.
Escuro: background #0F172A; secundário #111827; surface #1E293B/#263449; hover/borda #334155; texto #F8FAFC/#CBD5E1/#94A3B8; primary #60A5FA; success #4ADE80; warning #FBBF24; danger #F87171.
Tokens CSS centralizados e Tailwind. Tons de texto auxiliar, atenção e erro são ajustados para contraste mínimo de 4,5:1 nas superfícies correspondentes. Tema Claro/Escuro/Sistema com inicialização antes do React. Sidebar esquerda sanfona, recolhível, drawer mobile, header com conta/logout/tema. Sem notificações artificiais.
Componentes: Button, Card, Badge, Field, EmptyState, Loading, ErrorState, Pagination, ConfirmDialog, Toast, PageHeader, Sidebar/Header/Layout; hooks para consultas canceláveis e debounce; AuthContext/ThemeContext.

## 25–27. Rotas, services e ordem

/login, /dashboard, /alunos e /alunos/:id, /professores e /professores/:id, /responsaveis e /responsaveis/:id, /turmas e /turmas/:id, /areas, /disciplinas, /bimestres, /tags, /matriculas, /vinculos, /usuarios (cadastros com novo/editar), /acompanhamentos, /acompanhamentos/novo, /acompanhamentos/:id, /acompanhamentos/:id/revisao, /relatorios, /minha-conta. Rotas e menu por perfil.
Services: API Axios com interceptor; authService; cadastroService tipado por recurso; acompanhamentoService (CRUD, transições, histórico, ciência); relatorioService (filtros e CSV); downloads blob. Sem notaService/frequenciaService enquanto inexistentes.
Ordem: análise/mapa → setup e tokens/tema → sessão/login/proteções → layout → cadastros/relações → acompanhamentos/revisão → dashboards por perfil → área família/ciência/PDF → relatórios/CSV → build, testes contratos, fluxos reais e revisão responsiva/acessível. Nenhuma alteração automática do backend.

## Necessidades adicionais do backend (proposta, não implementada)

1. Avaliacao: id, vínculo professor/disciplina/turma, bimestreId, nome, data, valorMaximo, peso e ativo. Nota: id, avaliacaoId, alunoId, valor, datas de criação/edição; unicidade avaliação/aluno. POST/GET/PUT de avaliações e lançamento de notas individual/em lote atômico. Professor escreve apenas no seu vínculo e alunos matriculados, janela de lançamento validada; responsável lê somente informações publicadas do filho. Validar 0 ≤ nota ≤ valorMaximo, pesos e avaliação do mesmo ano.
2. Definir cálculo institucional de médias (ponderação, recuperação, arredondamento, incompletas e publicação) antes de implementá-lo. DTO de desempenho por aluno/bimestre/disciplina deve retornar notas permitidas, média calculada ou ausente, escala e regra. A média atual do acompanhamento não deve ser silenciosamente substituída.
3. Aula/Chamada: id, vínculo, data, quantidade de aulas; Presenca: id, chamadaId, alunoId, situação definida pela escola, justificativa opcional; unicidade chamada/aluno. POST/GET/PUT chamadas e presenças em lote; professor do vínculo, matrícula ativa e datas validadas. Alternativamente, modelo de faltas agregadas se escolhido pela escola; não manter os dois sem regra de reconciliação.
4. GET desempenho/frequência: totais de aulas contabilizadas, presentes, faltas e percentual calculado no servidor, por filho/disciplina/bimestre; denominador zero produz ausência de indicador, não 100%. Definir justificadas, transferências e limite escolar. Admin vê escola, professor seu vínculo, família seus filhos.
5. Aproveitamento: definir fórmula, escala, pesos e dados insuficientes. GET resumo por perfil deve retornar média geral/frequência/aproveitamento somente quando calculáveis, com regra e escala. Admin precisa agregados por turma/status; professor pendências de combinações aluno/vínculo/bimestre sem registro. Não inferir esses valores de uma página de registros.
6. GET ciência própria por acompanhamento (ou campo de ciência própria no DTO) com id/dataHoraCiencia/observacao; endpoint de pendências paginado e contagem por responsável. A resposta atual do acompanhamento não contém ciência. POST existente só informa a confirmação recém-realizada, e 409 confirma duplicidade sem informar a data.
7. Configuração pública autenticada: escala de média, fuso do calendário e limites; sessão /me e edição própria/alteração de senha caso desejada. Notificações e motivo de devolução precisam campos/contratos próprios antes de exibição.

Endpoints desta seção são propostas a aprovar futuramente, nunca chamadas pelo frontend atual.
