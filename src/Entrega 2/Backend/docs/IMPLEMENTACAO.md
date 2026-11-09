# Plano e verificação — KFKA

Inspeção inicial: repositório vazio, apenas `.git`; nenhum frontend ou configuração existente.
Backend isolado em `/backend`. Java 17 (baseline do Cookbook) e Spring Boot 3.5.16
(linha 3 compatível com Jakarta e com o estilo didático solicitado), Maven 3.9.11.
Não foi adotada a versão 4 citada em trechos atualizados do Cookbook: este projeto
usa consistentemente Boot 3 e SpringDoc 2.8.17. Sem Lombok.

## Estrutura

`src/main/java/br/com/kfka/{model,repository,controller,service,security,exception,config}`;
`src/main/resources`; `src/test`; `sql`; `docs`.
DTOs de entrada e saída ficam próximos ao domínio em `model`, sem criar arquitetura extra.

## Entidades e relacionamentos

- Usuario: perfil, e-mail único, senha BCrypt, ativo.
- Professor → Usuario (1:1); Responsavel → Usuario (1:1).
- Responsavel ↔ Aluno (N:N).
- AreaDisciplina → Disciplina (1:N).
- Matricula: Aluno + Turma, combinação única.
- VinculoTurma: Professor + Disciplina + Turma, combinação única.
- Bimestre: ano letivo + número único, janela de digitação.
- Acompanhamento → Aluno, Turma, Disciplina, Professor, Bimestre (N:1).
- Acompanhamento ↔ Tag (N:N).
- HistoricoAcompanhamento → Acompanhamento e Usuario (N:1).
- CienciaResponsavel → Responsavel e Acompanhamento (N:1, combinação única).

Enums: PerfilUsuario (ADMINISTRADOR, PROFESSOR, RESPONSAVEL) e
StatusAcompanhamento (RASCUNHO, ENVIADO_REVISAO, EM_REVISAO,
DEVOLVIDO_AJUSTES, PUBLICADO, CANCELADO).

## Ordem de implementação

1. Inspeção e apresentação do plano.
2. Spring Boot, Maven, perfis e banco por ambiente.
3. Models e enums.
4. Repositories.
5. CRUDs básicos.
6. Relacionamentos e validação dos vínculos.
7. Usuario e autenticação.
8. Spring Security, JWT e perfis.
9. Restrições de acesso do Professor.
10. Acompanhamento e transições.
11. Revisão e histórico.
12. Acesso do Responsavel.
13. Ciência.
14. PDF.
15. Relatórios e CSV.
16. Tratamento global de erros.
17. Swagger.
18. Testes críticos.
19. Revisão de segurança e banco.
20. README e preparação para deploy, sem publicar.

Verificações por etapa são registradas em `VERIFICACOES.md`.

## Referência direta

Cookbook: https://github.com/conteudoGeneration/cookbook_java_fullstack/tree/main/04_spring
Foram consultadas as aulas 04–06, 13, 16–20, 22, 24 e 25: entidades com
getters/setters, JpaRepository, REST com ResponseEntity, relacionamentos,
UserDetails, BCrypt, filtro JWT, SecurityFilterChain stateless, testes e SpringDoc.
Adaptações necessárias: authorities reais, cadastro administrativo, segredo externo,
consultas com escopo de propriedade, auditoria e regras do domínio escolar.
