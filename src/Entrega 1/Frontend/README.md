# KFKA — Frontend

React + TypeScript + Vite + React Router + Tailwind CSS + Axios + Phosphor Icons.
Frontend integrado ao backend real deste repositório. Não contém dados de demonstração ou serviços fictícios.

## Executar

Requer Node.js 22 ou superior. Dentro de `frontend/`:

```sh
corepack enable
pnpm install
cp .env.example .env
pnpm dev
```

Abra [a tela de login](http://localhost:5173/login). Inicie o backend seguindo o [README do backend](../backend/README.md). Para uma apresentação local, prepare as contas descritas abaixo; não existe cadastro público.

Também é possível usar `npm install`, `npm run dev`, `npm run build` e `npm test`. O lockfile versionado é do pnpm.

O Vite encaminha `/api` para `http://localhost:8080`, removendo o prefixo. Para outro servidor em desenvolvimento, defina `KFKA_API_TARGET` no ambiente ou `.env`. Para trocar a porta, execute `pnpm dev --port 5174` e inclua essa origem em `CORS_ORIGINS` no backend. A porta é estrita para evitar abrir outra aplicação por acidente.

## Login e senha dos usuários de teste

Estas são as credenciais utilizadas em `backend/src/test/java/br/com/kfka/FluxoEscolarTest.java` e podem ser adotadas para uma demonstração local:

| Perfil | Login (e-mail) | Senha | Acesso principal |
|---|---|---|---|
| Administrador | `admin@teste.local` | `SenhaTeste123!` | Cadastros, revisão e publicação de acompanhamentos. |
| Professor | `professor@teste.local` | `SenhaTeste123!` | Acompanhamentos próprios e turmas vinculadas. |
| Responsável | `responsavel@teste.local` | `SenhaTeste123!` | Acompanhamentos publicados dos alunos vinculados. |

**Essas contas não são criadas automaticamente ao iniciar a aplicação.** A suíte de testes as cria em seu próprio banco. Para entrar pelo frontend, cadastre-as no banco local conforme as etapas a seguir. As credenciais são públicas e exclusivas para testes; não as utilize em produção.

### Preparar o administrador

Depois de configurar o banco e as demais variáveis conforme o README do backend, defina no ambiente local:

```dotenv
SPRING_PROFILES_ACTIVE=dev
ADMIN_EMAIL=admin@teste.local
ADMIN_PASSWORD=SenhaTeste123!
```

Inicie o backend. Se usar `backend/.env`, carregue suas variáveis conforme as instruções do backend: o Spring Boot não lê esse arquivo automaticamente. O administrador inicial só será criado se ainda não existir nenhum usuário com perfil `ADMINISTRADOR`. Alterar essas variáveis não modifica a senha de uma conta existente.

### Preparar professor e responsável

Acesse [o Swagger local](http://localhost:8080/swagger-ui/index.html) com o backend em execução:

1. Execute `POST /usuarios/logar` com o e-mail e a senha do administrador da tabela.
2. Copie o `token` retornado e informe-o em **Authorize**.
3. Execute `POST /usuarios` uma vez para cada corpo abaixo.

Professor:

```json
{
  "nome": "Professora Ana",
  "email": "professor@teste.local",
  "senha": "SenhaTeste123!",
  "perfil": "PROFESSOR",
  "ativo": true
}
```

Responsável:

```json
{
  "nome": "Responsável",
  "email": "responsavel@teste.local",
  "senha": "SenhaTeste123!",
  "perfil": "RESPONSAVEL",
  "ativo": true
}
```

Os cadastros de usuário permitem autenticar. Para utilizar os recursos de cada perfil, associe o usuário professor em `POST /professores`, crie seus vínculos com turma e disciplina e associe o usuário responsável aos alunos em `POST /responsaveis`. Matrículas e bimestres também precisam estar configurados para registrar acompanhamentos. Siga o [roteiro da API](../backend/docs/GUIA_API.md), usando os IDs retornados pelos cadastros.

### Entrar no frontend

1. Abra [http://localhost:5173/login](http://localhost:5173/login).
2. Preencha **E-mail** e **Senha** com uma conta cadastrada.
3. Envie o formulário. O sistema abre `/dashboard` com as opções do perfil autenticado.
4. Para testar outro perfil, encerre a sessão usando a opção de sair e faça um novo login.

Se o acesso falhar, confirme que o backend está em execução, que `KFKA_API_TARGET` aponta para ele e que a conta existe e está ativa nesse banco. As credenciais acima só funcionarão após o cadastro correspondente. Um painel sem registros pode indicar ausência de vínculos ou de acompanhamentos publicados.

## Configuração e publicação

- `VITE_API_URL`: `/api` no desenvolvimento; em produção use proxy `/api` que remova o prefixo ou a URL HTTPS real do backend, sem `/api` adicional.
- `VITE_MEDIA_MINIMA` e `VITE_MEDIA_MAXIMA`: devem corresponder às variáveis `MEDIA_MINIMA`/`MEDIA_MAXIMA` do backend (padrão 0–10).
- `VITE_FUSO_HORARIO`: deve corresponder ao backend (padrão America/Sao_Paulo). Datas de período são horários locais da escola; a janela é validada novamente pelo servidor.
- Backend deve permitir a origem completa do frontend em `CORS_ORIGINS`, mesmo usando o proxy de desenvolvimento, pois o navegador envia Origin.
- Variáveis `VITE_` são públicas; nunca coloque senha, segredo JWT ou credenciais nelas.

```sh
pnpm build
pnpm preview
```

Publique o conteúdo de `dist/`. Configure fallback de rotas para `index.html`; configure o proxy da API ou `VITE_API_URL` antes do build. O preview só inspeciona o build e não substitui a configuração da hospedagem. Nenhum deploy foi realizado.

## Funcionalidades

- Login real, JWT Bearer, expiração da sessão, autorização visual por perfil e logout.
- Painéis distintos para administração, professores e responsáveis; contagens reais de cadastros e status, sem média geral inventada.
- Design system centralizado; Claro/Escuro/Sistema persistente, inicialização antes do React; sidebar esquerda com categorias sanfona, recolhimento desktop e drawer mobile com teclado.
- Cadastros e edição: usuários, alunos, professores, responsáveis, áreas, disciplinas, turmas, matrículas, vínculos, bimestres e tags; inativação com confirmação, exceto bimestres que não oferecem DELETE.
- Consultas e seletores paginados, busca com debounce em alunos/usuários; seletores de usuários respeitam perfil ativo. Os campos imutáveis ficam bloqueados durante edição.
- Professor seleciona vínculo próprio, matrícula compatível e bimestre aberto do mesmo ano. Salva rascunho, edita devolvido e envia. Se salvar funcionar e o envio falhar, informa que o rascunho foi preservado.
- Administrador inicia revisão, ajusta descrição/média/tags, devolve, publica ou cancela conforme status real; histórico com antes/depois.
- Responsável seleciona filho vinculado, consulta médias e observações publicadas por período, confirma ciência e baixa PDF. Ciência já registrada é tratada via resposta 409; não inventa data anterior.
- Relatórios com os filtros reais; exportação CSV administrativa e PDF via blob autenticado, loading/erros tratados.
- Página da própria conta somente leitura, pois não existe endpoint de autoedição.
- Estados vazios, carregamento, erros seguros, feedback e confirmação de ações críticas.

## Organização

`src/components`: UI/layout/seletores; `contexts`: sessão/tema; `hooks`: consulta cancelável/debounce; `pages`: login, painéis, cadastros, acompanhamento/revisão e relatórios; `services`: Axios e contratos; `types`: entidades/DTOs/enums; `utils`: números/datas/escala.

A sessão usa sessionStorage (encerrada ao fechar a sessão da aba); tema usa localStorage. Tokens só são lidos no AuthContext e enviados pelo interceptor. A autorização definitiva é sempre feita no backend.

## Verificação

```sh
pnpm build
pnpm test
pnpm exec playwright install chromium
```

Os testes de contratos usam um adaptador somente nos testes para conferir corpos, métodos, filtros e erros. A aplicação não utiliza esse adaptador.

Os testes de navegador exigem uma **instância descartável do backend**, com banco exclusivo de testes, conta administrativa e CORS configurados para a origem do frontend. Eles criam usuários e registros reais nessa instância; não os execute contra dados da escola. Defina:

- `KFKA_TEST_ACCOUNT_FILE`: caminho externo ao Git para JSON `{ "email": "...", "senha": "..." }` da conta administrativa de testes.
- `KFKA_TEST_API`: URL da API descartável (padrão http://localhost:8081).
- `KFKA_TEST_FRONTEND`: frontend apontando à API descartável (padrão http://localhost:5173).
- `KFKA_TEST_BROWSER`: opcional, caminho do Chromium instalado.

```sh
pnpm exec playwright test
```

Sem `KFKA_TEST_ACCOUNT_FILE`, a integração é explicitamente ignorada. Capturas e traces ficam em `test-results/` (não versionados; podem conter dados da sessão de testes).

## Limites do backend

Não existem notas individuais/avaliações, presenças/faltas/frequência, média geral calculada, aproveitamento, pendências de registros não iniciados, notificações, motivo da devolução nem consulta das ciências já registradas. Essas funcionalidades não têm rotas falsas no frontend.

A média exibida é exclusivamente a média informada no acompanhamento. Totais de cadastros incluem ativos e inativos, pois a API não oferece filtro ativo. Seletores e associações da turma são paginados; consulte páginas adicionais quando houver muitas opções. Não existe endpoint para filtrar matrículas/vínculos por turma; o frontend filtra as opções acessíveis na página atual e informa esse limite.

O [mapa de análise e necessidades adicionais](docs/ANALISE.md) documenta os contratos, permissões, regras e propostas de backend, sem implementá-las.
