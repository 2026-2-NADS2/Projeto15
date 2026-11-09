# Verificações executadas

- Etapa 01: inspecionados arquivos e Git; nenhum código existente para compilar.
- Etapa 02: compilação e teste inicial passaram com JDK 17 e Maven locais.

- Etapa 03: entidades e enums compilados; teste existente passou.

- Etapa 04: compilação e testes passaram.

- Etapa 05: compilação e testes passaram.

- Etapa 06: compilação e testes passaram.

- Etapa 07: compilação e testes passaram.

- Etapa 08: compilação e testes passaram.

- Etapa 09: compilação e testes passaram.

- Etapa 10: compilação e testes passaram.

- Etapa 11: compilação e testes passaram.

- Etapa 12: compilação e testes passaram.

- Etapa 13: compilação e testes passaram.

- Etapa 14: compilação e testes passaram.

- Etapa 15: compilação e testes passaram.

- Etapa 16: compilação e testes passaram.

- Etapa 17: compilação e testes passaram.

- Etapa 18: compilação e testes passaram.

- Etapa 19: compilação e testes passaram.

- Etapa 20: Maven Wrapper executou `verify` com sucesso; 19 testes, zero falhas,
  zero erros e zero testes ignorados. JAR executável gerado em
  `target/kfka-1.0.0.jar`.
- Fluxo completo testado por HTTP/MockMvc: cadastro administrativo, login de cada
  perfil, rascunho, envio, revisão, publicação, consulta do responsável, ciência,
  PDF e exportação CSV.
- Schema inicial e view executados em H2 em modo MySQL. O teste adicional
  `SchemaProducaoTest` usa `ddl-auto=validate`, os scripts SQL e configuração de
  produção. Ele protege contra regressão no tipo de texto longo do histórico.
- Inicialização real do servidor com perfil `prod`, banco H2 de verificação e
  schema previamente criado passou: `/status` retornou 200, `/alunos` sem token
  retornou 401 e `/v3/api-docs` retornou 200. Servidor encerrado após a checagem.
- PDF renderizado e inspecionado visualmente nas três páginas após a última
  alteração: margens, acentuação, texto longo e rodapés sem cortes.
- SQL inicial foi derivado do Hibernate com dialeto MySQL e recebeu nomes claros
  de constraints e checks do bimestre. Nenhuma credencial real nos arquivos.
- Não houve servidor MySQL nem Docker disponível localmente. A suíte MySQL com
  validação do schema está preparada no workflow, mas não foi executada aqui.
  Build Docker e deploy também não foram executados.

Ferramentas de verificação: JDK 17 e Maven preparados em `/tmp/kfka-tools`, sem
alteração da instalação global do computador. A compilação final usou o Maven
Wrapper incluído no backend.
