package br.com.kfka;

import br.com.kfka.model.*;
import br.com.kfka.service.UsuarioService;
import br.com.kfka.repository.UsuarioRepository;
import br.com.kfka.security.JwtService;
import com.fasterxml.jackson.databind.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FluxoEscolarTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioService usuarios;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired JwtService jwt;
    @Autowired Clock clock;
    String admin, professor, responsavel;
    long professorId, responsavelId, usuarioProfessorId, alunoId, turmaId, disciplinaId, areaId, bimestreId, tagId, vinculoId, matriculaId;

    JsonNode resposta(MockHttpServletRequestBuilder request, Object corpo, String token, int esperado) throws Exception {
        if (corpo != null) request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(corpo));
        if (token != null) request.header("Authorization", token);
        MvcResult result = mvc.perform(request).andExpect(status().is(esperado)).andReturn();
        String texto = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return texto.isBlank() ? mapper.createObjectNode() : mapper.readTree(texto);
    }
    long cadastrar(String rota, Object corpo) throws Exception { return resposta(post(rota), corpo, admin, 201).get("id").asLong(); }
    String login(String email) throws Exception {
        return "Bearer " + resposta(post("/usuarios/logar"), Map.of("email", email, "senha", "SenhaTeste123!"), null, 200).get("token").asText();
    }
    Map<String, Object> usuario(String nome, String email, String perfil) {
        return Map.of("nome", nome, "email", email, "senha", "SenhaTeste123!", "perfil", perfil, "ativo", true);
    }
    Map<String, Object> bimestre(int numero, LocalDateTime abertura, LocalDateTime encerramento) {
        return Map.of("numero", numero, "anoLetivo", 2026, "dataHoraAbertura", abertura.toString(), "dataHoraEncerramento", encerramento.toString());
    }
    Map<String, Object> acompanhamento(long aluno, long turma, long disciplina, long bimestre) {
        return Map.of("alunoId", aluno, "turmaId", turma, "disciplinaId", disciplina, "bimestreId", bimestre,
            "descricao", "Evolução em leitura e resolução de problemas.", "media", 8.5, "tagsIds", List.of(tagId));
    }
    Map<String, Object> edicao(String descricao, double media) { return Map.of("descricao", descricao, "media", media, "tagsIds", List.of(tagId)); }
    long criar() throws Exception { return resposta(post("/acompanhamentos"), acompanhamento(alunoId, turmaId, disciplinaId, bimestreId), professor, 201).get("id").asLong(); }
    void publicar(long id) throws Exception {
        resposta(patch("/acompanhamentos/" + id + "/enviar"), null, professor, 200);
        resposta(patch("/acompanhamentos/" + id + "/iniciar-revisao"), null, admin, 200);
        resposta(patch("/acompanhamentos/" + id + "/publicar"), null, admin, 200);
    }

    @BeforeEach void prepararEstruturaPelaApi() throws Exception {
        usuarios.salvar(null, new UsuarioCadastro("Admin", "admin@teste.local", "SenhaTeste123!", PerfilUsuario.ADMINISTRADOR, true));
        admin = login("admin@teste.local");
        usuarioProfessorId = cadastrar("/usuarios", usuario("Professora Ana", "professor@teste.local", "PROFESSOR"));
        long usuarioResponsavel = cadastrar("/usuarios", usuario("Responsável", "responsavel@teste.local", "RESPONSAVEL"));
        professorId = cadastrar("/professores", Map.of("usuarioId", usuarioProfessorId));
        alunoId = cadastrar("/alunos", Map.of("nome", "João da Silva", "ativo", true));
        responsavelId = cadastrar("/responsaveis", Map.of("usuarioId", usuarioResponsavel, "alunosIds", List.of(alunoId)));
        areaId = cadastrar("/areas", Map.of("nome", "Linguagens", "ativo", true));
        disciplinaId = cadastrar("/disciplinas", Map.of("nome", "Português", "areaDisciplinaId", areaId, "ativo", true));
        turmaId = cadastrar("/turmas", Map.of("nome", "5º A", "serie", "5º ano", "anoLetivo", 2026, "ativo", true));
        matriculaId = cadastrar("/matriculas", Map.of("alunoId", alunoId, "turmaId", turmaId, "ativo", true));
        vinculoId = cadastrar("/vinculos", Map.of("professorId", professorId, "disciplinaId", disciplinaId, "turmaId", turmaId, "ativo", true));
        tagId = cadastrar("/tags", Map.of("nome", "Participação", "ativo", true));
        LocalDateTime agora = LocalDateTime.now(clock);
        bimestreId = cadastrar("/bimestres", bimestre(1, agora.minusDays(1), agora.plusDays(1)));
        professor = login("professor@teste.local"); responsavel = login("responsavel@teste.local");
    }

    @Test void fluxoCompletoAteCienciaPdfRelatorioECsv() throws Exception {
        resposta(get("/turmas"), null, professor, 200);
        resposta(get("/disciplinas"), null, professor, 200);
        resposta(get("/alunos"), null, professor, 200);
        long id = criar();
        assertEquals("RASCUNHO", resposta(get("/acompanhamentos/" + id), null, professor, 200).get("status").asText());
        resposta(put("/acompanhamentos/" + id), edicao("Bom progresso em Português.", 9), professor, 200);
        assertEquals(0, resposta(get("/relatorios"), null, responsavel, 200).get("totalElements").asInt());
        publicar(id);
        JsonNode relatorio = resposta(get("/relatorios").param("anoLetivo", "2026").param("bimestre", "1").param("alunoId", "" + alunoId).param("disciplinaId", "" + disciplinaId), null, responsavel, 200);
        assertEquals(1, relatorio.get("totalElements").asInt());
        resposta(post("/acompanhamentos/" + id + "/ciencia"), Map.of("observacao", "Li o acompanhamento."), responsavel, 201);
        byte[] pdf = mvc.perform(get("/acompanhamentos/" + id + "/pdf").header("Authorization", responsavel))
            .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF)).andReturn().getResponse().getContentAsByteArray();
        try (var documento = Loader.loadPDF(pdf)) {
            String texto = new PDFTextStripper().getText(documento);
            for (String esperado : List.of("João da Silva", "5º A", "2026", "Português", "Professora Ana", "Bom progresso", "Participação")) assertTrue(texto.contains(esperado), esperado);
        }
        String csv = mvc.perform(get("/relatorios/exportar/csv").param("turmaId", "" + turmaId).header("Authorization", admin))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFFid;")); assertTrue(csv.contains("João da Silva"));
        JsonNode historico = resposta(get("/acompanhamentos/" + id + "/historico"), null, admin, 200);
        assertEquals(5, historico.size());
        assertEquals("PUBLICADO", historico.get(4).get("statusNovo").asText());
        assertFalse(historico.toString().contains("senha"));
    }

    @Test void recuperacaoDeSenhaUsaCodigoTemporarioEUnico() throws Exception {
        JsonNode pedido = resposta(post("/usuarios/recuperacao-senha"),
            Map.of("email", "professor@teste.local"), null, 200);
        assertTrue(pedido.hasNonNull("codigo"));
        String codigo = pedido.get("codigo").asText();
        resposta(post("/usuarios/redefinir-senha"),
            Map.of("codigo", codigo, "novaSenha", "NovaSenhaTeste123!"), null, 204);
        resposta(post("/usuarios/logar"),
            Map.of("email", "professor@teste.local", "senha", "NovaSenhaTeste123!"), null, 200);
        resposta(post("/usuarios/redefinir-senha"),
            Map.of("codigo", codigo, "novaSenha", "OutraSenhaTeste123!"), null, 400);
        JsonNode inexistente = resposta(post("/usuarios/recuperacao-senha"),
            Map.of("email", "inexistente@teste.local"), null, 200);
        assertTrue(inexistente.get("codigo").isNull());
    }

    @Test void cadastroLoginHashEEmailUnico() throws Exception {
        JsonNode u = resposta(get("/usuarios/" + usuarioProfessorId), null, admin, 200);
        assertFalse(u.has("senha")); assertTrue(usuarioRepository.findById(usuarioProfessorId).orElseThrow().getSenha().startsWith("$2"));
        JsonNode login = resposta(post("/usuarios/logar"), Map.of("email", "PROFESSOR@TESTE.LOCAL", "senha", "SenhaTeste123!"), null, 200);
        assertEquals("PROFESSOR", login.get("perfil").asText()); assertFalse(login.has("senha")); assertTrue(login.has("token"));
        resposta(post("/usuarios"), usuario("Duplicado", "PROFESSOR@TESTE.LOCAL", "PROFESSOR"), admin, 409);
        resposta(post("/usuarios/logar"), Map.of("email", "professor@teste.local", "senha", "senhaerrada"), null, 401);
        resposta(post("/usuarios/cadastrar"), usuario("Admin indevido", "novo@teste.local", "ADMINISTRADOR"), null, 401);
    }

    @Test void rotaProtegidaJwtInvalidoExpiradoEUsuarioInativo() throws Exception {
        resposta(get("/alunos"), null, null, 401);
        resposta(get("/alunos"), null, "Bearer token-invalido", 401);
        String segredo = "chave-exclusiva-de-testes-nao-usar-em-producao-123456789";
        String expirado = io.jsonwebtoken.Jwts.builder().issuer("kfka").subject("professor@teste.local")
            .issuedAt(Date.from(Instant.now().minusSeconds(120))).expiration(Date.from(Instant.now().minusSeconds(60)))
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8))).compact();
        resposta(get("/alunos"), null, "Bearer " + expirado, 401);
        resposta(delete("/usuarios/" + usuarioProfessorId), null, admin, 204);
        resposta(get("/alunos"), null, professor, 401);
        resposta(post("/usuarios/logar"), Map.of("email", "professor@teste.local", "senha", "SenhaTeste123!"), null, 401);
    }

    @Test void professorSemVinculoOuComVinculoInativoNaoCria() throws Exception {
        long outraTurma = cadastrar("/turmas", Map.of("nome", "Outra", "serie", "5º", "anoLetivo", 2026, "ativo", true));
        cadastrar("/matriculas", Map.of("alunoId", alunoId, "turmaId", outraTurma, "ativo", true));
        resposta(post("/acompanhamentos"), acompanhamento(alunoId, outraTurma, disciplinaId, bimestreId), professor, 403);
        resposta(delete("/vinculos/" + vinculoId), null, admin, 204);
        resposta(post("/acompanhamentos"), acompanhamento(alunoId, turmaId, disciplinaId, bimestreId), professor, 403);
    }

    @Test void professorNaoVeTurmaDisciplinaAlunoOuAcompanhamentoDeOutro() throws Exception {
        long u = cadastrar("/usuarios", usuario("Outro professor", "outro.prof@teste.local", "PROFESSOR"));
        long p = cadastrar("/professores", Map.of("usuarioId", u));
        long turma = cadastrar("/turmas", Map.of("nome", "6º B", "serie", "6º", "anoLetivo", 2026, "ativo", true));
        long aluno = cadastrar("/alunos", Map.of("nome", "Outro aluno", "ativo", true));
        long disciplina = cadastrar("/disciplinas", Map.of("nome", "Inglês", "areaDisciplinaId", areaId, "ativo", true));
        cadastrar("/matriculas", Map.of("alunoId", aluno, "turmaId", turma, "ativo", true));
        cadastrar("/vinculos", Map.of("professorId", p, "disciplinaId", disciplina, "turmaId", turma, "ativo", true));
        String outroToken = login("outro.prof@teste.local");
        long id = resposta(post("/acompanhamentos"), acompanhamento(aluno, turma, disciplina, bimestreId), outroToken, 201).get("id").asLong();
        for (String rota : List.of("/turmas/" + turma, "/disciplinas/" + disciplina, "/alunos/" + aluno, "/acompanhamentos/" + id)) resposta(get(rota), null, professor, 404);
        resposta(put("/acompanhamentos/" + id), edicao("Tentativa", 7), professor, 403);
        assertEquals(1, resposta(get("/turmas"), null, professor, 200).get("totalElements").asInt());
        assertEquals(0, resposta(get("/relatorios").param("professorId", "" + p), null, professor, 200).get("totalElements").asInt());
    }

    @Test void periodoFechadoOuFuturoImpedeCriacaoEdicaoEEnvio() throws Exception {
        LocalDateTime agora = LocalDateTime.now(clock);
        long fechado = cadastrar("/bimestres", bimestre(2, agora.minusDays(2), agora.minusDays(1)));
        long futuro = cadastrar("/bimestres", bimestre(3, agora.plusDays(1), agora.plusDays(2)));
        for (long b : List.of(fechado, futuro)) resposta(post("/acompanhamentos"), acompanhamento(alunoId, turmaId, disciplinaId, b), professor, 409);
        long id = criar();
        resposta(put("/bimestres/" + bimestreId), bimestre(1, agora.minusDays(2), agora.minusDays(1)), admin, 200);
        resposta(put("/acompanhamentos/" + id), edicao("Fora do prazo", 8), professor, 409);
        resposta(patch("/acompanhamentos/" + id + "/enviar"), null, professor, 409);
    }

    @Test void matriculaMediaEAnoLetivoSaoValidados() throws Exception {
        long semMatricula = cadastrar("/alunos", Map.of("nome", "Sem matrícula", "ativo", true));
        resposta(post("/acompanhamentos"), acompanhamento(semMatricula, turmaId, disciplinaId, bimestreId), professor, 400);
        var dados = new HashMap<>(acompanhamento(alunoId, turmaId, disciplinaId, bimestreId)); dados.put("media", 10.01);
        resposta(post("/acompanhamentos"), dados, professor, 400);
        dados.put("media", -1); resposta(post("/acompanhamentos"), dados, professor, 400);
        dados.put("media", 8.555); resposta(post("/acompanhamentos"), dados, professor, 400);
        long outroAno = cadastrar("/bimestres", Map.of("numero", 1, "anoLetivo", 2025, "dataHoraAbertura", LocalDateTime.now(clock).minusDays(1).toString(), "dataHoraEncerramento", LocalDateTime.now(clock).plusDays(1).toString()));
        resposta(post("/acompanhamentos"), acompanhamento(alunoId, turmaId, disciplinaId, outroAno), professor, 400);
    }

    @Test void naoPermiteDuplicadosNemBimestreInvalido() throws Exception {
        criar(); resposta(post("/acompanhamentos"), acompanhamento(alunoId, turmaId, disciplinaId, bimestreId), professor, 409);
        resposta(post("/vinculos"), Map.of("professorId", professorId, "disciplinaId", disciplinaId, "turmaId", turmaId, "ativo", true), admin, 409);
        resposta(post("/matriculas"), Map.of("alunoId", alunoId, "turmaId", turmaId, "ativo", true), admin, 409);
        LocalDateTime agora = LocalDateTime.now(clock);
        resposta(post("/bimestres"), bimestre(1, agora.minusDays(1), agora.plusDays(1)), admin, 409);
        resposta(post("/bimestres"), bimestre(5, agora.minusDays(1), agora.plusDays(1)), admin, 400);
        resposta(post("/bimestres"), bimestre(2, agora, agora.minusDays(1)), admin, 400);
    }

    @Test void transicoesInvalidasEEdicaoPublicadoSaoBloqueadas() throws Exception {
        long id = criar();
        resposta(patch("/acompanhamentos/" + id + "/publicar"), null, admin, 409);
        resposta(patch("/acompanhamentos/" + id + "/iniciar-revisao"), null, admin, 409);
        resposta(patch("/acompanhamentos/" + id + "/publicar"), null, professor, 403);
        publicar(id);
        resposta(put("/acompanhamentos/" + id), edicao("Alteração proibida", 1), professor, 409);
        resposta(put("/acompanhamentos/" + id + "/revisao"), edicao("Alteração proibida", 1), admin, 409);
        resposta(patch("/acompanhamentos/" + id + "/cancelar"), null, admin, 409);
    }

    @Test void devolucaoReenvioEdicaoAdministrativaEAuditoria() throws Exception {
        long id = criar();
        resposta(patch("/acompanhamentos/" + id + "/enviar"), null, professor, 200);
        resposta(put("/acompanhamentos/" + id), edicao("Não editável", 8), professor, 409);
        resposta(patch("/acompanhamentos/" + id + "/iniciar-revisao"), null, admin, 200);
        resposta(put("/acompanhamentos/" + id + "/revisao"), edicao("Texto revisado", 9), admin, 200);
        resposta(patch("/acompanhamentos/" + id + "/devolver"), null, admin, 200);
        resposta(put("/acompanhamentos/" + id), edicao("Texto ajustado", 9), professor, 200);
        resposta(patch("/acompanhamentos/" + id + "/enviar"), null, professor, 200);
        resposta(patch("/acompanhamentos/" + id + "/iniciar-revisao"), null, admin, 200);
        resposta(patch("/acompanhamentos/" + id + "/publicar"), null, admin, 200);
        JsonNode historico = resposta(get("/acompanhamentos/" + id + "/historico"), null, admin, 200);
        assertEquals(9, historico.size());
        assertEquals("EDICAO_ADMINISTRADOR", historico.get(3).get("acao").asText());
        assertTrue(historico.get(3).get("dadosAnteriores").asText().contains("Evolução"));
        assertTrue(historico.get(3).get("dadosNovos").asText().contains("Texto revisado"));
        resposta(get("/acompanhamentos/" + id + "/historico"), null, responsavel, 403);
    }

    @Test void estadosNaoPublicadosNaoSaoExpostosAoResponsavel() throws Exception {
        long id = criar();
        for (String acao : List.of("rascunho", "enviar", "iniciar-revisao", "devolver", "cancelar")) {
            if (!acao.equals("rascunho")) resposta(patch("/acompanhamentos/" + id + "/" + acao), null, acao.equals("enviar") ? professor : admin, 200);
            resposta(get("/acompanhamentos/" + id), null, responsavel, 404);
            resposta(get("/acompanhamentos/" + id + "/pdf"), null, responsavel, 404);
            resposta(post("/acompanhamentos/" + id + "/ciencia"), null, responsavel, 404);
            assertEquals(0, resposta(get("/relatorios"), null, responsavel, 200).get("totalElements").asInt());
            assertEquals(0, resposta(get("/acompanhamentos"), null, responsavel, 200).get("totalElements").asInt());
        }
    }

    @Test void outroResponsavelNaoAcessaPublicacaoPdfOuCienciaECienciaNaoDuplica() throws Exception {
        long id = criar(); publicar(id);
        long u = cadastrar("/usuarios", usuario("Outra família", "outra.familia@teste.local", "RESPONSAVEL"));
        cadastrar("/responsaveis", Map.of("usuarioId", u, "alunosIds", List.of()));
        String outro = login("outra.familia@teste.local");
        resposta(get("/acompanhamentos/" + id), null, outro, 404);
        resposta(get("/acompanhamentos/" + id + "/pdf"), null, outro, 404);
        resposta(post("/acompanhamentos/" + id + "/ciencia"), null, outro, 404);
        resposta(post("/acompanhamentos/" + id + "/ciencia"), null, responsavel, 201);
        resposta(post("/acompanhamentos/" + id + "/ciencia"), null, responsavel, 409);
        resposta(get("/relatorios/exportar/csv"), null, responsavel, 403);
        resposta(get("/acompanhamentos/" + id + "/pdf"), null, professor, 403);
    }

    @Test void doisResponsaveisPodemRegistrarCienciaDoMesmoAluno() throws Exception {
        long u = cadastrar("/usuarios", usuario("Segunda família", "segunda@teste.local", "RESPONSAVEL"));
        cadastrar("/responsaveis", Map.of("usuarioId", u, "alunosIds", List.of(alunoId)));
        long id = criar(); publicar(id);
        resposta(post("/acompanhamentos/" + id + "/ciencia"), null, responsavel, 201);
        resposta(post("/acompanhamentos/" + id + "/ciencia"), null, login("segunda@teste.local"), 201);
    }

    @Test void paginacaoFiltrosCsvEscapadoEPdfComVariasPaginas() throws Exception {
        long id = criar();
        String descricao = "=HYPERLINK(\"exemplo\"); teste\n" + "Observação pedagógica com acentuação e texto longo. ".repeat(150) + "fim-do-relatorio";
        resposta(put("/acompanhamentos/" + id), edicao(descricao, 8), professor, 200); publicar(id);
        assertEquals(1, resposta(get("/relatorios").param("tagId", "" + tagId).param("size", "1"), null, admin, 200).get("totalElements").asInt());
        assertEquals(0, resposta(get("/relatorios").param("bimestre", "2"), null, admin, 200).get("totalElements").asInt());
        byte[] pdf = mvc.perform(get("/acompanhamentos/" + id + "/pdf").header("Authorization", admin)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try (var doc = Loader.loadPDF(pdf)) { assertTrue(doc.getNumberOfPages() > 1); assertTrue(new PDFTextStripper().getText(doc).contains("fim-do-relatorio")); }
        Files.createDirectories(Path.of("target/verificacao")); Files.write(Path.of("target/verificacao/acompanhamento.pdf"), pdf);
        String csv = mvc.perform(get("/relatorios/exportar/csv").header("Authorization", admin)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"exemplo\"\")"));
    }

    @Test void errosConsistentesSwaggerPublicoECorsRestrito() throws Exception {
        JsonNode erro = resposta(get("/alunos"), null, null, 401);
        for (String campo : List.of("timestamp", "status", "erro", "mensagem", "path")) assertTrue(erro.has(campo));
        JsonNode invalidacao = resposta(post("/alunos"), Map.of("nome", ""), admin, 400);
        assertEquals(400, invalidacao.get("status").asInt()); assertFalse(invalidacao.has("trace"));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(options("/alunos").header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "GET").header("Access-Control-Request-Headers", "Authorization"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/alunos").header("Origin", "https://origem-nao-autorizada.example").header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
    }
}
