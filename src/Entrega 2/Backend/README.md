# KFKA — Plataforma de Acompanhamento Escolar

API para escolas de Ensino Fundamental. O administrador cadastra a estrutura e
configura os bimestres; o professor registra acompanhamentos e envia para revisão;
o administrador publica; o responsável consulta seus alunos, registra ciência e
baixa o PDF. Administradores consultam relatórios e exportam CSV.

## Tecnologias e organização

Java 17, Spring Boot 3.5.16, Maven 3.9.11, Spring Web, Data JPA, Jakarta Validation,
Spring Security, BCrypt, JWT/JJWT 0.12.6, MySQL, SpringDoc 2.8.17, Apache PDFBox
3.0.7, JUnit 5 e MockMvc. H2 é usado somente nos testes locais.
Sem Lombok, exclusão em cascata ou arquiteturas adicionais.

```text
backend/
├── src/main/java/br/com/kfka/
│   ├── model/         entidades, enums e DTOs
│   ├── repository/    interfaces JpaRepository e consultas utilizadas
│   ├── controller/    requisições e respostas HTTP
│   ├── service/       regras, relacionamentos e escopo de acesso
│   ├── security/      UserDetails, JWT, filtro e permissões
│   ├── exception/     erros padronizados
│   └── config/        BCrypt, relógio, Swagger e administrador inicial
├── src/main/resources/
├── src/test/
├── sql/               schema, índices e view MySQL
├── docs/              plano, verificações e exemplos da API
├── pom.xml
├── mvnw / mvnw.cmd
├── Dockerfile
└── compose.yaml
```

Referência direta: [Cookbook Generation — Spring](https://github.com/conteudoGeneration/cookbook_java_fullstack/tree/main/04_spring).
As classes com getters/setters, repositories, REST, BCrypt, UserDetails, filtro JWT
e SpringDoc seguem os conceitos do Blog Pessoal. As authorities foram adaptadas
para os três perfis e as regras de propriedade ficam na camada Service.
O [plano](docs/IMPLEMENTACAO.md) registra as aulas consultadas e a ordem adotada.

## Pré-requisitos

- JDK 17, com `JAVA_HOME` configurado.
- MySQL 8 ou superior, ou Docker com Docker Compose.
- Maven é opcional: `./mvnw` baixa a versão fixada no projeto na primeira execução.
- No Windows, substitua `./mvnw` por `mvnw.cmd`.

## MySQL e variáveis de ambiente

Para usar um servidor MySQL existente, crie um banco vazio `kfka` com charset
`utf8mb4` e um usuário dedicado. Configure a senha fora do repositório.
Em um banco vazio, execute na ordem:

```bash
mysql -h localhost -u SEU_USUARIO -p kfka < sql/01_schema.sql
mysql -h localhost -u SEU_USUARIO -p kfka < sql/02_view_relatorios.sql
```

`01_schema.sql` cria tabelas, índices, chaves estrangeiras e restrições únicas.
`02_view_relatorios.sql` cria `vw_acompanhamentos_publicados`, sem senha ou e-mail.
O schema inicial deve ser executado uma única vez; a view pode ser reaplicada.
Se as tabelas já foram criadas pelo perfil `dev`, aplique somente a view.
Não aplique o schema inicial sobre tabelas existentes. Alterações futuras de schema
precisam de scripts incrementais revisados antes do perfil `prod`.

Copie `.env.example` para `.env` e preencha os valores. O arquivo `.env` é ignorado
pelo Git. O Spring Boot não carrega `.env` automaticamente; ele precisa receber as
variáveis exportadas pelo terminal, IDE ou plataforma. Docker Compose lê `.env`.
Para carregar um `.env` local escrito por você, compatível com o shell:

```bash
set -a
. ./.env
set +a
```

Valores com espaços ou caracteres especiais precisam estar entre aspas no `.env`.

| Variável | Uso |
|---|---|
| `DB_URL` | URL JDBC, por exemplo `jdbc:mysql://localhost:3306/kfka?serverTimezone=America/Sao_Paulo` |
| `DB_USERNAME` | Usuário dedicado do MySQL |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Segredo aleatório com no mínimo 32 bytes UTF-8; obrigatório, sem valor padrão |
| `JWT_EXPIRACAO_MINUTOS` | Duração do token, padrão 120 |
| `CORS_ORIGINS` | Origens completas separadas por vírgula; obrigatório em produção |
| `SPRING_PROFILES_ACTIVE` | `dev` ou `prod` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Cadastro inicial opcional nos perfis `dev` ou `bootstrap` |
| `MEDIA_MINIMA`, `MEDIA_MAXIMA` | Escala da média, padrão 0–10, até duas casas decimais |
| `FUSO_HORARIO` | Padrão `America/Sao_Paulo` |
| `PORT` | Porta HTTP, padrão 8080 |
| `MYSQL_ROOT_PASSWORD` | Apenas Docker Compose: senha inicial do root do banco |
| `MYSQL_PORT` | Apenas Docker Compose: porta local do banco, padrão 3306 |

Você pode gerar um segredo com `openssl rand -hex 32` e colocá-lo apenas no ambiente.
Não publique `.env`, credenciais ou tokens. A chave JWT deste projeto usa os bytes
UTF-8 do valor informado, sem decodificação Base64.

## Executar

Dentro de `src/Entrega 2/Backend`, com as variáveis configuradas:

```bash
./mvnw spring-boot:run
```

No Windows (Git Bash), se o `mvnw` reclamar do `JAVA_HOME`, aponte para o JDK 17
instalado e carregue o `.env` no mesmo comando:

```bash
export JAVA_HOME="C:/Program Files/Java/jdk-17" && set -a && . ./.env && set +a && ./mvnw spring-boot:run
```

O perfil padrão é `dev`, que usa `ddl-auto=update`. Em produção, `prod` usa
`ddl-auto=validate` e exige schema previamente criado; não altera o banco.

Para executar tudo com Docker:

```bash
cp .env.example .env
# Preencha .env com senhas, JWT_SECRET e as origens permitidas.
docker compose up --build
```

Os scripts SQL são executados automaticamente pelo MySQL somente quando o volume
é inicializado pela primeira vez. O volume preserva os dados entre reinicializações.
As imagens Docker não foram construídas neste ambiente, que não possui Docker.

## Administrador inicial

Em desenvolvimento, configure `ADMIN_EMAIL` e `ADMIN_PASSWORD` no ambiente.
A senha deve ter entre 8 e 72 caracteres e no máximo 72 bytes UTF-8.
O cadastro ocorre apenas se ainda não existir usuário com perfil ADMINISTRADOR.
Um usuário existente nunca tem sua senha sobrescrita pelo inicializador.
Remova essas variáveis depois do cadastro inicial.

Em produção, após preparar o schema, faça uma execução controlada inicial com
`SPRING_PROFILES_ACTIVE=prod,bootstrap` e essas variáveis. Depois reinicie apenas
com `prod`, removendo `ADMIN_EMAIL` e `ADMIN_PASSWORD` do ambiente.
Todos os próximos usuários são cadastrados por um administrador autenticado.
Não existe cadastro público, inclusive em `/usuarios/cadastrar`.

## Testar e empacotar

Os testes locais não exigem MySQL nem credenciais: usam o perfil `test` com banco
H2 descartável e segredo exclusivo de teste.

```bash
./mvnw test
./mvnw clean verify
java -jar target/kfka-1.0.0.jar
```

A verificação final executou 20 testes sem falhas. A suíte cobre cadastro/login, BCrypt, proteção JWT, usuários inativos, propriedade
por professor/responsável, matrícula e vínculo, período do bimestre, média,
duplicidade, revisão, devolução, publicação, histórico, ciência, PDF e CSV.
O teste de PDF também verifica texto e geração de múltiplas páginas. Um teste adicional
inicializa com configuração de produção e valida o schema SQL e a view no H2.

Para rodar a mesma suíte em um MySQL **exclusivo de testes**, crie o schema e a view
nesse banco e configure `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_DB_PASSWORD`,
`TEST_DB_DRIVER=com.mysql.cj.jdbc.Driver` e `TEST_DDL_AUTO=validate`, depois execute
`./mvnw verify`. Nunca aponte os testes para o banco com dados reais.
Sem `TEST_DDL_AUTO`, os testes usam `create-drop`. Veja [verificações](docs/VERIFICACOES.md).

## Swagger e autenticação

- Swagger: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Disponibilidade: `GET /status`
- Login: `POST /usuarios/logar`, corpo `{ "email": "...", "senha": "..." }`.

O login retorna `id`, `nome`, `email`, `perfil` e `token`, sem senha.
Use `Authorization: Bearer TOKEN` ou o botão **Authorize** do Swagger.
No botão, informe somente o token. O backend é stateless, sem sessão de login.
Tokens inválidos/expirados e usuários inativos recebem 401; acesso proibido recebe
403. Recursos fora do escopo de consulta recebem 404 para não expor sua existência.

## Perfis e principais endpoints

| Rotas | Leitura | Escrita |
|---|---|---|
| `/usuarios` | Administrador | Administrador |
| `/alunos`, `/turmas` | Admin; professor/responsável com escopo | Administrador |
| `/professores`, `/disciplinas`, `/matriculas`, `/vinculos` | Admin; professor com escopo | Administrador |
| `/responsaveis` | Admin; responsável vê seu cadastro | Administrador |
| `/areas`, `/tags`, `/bimestres` | Admin e professor | Administrador |
| `/acompanhamentos` | Todos, com escopo | Professor cria rascunho e edita seus registros permitidos |
| `/relatorios` | Todos, com escopo | Consulta |
| `/relatorios/exportar/csv` | Administrador | Exportação |

Os CRUDs oferecem GET coleção, GET `/{id}`, POST e PUT `/{id}`.
DELETE `/{id}` **inativa** usuários, professores/responsáveis, alunos, áreas,
disciplinas, turmas, tags, matrículas e vínculos; não exclui fisicamente registros.
Bimestres não têm DELETE; suas datas podem ser ajustadas por PUT.
Ano letivo e número de bimestre não podem ser trocados em registros existentes.

| Ação | Endpoint | Perfil |
|---|---|---|
| Acompanhamentos do aluno | `GET /alunos/{id}/acompanhamentos` | Conforme escopo |
| Enviar | `PATCH /acompanhamentos/{id}/enviar` | Professor proprietário |
| Iniciar revisão | `PATCH /acompanhamentos/{id}/iniciar-revisao` | Administrador |
| Editar durante revisão | `PUT /acompanhamentos/{id}/revisao` | Administrador |
| Devolver | `PATCH /acompanhamentos/{id}/devolver` | Administrador |
| Publicar | `PATCH /acompanhamentos/{id}/publicar` | Administrador |
| Cancelar | `PATCH /acompanhamentos/{id}/cancelar` | Administrador |
| Histórico | `GET /acompanhamentos/{id}/historico` | Admin ou professor proprietário |
| Registrar ciência | `POST /acompanhamentos/{id}/ciencia` | Responsável vinculado, publicado |
| PDF | `GET /acompanhamentos/{id}/pdf` | Admin ou responsável vinculado, publicado |

Listagens são paginadas: `?page=0&size=20&sort=id,asc`, máximo 100 por página.
Usuários e alunos permitem `?nome=texto`.
Relatórios aceitam `turmaId`, `disciplinaId`, `professorId`, `alunoId`, `tagId`,
`bimestreId`, `anoLetivo`, `bimestre` (número 1–4) e `status`.
O CSV aceita os mesmos filtros e exporta todas as páginas, com UTF-8/BOM,
separador `;`, aspas escapadas e proteção contra fórmulas em campos textuais.
Para conjuntos muito grandes, use filtros: o arquivo final é montado em memória.

## Fluxo e regras

```text
RASCUNHO → ENVIADO_REVISAO → EM_REVISAO → PUBLICADO
                                 ↓
                         DEVOLVIDO_AJUSTES → ENVIADO_REVISAO
```

O administrador pode cancelar qualquer estado ainda não publicado, exceto
CANCELADO. PUBLICADO e CANCELADO são terminais. Rascunho não pode ser publicado
diretamente. O administrador precisa iniciar revisão antes de editar ou publicar.

Criação, edição e envio pelo professor exigem vínculo ativo turma/disciplina,
matrícula ativa do aluno, registros acadêmicos ativos e bimestre do mesmo ano da
turma. A janela de digitação inclui a abertura e exclui o instante de encerramento,
no fuso configurado. A revisão administrativa pode continuar após o prazo.
O professor do acompanhamento vem do login, nunca do corpo da requisição.

Responsáveis veem apenas PUBLICADO de seus alunos, inclusive em filtros,
consultas diretas e PDF. Cada responsável registra ciência uma única vez por
acompanhamento. Dois responsáveis do mesmo aluno podem registrar suas próprias
ciências. Todas as mudanças do acompanhamento preservam auditoria.

Veja [GUIA_API.md](docs/GUIA_API.md) para os corpos de requisição e um roteiro completo.

## Frontend no mesmo endereço (Entrega 2)

Para o deploy na nuvem, o frontend React é compilado e empacotado dentro do `.jar`
(`src/main/resources/static`, gerado no build e ignorado pelo Git). A classe
`config/FrontendFilter` organiza o endereço único:

| Caminho | Atende |
|---|---|
| `/api/...` | API; o prefixo `/api` é removido antes de chegar aos controllers |
| `/`, `/login`, `/dashboard`, `/acompanhamentos`... (navegador) | `index.html` do React |
| `/assets/...`, `/kfka-logo.png` | Arquivos das telas |
| `/status`, `/swagger-ui/index.html`, `/v3/api-docs` | Iguais ao modo local |

Os caminhos antigos da API, sem `/api`, continuam funcionando para o Swagger e para
clientes que não sejam o navegador. Sem o frontend empacotado (execução local com
`mvnw spring-boot:run`), o backend funciona exatamente como na Entrega 1.

## Deploy na nuvem (Railway)

Um projeto no Railway com dois serviços: **MySQL** e **App**. O App usa o
[`Dockerfile`](../Dockerfile) da pasta `src/Entrega 2`, que compila o frontend,
coloca as telas dentro do backend e gera um único `.jar`.

```
https://SEU-APP.up.railway.app
  ├── /                       telas (React)
  ├── /api/...                API (Spring Boot)
  └── /swagger-ui/index.html  documentação
             ↓
        MySQL (rede privada do Railway)
```

### 1. Criar o projeto e o MySQL

1. Entre em <https://railway.com> com a conta do GitHub.
2. **New Project → GitHub Repository →** `Projeto15`. O primeiro deploy falha porque
   ainda falta configurar; isso é esperado.
3. No mesmo projeto: **+ Create → Database → MySQL**.

### 2. Criar as tabelas

1. No serviço **MySQL → Variables**, copie `MYSQL_PUBLIC_URL`
   (`mysql://root:SENHA@HOST:PORTA/railway`).
2. Dentro de `src/Entrega 2/Backend`, troque HOST e PORTA e informe a senha quando pedir:

   ```bash
   mysql -h HOST -P PORTA -u root -p railway < sql/01_schema.sql
   ```

   ```bash
   mysql -h HOST -P PORTA -u root -p railway < sql/02_view_relatorios.sql
   ```

### 3. Configurar o App

**Settings:** Root Directory `src/Entrega 2` · Healthcheck Path `/status` ·
Networking → **Generate Domain** (porta `8080`).

**Variables** (Raw Editor):

```
SPRING_PROFILES_ACTIVE=prod,bootstrap
PORT=8080
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
CORS_ORIGINS=https://${{RAILWAY_PUBLIC_DOMAIN}}
JWT_SECRET=TROCAR
ADMIN_EMAIL=TROCAR
ADMIN_PASSWORD=TROCAR
FUSO_HORARIO=America/Sao_Paulo
```

- Os valores `${{...}}` são preenchidos pelo Railway. Se o serviço do banco tiver
  outro nome, troque `MySQL` por ele.
- `JWT_SECRET`: gere com `openssl rand -hex 32`.
- `ADMIN_PASSWORD`: 8 a 72 caracteres.

### 4. Conferir

- `/status` → `{"status":"UP"}`
- `/` abre a tela de login, e o login do administrador abre o painel
- `/swagger-ui/index.html` abre o Swagger

### 5. Depois do primeiro login

Troque `SPRING_PROFILES_ACTIVE` para `prod` e apague `ADMIN_EMAIL` e `ADMIN_PASSWORD`.
O Railway refaz o deploy sozinho.

### Problemas comuns

| Mensagem no log | Causa provável |
|---|---|
| `Schema-validation: missing table` | Os scripts SQL do passo 2 não foram aplicados |
| `Communications link failure` | `DB_URL` errada ou serviço do banco com outro nome |
| `Public Key Retrieval is not allowed` | Falta `&allowPublicKeyRetrieval=true` na `DB_URL` |
| Healthcheck falhando | `PORT` diferente de `8080` ou porta do domínio diferente |

Nenhuma credencial foi adicionada ao código: tudo é configurado como variável no Railway.
