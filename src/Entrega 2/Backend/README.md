# KFKA Backend

API da plataforma KFKA para acompanhamento escolar. Professores registram e enviam acompanhamentos; administradores revisam e publicam; responsáveis consultam os registros e confirmam ciência.

## Links do projeto

- [Ver o site KFKA](https://app-production-aedab.up.railway.app)
- [Abrir o Swagger da API](https://app-production-aedab.up.railway.app/swagger-ui/index.html)
- [Código do frontend](../Frontend/)

## Tecnologias

Java 17, Spring Boot 3.5, Maven Wrapper, MySQL 8, Spring Security e JWT. Os testes usam H2.

## Executar com Docker

É necessário ter Docker com Docker Compose. Na pasta deste backend:

1. Copie `.env.example` para `.env`.
2. Defina `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `JWT_SECRET` (mínimo de 32 bytes) e `CORS_ORIGINS`. `ADMIN_EMAIL` e `ADMIN_PASSWORD` são opcionais para criar o administrador inicial.
3. Inicie a aplicação:

```bash
docker compose up --build
```

O Compose inicia o MySQL e a API. Os scripts em `sql/` são executados quando o volume do banco é criado pela primeira vez. O arquivo `.env` não deve ser enviado ao Git.

## Executar localmente

Use JDK 17 e um MySQL disponível. Configure no ambiente as variáveis necessárias de `.env.example` e execute:

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`. O Spring Boot não carrega `.env` sozinho; configure as variáveis no terminal ou na IDE. O perfil `dev` atualiza as tabelas automaticamente. Para um banco já existente, aplique os scripts SQL conforme necessário; em produção, o perfil `prod` valida o schema existente.

## API

- Health check: `GET /status`
- Swagger local: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- A API usa JWT. Faça login em `POST /usuarios/logar` e envie o token como `Authorization: Bearer TOKEN`.

A documentação com o fluxo de uso e exemplos de requisição está em [`docs/GUIA_API.md`](docs/GUIA_API.md). O schema e a view ficam em `sql/`.
