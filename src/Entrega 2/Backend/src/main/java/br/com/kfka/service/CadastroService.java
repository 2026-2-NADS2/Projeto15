package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Service
@Transactional
public class CadastroService {
    @Autowired private AcessoService acesso;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private AreaDisciplinaRepository areaDisciplinaRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private BimestreRepository bimestreRepository;

    @Transactional(readOnly=true)
    public Page<Aluno> listarAluno(String nome, Pageable pageable) { return alunoRepository.findAll(acesso.alunos().and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase(java.util.Locale.ROOT) + "%")), pageable); }

    @Transactional(readOnly=true)
    public Aluno buscarAluno(Long id) {
        return alunoRepository.findOne(acesso.alunos().and((root, query, cb) -> cb.equal(root.get("id"), id))).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
    }

    public Aluno salvarAluno(Long id, Aluno dados) {
        Aluno registro = id == null ? new Aluno() : buscarAluno(id);
        registro.setNome(dados.getNome());
        registro.setAtivo(dados.getAtivo());
        return alunoRepository.saveAndFlush(registro);
    }

    public void inativarAluno(Long id) {
        Aluno registro = buscarAluno(id);
        registro.setAtivo(false);
        alunoRepository.save(registro);
    }

    @Transactional(readOnly=true)
    public Page<AreaDisciplina> listarAreaDisciplina(Pageable pageable) { return areaDisciplinaRepository.findAll(pageable); }

    @Transactional(readOnly=true)
    public AreaDisciplina buscarAreaDisciplina(Long id) {
        return areaDisciplinaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AreaDisciplina não encontrado"));
    }

    public AreaDisciplina salvarAreaDisciplina(Long id, AreaDisciplina dados) {
        AreaDisciplina registro = id == null ? new AreaDisciplina() : buscarAreaDisciplina(id);
        registro.setNome(dados.getNome());
        registro.setAtivo(dados.getAtivo());
        return areaDisciplinaRepository.saveAndFlush(registro);
    }

    public void inativarAreaDisciplina(Long id) {
        AreaDisciplina registro = buscarAreaDisciplina(id);
        registro.setAtivo(false);
        areaDisciplinaRepository.save(registro);
    }

    @Transactional(readOnly=true)
    public Page<Tag> listarTag(Pageable pageable) { return tagRepository.findAll(pageable); }

    @Transactional(readOnly=true)
    public Tag buscarTag(Long id) {
        return tagRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag não encontrado"));
    }

    public Tag salvarTag(Long id, Tag dados) {
        Tag registro = id == null ? new Tag() : buscarTag(id);
        registro.setNome(dados.getNome());
        registro.setAtivo(dados.getAtivo());
        registro.setDescricao(dados.getDescricao());
        return tagRepository.saveAndFlush(registro);
    }

    public void inativarTag(Long id) {
        Tag registro = buscarTag(id);
        registro.setAtivo(false);
        tagRepository.save(registro);
    }

    @Transactional(readOnly=true)
    public Page<Turma> listarTurma(Pageable pageable) { return turmaRepository.findAll(acesso.turmas(), pageable); }

    @Transactional(readOnly=true)
    public Turma buscarTurma(Long id) {
        return turmaRepository.findOne(acesso.turmas().and((root, query, cb) -> cb.equal(root.get("id"), id))).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrado"));
    }

    public Turma salvarTurma(Long id, Turma dados) {
        Turma registro = id == null ? new Turma() : buscarTurma(id);
        registro.setNome(dados.getNome());
        registro.setAtivo(dados.getAtivo());
        registro.setSerie(dados.getSerie());
        if (id != null && !registro.getAnoLetivo().equals(dados.getAnoLetivo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ano letivo não pode ser alterado. Cadastre um novo registro para outro ano");
        registro.setAnoLetivo(dados.getAnoLetivo());
        return turmaRepository.saveAndFlush(registro);
    }

    public void inativarTurma(Long id) {
        Turma registro = buscarTurma(id);
        registro.setAtivo(false);
        turmaRepository.save(registro);
    }

    @Transactional(readOnly=true)
    public Page<Bimestre> listarBimestre(Pageable pageable) { return bimestreRepository.findAll(pageable); }

    @Transactional(readOnly=true)
    public Bimestre buscarBimestre(Long id) {
        return bimestreRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bimestre não encontrado"));
    }

    public Bimestre salvarBimestre(Long id, Bimestre dados) {
        Bimestre registro = id == null ? new Bimestre() : buscarBimestre(id);
        if (!dados.getDataHoraEncerramento().isAfter(dados.getDataHoraAbertura()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encerramento deve ser posterior à abertura");
        boolean duplicado = id == null
            ? bimestreRepository.existsByAnoLetivoAndNumero(dados.getAnoLetivo(), dados.getNumero())
            : bimestreRepository.existsByAnoLetivoAndNumeroAndIdNot(dados.getAnoLetivo(), dados.getNumero(), id);
        if (duplicado) throw new ResponseStatusException(HttpStatus.CONFLICT, "Bimestre já cadastrado neste ano letivo");
        if (id != null && !registro.getNumero().equals(dados.getNumero()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Número do bimestre não pode ser alterado. Cadastre outro bimestre");
        registro.setNumero(dados.getNumero());
        if (id != null && !registro.getAnoLetivo().equals(dados.getAnoLetivo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ano letivo não pode ser alterado. Cadastre um novo registro para outro ano");
        registro.setAnoLetivo(dados.getAnoLetivo());
        registro.setDataHoraAbertura(dados.getDataHoraAbertura());
        registro.setDataHoraEncerramento(dados.getDataHoraEncerramento());
        return bimestreRepository.saveAndFlush(registro);
    }

    @Transactional(readOnly=true)
    public List<AnoLetivo> listarAnosLetivos() {
        acesso.exigirPerfil(PerfilUsuario.ADMINISTRADOR);
        return bimestreRepository.listarAnos().stream()
            .map(ano -> new AnoLetivo(ano, bimestreRepository.countByAnoLetivo(ano))).toList();
    }

    @Transactional(readOnly=true)
    public AnoLetivo buscarAnoLetivo(Integer ano) {
        acesso.exigirPerfil(PerfilUsuario.ADMINISTRADOR);
        long total = bimestreRepository.countByAnoLetivo(ano);
        if (total == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ano letivo não encontrado");
        return new AnoLetivo(ano, total);
    }

    public AnoLetivo criarAnoLetivo(Integer ano) {
        acesso.exigirPerfil(PerfilUsuario.ADMINISTRADOR);
        if (bimestreRepository.countByAnoLetivo(ano) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ano letivo já cadastrado");
        LocalDateTime inicio = LocalDateTime.of(ano, Month.JANUARY, 1, 0, 0);
        for (int numero = 1; numero <= 4; numero++) {
            Bimestre bimestre = new Bimestre();
            bimestre.setNumero(numero);
            bimestre.setAnoLetivo(ano);
            bimestre.setDataHoraAbertura(inicio.plusMonths((numero - 1L) * 3));
            bimestre.setDataHoraEncerramento(inicio.plusMonths(numero * 3L).minusMinutes(1));
            bimestreRepository.save(bimestre);
        }
        bimestreRepository.flush();
        return new AnoLetivo(ano, 4);
    }
}
