# Diagnóstico e plano de migração visual

## Escopo

O arquivo `KFKA-Projeto.zip` é usado somente como referência visual. O frontend React, a autenticação JWT, as rotas, os serviços HTTP e o backend Java/Spring Boot do projeto atual permanecem como arquitetura final. Nenhum código C#, controller, service, entidade, migration, configuração ou banco da referência será incorporado.

## Projeto de referência

### Identidade visual

- Tema escuro fixo com fundo quase preto e dois gradientes radiais: roxo no canto superior esquerdo e ciano no canto inferior direito.
- Cores principais: ciano `#44b1d2`, roxo `#9747ff`, texto `#f4f5fa`, texto secundário `#b9c5d5` e painéis ciano translúcidos.
- Tipografia `Instrument Sans`, com `Inter` como alternativa.
- Cabeçalho horizontal de 106 px, logo à esquerda, título da página ao centro e acesso ao perfil à direita.
- Sidebar fixa de 210 px, links em lista simples e botão de saída no final.
- Área de conteúdo com margem lateral de 210 px e espaçamento de 32 × 40 px.
- Cards e painéis com borda ciano, fundo translúcido, raio de 12 px e sem sombras relevantes.
- Botões ciano, campos escuros, badges azulados e barras de progresso com gradiente roxo–ciano.
- No mobile, o cabeçalho reduz para 85 px, o menu vira um painel acionado por botão, as métricas ficam em coluna e os grids passam a uma coluna.

### Menus reais

Comuns:

1. Início
2. Acompanhamentos
3. Meu perfil
4. Sair da conta

Professor:

1. Novo registro

Administrador:

1. Fila de revisão
2. Usuários
3. Alunos
4. Áreas
5. Disciplinas
6. Tags
7. Turmas
8. Anos letivos
9. Bimestres
10. Alunos por turma
11. Vínculos docentes
12. Responsáveis por aluno
13. Auditoria

Responsável não recebe menus administrativos nem criação de acompanhamento.

### Páginas e estruturas encontradas

- Login, recuperação e redefinição de senha.
- Início com três métricas, gráfico de médias por disciplina e card “Seu próximo passo”.
- Lista de acompanhamentos com nove filtros, tabela, paginação e exportação administrativa.
- Criação e edição de acompanhamento.
- Detalhes do relatório com média, descrição, tags, PDF, transições, ciência, observações e histórico.
- Listas e formulários genéricos de cadastros.
- Usuários e cadastro de usuário.
- Auditoria.
- Perfil, acesso negado, página indisponível e erro.

Não há componentes formais de React na referência. A reutilização visual ocorre por classes CSS e pelo layout Razor compartilhado: `heading`, `panel`, `metrics`, `metric`, `grid-two`, `filters`, `table-wrap`, `badge`, `pagination`, `auth` e `edit-form`.

## Projeto atual

- React 19, TypeScript, Vite, React Router e Tailwind integrado a CSS próprio.
- Autenticação JWT centralizada em `AuthContext`, com autorização por perfil e expiração de sessão.
- Integração REST por Axios, com token Bearer, tratamento de 401 e downloads autenticados.
- Layout reutilizável com sidebar responsiva, header, breadcrumb, menu de conta e seletor de tema.
- Componentes reutilizáveis: `Card`, `PageHeader`, `Button`, `Field`, `Badge`, `Pagination`, estados de carregamento/erro/vazio, confirmação e toast.
- Páginas existentes: login, três dashboards por perfil, conta, cadastros de todos os recursos Java, acompanhamentos, relatórios, formulário e detalhes.
- As rotas e permissões atuais já cobrem mais recursos válidos da API Java do que a referência e devem ser preservadas.
- As rotas locais `/demo/admin`, `/demo/professor` e `/demo/responsavel` continuam disponíveis somente no desenvolvimento.

## Correspondência visual e funcional

| Referência | Frontend atual | API Java preservada | Adaptação |
|---|---|---|---|
| Início | `/dashboard` | relatórios e cadastros existentes | Aplicar métricas, painéis e barras da referência, mantendo dados reais por perfil |
| Acompanhamentos | `/acompanhamentos` | filtros reais de acompanhamento | Reorganizar filtros e tabela conforme a referência |
| Fila de revisão | `/acompanhamentos?status=ENVIADO_REVISAO` | filtro de status | Manter rota atual e nome visual equivalente |
| Novo registro | `/acompanhamentos/novo` | criação existente | Aplicar painel e formulário da referência |
| Detalhes | `/acompanhamentos/:id` | detalhes, histórico, ciência e PDF | Aplicar painéis, tags, ações e histórico da referência |
| Cadastros | rotas atuais por recurso | CRUDs Java existentes | Manter URLs atuais e aplicar tabelas/formulários de referência |
| Usuários | `/usuarios` | CRUD de usuários | Aplicar estrutura da lista e do formulário antigos |
| Perfil | `/minha-conta` | sessão JWT atual | Aplicar painel visual da referência |
| Excel antigo | `/relatorios` com CSV atual | exportação existente | Preservar formato suportado pelo Java; transportar apenas o visual |
| Auditoria | sem endpoint correspondente | inexistente | Não criar tela falsa nem endpoint novo |
| Recuperar senha | sem endpoint correspondente | inexistente | Não copiar a autenticação C# |
| Anos letivos | representados nos bimestres atuais | modelo Java atual | Não criar entidade paralela |

## Plano de migração

1. Criar tokens visuais equivalentes às cores, gradientes, tipografia, bordas, raios e medidas da referência.
2. Adaptar o logo, o cabeçalho e a sidebar no componente `Layout`, preservando acessibilidade, rotas e menus permitidos pela API Java.
3. Reestilizar os componentes compartilhados para que cards, tabelas, formulários, botões, badges, filtros, modais e paginação adotem a referência de modo uniforme.
4. Adaptar o login sem copiar recuperação de senha ou autenticação da referência.
5. Reorganizar os dashboards por perfil com a composição visual antiga e os dados reais já consumidos.
6. Migrar listas e formulários de cadastros, mantendo schemas, validações e serviços atuais.
7. Migrar lista, formulário, detalhe, histórico e relatórios de acompanhamento.
8. Validar desktop, tablet e mobile; estados claro/escuro serão revistos porque a referência aprovada é escura.
9. Executar compilação, testes de contratos e fluxos reais no navegador para os três perfis.

## Decisões de preservação

- A hierarquia administrativa atual continuará agrupada quando isso for necessário para acomodar todos os recursos Java, mas os rótulos, a ordem e a aparência serão aproximados dos menus reais da referência.
- Funcionalidades atuais sem equivalente visual antigo continuarão disponíveis e receberão o mesmo sistema visual.
- Funcionalidades antigas sem suporte no Java, como auditoria, recuperação de senha e observação textual do responsável, não serão simuladas.
- O logo fornecido pela referência pode ser usado como ativo visual; não será carregado nenhum código executável do projeto antigo.
