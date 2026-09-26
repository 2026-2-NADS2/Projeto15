package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.model.DadosCadastro.*;
import br.com.kfka.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.HashSet;

@Service
@Transactional
public class RelacionamentoService {
    @Autowired private AcessoService acesso;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private ResponsavelRepository responsavelRepository;
    @Autowired private AreaDisciplinaRepository areaDisciplinaRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private MatriculaRepository matriculaRepository;
    @Autowired private VinculoTurmaRepository vinculoTurmaRepository;

    private ResponseStatusException erro(HttpStatus status, String mensagem) { return new ResponseStatusException(status, mensagem); }

    private Usuario usuario(Long id, PerfilUsuario perfil) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        if (!usuario.getAtivo() || usuario.getPerfil() != perfil)
            throw erro(HttpStatus.BAD_REQUEST, "Usuário deve estar ativo e possuir perfil " + perfil);
        return usuario;
    }

    @Transactional(readOnly=true)
    public Page<Professor> listarProfessor(Pageable pageable) { return professorRepository.findAll(acesso.professores(), pageable); }

    @Transactional(readOnly=true)
    public Professor buscarProfessor(Long id) { return professorRepository.findOne(acesso.professores().and((root, query, cb) -> cb.equal(root.get("id"), id))).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Professor não encontrado")); }

    public Professor salvarProfessor(Long id, DadosProfessor dados) {
        Professor registro = id == null ? new Professor() : buscarProfessor(id);
        if (id != null && !registro.getUsuario().getId().equals(dados.usuarioId()))
            throw erro(HttpStatus.BAD_REQUEST, "Não é permitido trocar o usuário do professor");
        boolean duplicado = id == null ? professorRepository.existsByUsuarioId(dados.usuarioId()) : professorRepository.existsByUsuarioIdAndIdNot(dados.usuarioId(), id);
        if (duplicado) throw erro(HttpStatus.CONFLICT, "Usuário já possui cadastro de professor");
        registro.setUsuario(usuario(dados.usuarioId(), PerfilUsuario.PROFESSOR));
        return professorRepository.saveAndFlush(registro);
    }

    public void inativarProfessor(Long id) {
        Professor registro = buscarProfessor(id);
        registro.getUsuario().setAtivo(false);
        professorRepository.save(registro);
    }

    @Transactional(readOnly=true)
    public Page<Responsavel> listarResponsavel(Pageable pageable) { return responsavelRepository.findAll(acesso.responsaveis(), pageable); }

    @Transactional(readOnly=true)
    public Responsavel buscarResponsavel(Long id) { return responsavelRepository.findOne(acesso.responsaveis().and((root, query, cb) -> cb.equal(root.get("id"), id))).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Responsavel não encontrado")); }

    public Responsavel salvarResponsavel(Long id, DadosResponsavel dados) {
        Responsavel registro = id == null ? new Responsavel() : buscarResponsavel(id);
        if (id != null && !registro.getUsuario().getId().equals(dados.usuarioId()))
            throw erro(HttpStatus.BAD_REQUEST, "Não é permitido trocar o usuário do responsável");
        boolean duplicado = id == null ? responsavelRepository.existsByUsuarioId(dados.usuarioId()) : responsavelRepository.existsByUsuarioIdAndIdNot(dados.usuarioId(), id);
        if (duplicado) throw erro(HttpStatus.CONFLICT, "Usuário já possui cadastro de responsável");
        registro.setUsuario(usuario(dados.usuarioId(), PerfilUsuario.RESPONSAVEL));
        HashSet<Aluno> alunos = new HashSet<>();
        for (Long alunoId : dados.alunosIds()) {
            Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
            if (!aluno.getAtivo()) throw erro(HttpStatus.BAD_REQUEST, "Aluno deve estar ativo");
            alunos.add(aluno);
        }
        registro.setAlunos(alunos);
        return responsavelRepository.saveAndFlush(registro);
    }

    public void inativarResponsavel(Long id) {
        Responsavel registro = buscarResponsavel(id);
        registro.getUsuario().setAtivo(false);
        responsavelRepository.save(registro);
    }

    @Transactional(readOnly=true)
    public Page<Disciplina> listarDisciplina(Pageable pageable) { return disciplinaRepository.findAll(acesso.disciplinas(), pageable); }

    @Transactional(readOnly=true)
    public Disciplina buscarDisciplina(Long id) { return disciplinaRepository.findOne(acesso.disciplinas().and((root, query, cb) -> cb.equal(root.get("id"), id))).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Disciplina não encontrado")); }

    public Disciplina salvarDisciplina(Long id, DadosDisciplina dados) {
        Disciplina registro = id == null ? new Disciplina() : buscarDisciplina(id);
        AreaDisciplina area = areaDisciplinaRepository.findById(dados.areaDisciplinaId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Área não encontrada"));
        if (!area.getAtivo()) throw erro(HttpStatus.BAD_REQUEST, "Área deve estar ativa");
        registro.setNome(dados.nome());
        registro.setAreaDisciplina(area);
        registro.setAtivo(dados.ativo());
        return disciplinaRepository.saveAndFlush(registro);
    }

    public void inativarDisciplina(Long id) {
        Disciplina registro = buscarDisciplina(id);
        registro.setAtivo(false);
        disciplinaRepository.save(registro);
    }

    @Transactional(readOnly=true)
    public Page<Matricula> listarMatricula(Pageable pageable) { return matriculaRepository.findAll(acesso.matriculas(), pageable); }

    @Transactional(readOnly=true)
    public Matricula buscarMatricula(Long id) { return matriculaRepository.findOne(acesso.matriculas().and((root, query, cb) -> cb.equal(root.get("id"), id))).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Matricula não encontrado")); }

    public Matricula salvarMatricula(Long id, DadosMatricula dados) {
        Matricula registro = id == null ? new Matricula() : buscarMatricula(id);
        if (id != null && (!registro.getAluno().getId().equals(dados.alunoId()) || !registro.getTurma().getId().equals(dados.turmaId())))
            throw erro(HttpStatus.BAD_REQUEST, "Matrícula permite alterar apenas a situação ativa");
        Aluno aluno = alunoRepository.findById(dados.alunoId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
        Turma turma = turmaRepository.findById(dados.turmaId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Turma não encontrada"));
        if (dados.ativo() && (!aluno.getAtivo() || !turma.getAtivo())) throw erro(HttpStatus.BAD_REQUEST, "Aluno e turma devem estar ativos");
        boolean duplicado = id == null ? matriculaRepository.existsByAlunoIdAndTurmaId(dados.alunoId(), dados.turmaId()) : matriculaRepository.existsByAlunoIdAndTurmaIdAndIdNot(dados.alunoId(), dados.turmaId(), id);
        if (duplicado) throw erro(HttpStatus.CONFLICT, "Matrícula já cadastrada");
        registro.setAluno(aluno);
        registro.setTurma(turma);
        registro.setAtivo(dados.ativo());
        return matriculaRepository.saveAndFlush(registro);
    }

    public void inativarMatricula(Long id) {
        Matricula registro = buscarMatricula(id);
        registro.setAtivo(false);
        matriculaRepository.save(registro);
    }

    @Transactional(readOnly=true)
    public Page<VinculoTurma> listarVinculoTurma(Pageable pageable) { return vinculoTurmaRepository.findAll(acesso.vinculos(), pageable); }

    @Transactional(readOnly=true)
    public VinculoTurma buscarVinculoTurma(Long id) { return vinculoTurmaRepository.findOne(acesso.vinculos().and((root, query, cb) -> cb.equal(root.get("id"), id))).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "VinculoTurma não encontrado")); }

    public VinculoTurma salvarVinculoTurma(Long id, DadosVinculo dados) {
        VinculoTurma registro = id == null ? new VinculoTurma() : buscarVinculoTurma(id);
        if (id != null && (!registro.getProfessor().getId().equals(dados.professorId()) || !registro.getDisciplina().getId().equals(dados.disciplinaId()) || !registro.getTurma().getId().equals(dados.turmaId())))
            throw erro(HttpStatus.BAD_REQUEST, "Vínculo permite alterar apenas a situação ativa");
        Professor professor = professorRepository.findById(dados.professorId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Professor não encontrado"));
        Disciplina disciplina = disciplinaRepository.findById(dados.disciplinaId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
        Turma turma = turmaRepository.findById(dados.turmaId()).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Turma não encontrada"));
        if (dados.ativo() && (!professor.getUsuario().getAtivo() || !disciplina.getAtivo() || !turma.getAtivo())) throw erro(HttpStatus.BAD_REQUEST, "Professor, disciplina e turma devem estar ativos");
        boolean duplicado = id == null ? vinculoTurmaRepository.existsByProfessorIdAndDisciplinaIdAndTurmaId(dados.professorId(), dados.disciplinaId(), dados.turmaId()) : vinculoTurmaRepository.existsByProfessorIdAndDisciplinaIdAndTurmaIdAndIdNot(dados.professorId(), dados.disciplinaId(), dados.turmaId(), id);
        if (duplicado) throw erro(HttpStatus.CONFLICT, "Vínculo acadêmico já cadastrado");
        registro.setProfessor(professor);
        registro.setDisciplina(disciplina);
        registro.setTurma(turma);
        registro.setAtivo(dados.ativo());
        return vinculoTurmaRepository.saveAndFlush(registro);
    }

    public void inativarVinculoTurma(Long id) {
        VinculoTurma registro = buscarVinculoTurma(id);
        registro.setAtivo(false);
        vinculoTurmaRepository.save(registro);
    }
}
