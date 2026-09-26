package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class AcompanhamentoService {
    @Autowired private AcompanhamentoRepository repository;
    @Autowired private AlunoRepository alunos;
    @Autowired private TurmaRepository turmas;
    @Autowired private DisciplinaRepository disciplinas;
    @Autowired private ProfessorRepository professores;
    @Autowired private BimestreRepository bimestres;
    @Autowired private TagRepository tags;
    @Autowired private MatriculaRepository matriculas;
    @Autowired private VinculoTurmaRepository vinculos;
    @Autowired private AcessoService acesso;
    @Autowired private MediaService medias;
    @Autowired private Clock clock;
    @Autowired private HistoricoService historico;

    private ResponseStatusException erro(HttpStatus status, String mensagem) { return new ResponseStatusException(status, mensagem); }

    @Transactional(readOnly=true)
    public Page<AcompanhamentoResposta> listar(Pageable pageable) {
        return repository.findAll(acesso.acompanhamentos(), pageable).map(AcompanhamentoResposta::de);
    }

    @Transactional(readOnly=true)
    public Acompanhamento buscarPermitido(Long id) {
        return repository.findOne(acesso.acompanhamentos().and((root, query, cb) -> cb.equal(root.get("id"), id)))
            .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Acompanhamento não encontrado ou indisponível para este usuário"));
    }

    private Acompanhamento buscarParaAlterar(Long id) {
        Acompanhamento a = repository.buscarParaAlterar(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Acompanhamento não encontrado"));
        if (acesso.usuarioAtual().getPerfil() == PerfilUsuario.RESPONSAVEL) acesso.proibido();
        if (acesso.usuarioAtual().getPerfil() == PerfilUsuario.PROFESSOR && !a.getProfessor().getUsuario().getId().equals(acesso.usuarioAtual().getId())) acesso.proibido();
        return a;
    }

    private void validarPeriodo(Bimestre bimestre) {
        LocalDateTime agora = LocalDateTime.now(clock);
        if (agora.isBefore(bimestre.getDataHoraAbertura()) || !agora.isBefore(bimestre.getDataHoraEncerramento()))
            throw erro(HttpStatus.CONFLICT, "Período de digitação do bimestre está fechado");
    }

    private void validarVinculo(Acompanhamento a) {
        if (!vinculos.existsByProfessorUsuarioIdAndDisciplinaIdAndTurmaIdAndAtivoTrue(acesso.usuarioAtual().getId(), a.getDisciplina().getId(), a.getTurma().getId()))
            throw erro(HttpStatus.FORBIDDEN, "Professor não possui vínculo ativo com esta turma e disciplina");
        if (!matriculas.existsByAlunoIdAndTurmaIdAndAtivoTrue(a.getAluno().getId(), a.getTurma().getId()))
            throw erro(HttpStatus.BAD_REQUEST, "Aluno não possui matrícula ativa nesta turma");
        if (!a.getAluno().getAtivo() || !a.getTurma().getAtivo() || !a.getDisciplina().getAtivo() || !a.getDisciplina().getAreaDisciplina().getAtivo())
            throw erro(HttpStatus.CONFLICT, "Aluno, turma, disciplina e área devem estar ativos");
        if (!a.getTurma().getAnoLetivo().equals(a.getBimestre().getAnoLetivo()))
            throw erro(HttpStatus.BAD_REQUEST, "Turma e bimestre devem pertencer ao mesmo ano letivo");
    }

    private Set<Tag> buscarTags(Set<Long> ids) {
        Set<Tag> selecionadas = new HashSet<>();
        for (Long id : ids) {
            Tag tag = tags.findById(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Tag não encontrada"));
            if (!tag.getAtivo()) throw erro(HttpStatus.BAD_REQUEST, "Tag deve estar ativa");
            selecionadas.add(tag);
        }
        return selecionadas;
    }

    private void preencher(Acompanhamento a, String descricao, java.math.BigDecimal media, Set<Long> ids) {
        medias.validar(media);
        a.setDescricao(descricao);
        a.setMedia(media);
        a.setTags(buscarTags(ids));
        a.setDataAtualizacao(LocalDateTime.now(clock));
    }

    public AcompanhamentoResposta criar(AcompanhamentoCadastro dados) {
        acesso.exigirPerfil(PerfilUsuario.PROFESSOR);
        Acompanhamento a = new Acompanhamento();
        a.setProfessor(professores.findByUsuarioId(acesso.usuarioAtual().getId()).orElseThrow(() -> erro(HttpStatus.FORBIDDEN, "Cadastro de professor não encontrado")));
        a.setAluno(alunos.findById(dados.alunoId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Aluno não encontrado")));
        a.setTurma(turmas.findById(dados.turmaId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Turma não encontrada")));
        a.setDisciplina(disciplinas.findById(dados.disciplinaId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Disciplina não encontrada")));
        a.setBimestre(bimestres.findById(dados.bimestreId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Bimestre não encontrado")));
        validarVinculo(a);
        validarPeriodo(a.getBimestre());
        if (repository.existsByAlunoIdAndTurmaIdAndDisciplinaIdAndProfessorIdAndBimestreId(dados.alunoId(), dados.turmaId(), dados.disciplinaId(), a.getProfessor().getId(), dados.bimestreId()))
            throw erro(HttpStatus.CONFLICT, "Já existe acompanhamento para aluno, turma, disciplina, professor e bimestre");
        preencher(a, dados.descricao(), dados.media(), dados.tagsIds());
        a.setDataCriacao(LocalDateTime.now(clock));
        repository.saveAndFlush(a);
        historico.registrar(a, null, "CRIACAO", null);
        return AcompanhamentoResposta.de(a);
    }

    public AcompanhamentoResposta editar(Long id, AcompanhamentoEdicao dados) {
        acesso.exigirPerfil(PerfilUsuario.PROFESSOR);
        Acompanhamento a = buscarParaAlterar(id);
        if (a.getStatus() != StatusAcompanhamento.RASCUNHO && a.getStatus() != StatusAcompanhamento.DEVOLVIDO_AJUSTES)
            throw erro(HttpStatus.CONFLICT, "Somente rascunho ou acompanhamento devolvido pode ser editado pelo professor");
        validarPeriodo(a.getBimestre());
        validarVinculo(a);
        String antes = historico.retrato(a);
        preencher(a, dados.descricao(), dados.media(), dados.tagsIds());
        repository.saveAndFlush(a);
        historico.registrar(a, a.getStatus(), "EDICAO_PROFESSOR", antes);
        return AcompanhamentoResposta.de(a);
    }

    public AcompanhamentoResposta enviar(Long id) {
        acesso.exigirPerfil(PerfilUsuario.PROFESSOR);
        Acompanhamento a = buscarParaAlterar(id);
        validarPeriodo(a.getBimestre());
        validarVinculo(a);
        if (a.getStatus() != StatusAcompanhamento.RASCUNHO && a.getStatus() != StatusAcompanhamento.DEVOLVIDO_AJUSTES)
            throw erro(HttpStatus.CONFLICT, "Acompanhamento não pode ser enviado neste status");
        return mudarStatus(a, StatusAcompanhamento.ENVIADO_REVISAO);
    }

    public AcompanhamentoResposta revisar(Long id, StatusAcompanhamento destino) {
        acesso.exigirPerfil(PerfilUsuario.ADMINISTRADOR);
        Acompanhamento a = buscarParaAlterar(id);
        boolean permitido = switch (destino) {
            case EM_REVISAO -> a.getStatus() == StatusAcompanhamento.ENVIADO_REVISAO;
            case PUBLICADO, DEVOLVIDO_AJUSTES -> a.getStatus() == StatusAcompanhamento.EM_REVISAO;
            case CANCELADO -> a.getStatus() != StatusAcompanhamento.PUBLICADO && a.getStatus() != StatusAcompanhamento.CANCELADO;
            default -> false;
        };
        if (!permitido) throw erro(HttpStatus.CONFLICT, "Transição de " + a.getStatus() + " para " + destino + " não permitida");
        return mudarStatus(a, destino);
    }

    private AcompanhamentoResposta mudarStatus(Acompanhamento a, StatusAcompanhamento destino) {
        StatusAcompanhamento anterior = a.getStatus();
        String antes = historico.retrato(a);
        a.setStatus(destino);
        a.setDataAtualizacao(LocalDateTime.now(clock));
        repository.saveAndFlush(a);
        historico.registrar(a, anterior, "STATUS_" + destino.name(), antes);
        return AcompanhamentoResposta.de(a);
    }

    public AcompanhamentoResposta editarRevisao(Long id, AcompanhamentoEdicao dados) {
        acesso.exigirPerfil(PerfilUsuario.ADMINISTRADOR);
        Acompanhamento a = buscarParaAlterar(id);
        if (a.getStatus() != StatusAcompanhamento.EM_REVISAO)
            throw erro(HttpStatus.CONFLICT, "Administrador só pode editar informações durante a revisão");
        String antes = historico.retrato(a);
        preencher(a, dados.descricao(), dados.media(), dados.tagsIds());
        repository.saveAndFlush(a);
        historico.registrar(a, a.getStatus(), "EDICAO_ADMINISTRADOR", antes);
        return AcompanhamentoResposta.de(a);
    }

    @Transactional(readOnly=true)
    public List<HistoricoResposta> historico(Long id) {
        if (acesso.usuarioAtual().getPerfil() == PerfilUsuario.RESPONSAVEL) acesso.proibido();
        buscarPermitido(id);
        return historico.listar(id);
    }

    @Transactional(readOnly=true)
    public Page<AcompanhamentoResposta> listarPorAluno(Long alunoId, Pageable pageable) {
        if (alunos.findOne(acesso.alunos().and((root, query, cb) -> cb.equal(root.get("id"), alunoId))).isEmpty())
            throw erro(HttpStatus.NOT_FOUND, "Aluno não encontrado ou indisponível para este usuário");
        return repository.findAll(acesso.acompanhamentos().and((root, query, cb) -> cb.equal(root.get("aluno").get("id"), alunoId)), pageable)
            .map(AcompanhamentoResposta::de);
    }
}
