package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.security.UserDetailsImpl;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.criteria.*;

@Service
public class AcessoService {
    public Usuario usuarioAtual() {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UserDetailsImpl principal))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação necessária");
        return principal.getUsuario();
    }

    public void exigirPerfil(PerfilUsuario perfil) {
        if (usuarioAtual().getPerfil() != perfil) proibido();
    }

    public void proibido() { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não possui acesso a este recurso"); }

    private Predicate possuiTurma(Expression<Long> turmaId, CommonAbstractCriteria query, CriteriaBuilder cb, Long usuarioId) {
        Subquery<Long> sub = query.subquery(Long.class);
        Root<VinculoTurma> v = sub.from(VinculoTurma.class);
        sub.select(v.get("id")).where(cb.equal(v.get("turma").get("id"), turmaId),
            cb.equal(v.get("professor").get("usuario").get("id"), usuarioId), cb.isTrue(v.get("ativo")));
        return cb.exists(sub);
    }

    private Predicate possuiAluno(Expression<Long> alunoId, CommonAbstractCriteria query, CriteriaBuilder cb, Long usuarioId) {
        Subquery<Long> sub = query.subquery(Long.class);
        Root<Responsavel> r = sub.from(Responsavel.class);
        sub.select(r.get("id")).where(cb.equal(r.get("usuario").get("id"), usuarioId), cb.equal(r.join("alunos").get("id"), alunoId));
        return cb.exists(sub);
    }

    public Specification<Aluno> alunos() {
        Usuario u = usuarioAtual();
        return (root, query, cb) -> {
            if (u.getPerfil() == PerfilUsuario.ADMINISTRADOR) return cb.conjunction();
            if (u.getPerfil() == PerfilUsuario.RESPONSAVEL) return possuiAluno(root.get("id"), query, cb, u.getId());
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Matricula> m = sub.from(Matricula.class);
            sub.select(m.get("id")).where(cb.equal(m.get("aluno").get("id"), root.get("id")), cb.isTrue(m.get("ativo")),
                possuiTurma(m.get("turma").get("id"), sub, cb, u.getId()));
            return cb.exists(sub);
        };
    }

    public Specification<Turma> turmas() {
        Usuario u = usuarioAtual();
        return (root, query, cb) -> {
            if (u.getPerfil() == PerfilUsuario.ADMINISTRADOR) return cb.conjunction();
            if (u.getPerfil() == PerfilUsuario.PROFESSOR) return possuiTurma(root.get("id"), query, cb, u.getId());
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Matricula> m = sub.from(Matricula.class);
            sub.select(m.get("id")).where(cb.equal(m.get("turma").get("id"), root.get("id")), cb.isTrue(m.get("ativo")),
                possuiAluno(m.get("aluno").get("id"), sub, cb, u.getId()));
            return cb.exists(sub);
        };
    }

    public Specification<Disciplina> disciplinas() {
        Usuario u = usuarioAtual();
        return (root, query, cb) -> {
            if (u.getPerfil() == PerfilUsuario.ADMINISTRADOR) return cb.conjunction();
            if (u.getPerfil() != PerfilUsuario.PROFESSOR) return cb.disjunction();
            Subquery<Long> sub = query.subquery(Long.class);
            Root<VinculoTurma> v = sub.from(VinculoTurma.class);
            sub.select(v.get("id")).where(cb.equal(v.get("disciplina").get("id"), root.get("id")),
                cb.equal(v.get("professor").get("usuario").get("id"), u.getId()), cb.isTrue(v.get("ativo")));
            return cb.exists(sub);
        };
    }

    public Specification<Professor> professores() {
        Usuario u = usuarioAtual();
        return (root, query, cb) -> u.getPerfil() == PerfilUsuario.ADMINISTRADOR ? cb.conjunction()
            : cb.equal(root.get("usuario").get("id"), u.getId());
    }

    public Specification<Responsavel> responsaveis() {
        Usuario u = usuarioAtual();
        return (root, query, cb) -> u.getPerfil() == PerfilUsuario.ADMINISTRADOR ? cb.conjunction()
            : cb.equal(root.get("usuario").get("id"), u.getId());
    }

    public Specification<VinculoTurma> vinculos() {
        Usuario u = usuarioAtual();
        return (root, query, cb) -> u.getPerfil() == PerfilUsuario.ADMINISTRADOR ? cb.conjunction()
            : cb.and(cb.equal(root.get("professor").get("usuario").get("id"), u.getId()), cb.isTrue(root.get("ativo")));
    }

    public Specification<Matricula> matriculas() {
        Usuario u = usuarioAtual();
        return (root, query, cb) -> u.getPerfil() == PerfilUsuario.ADMINISTRADOR ? cb.conjunction()
            : cb.and(cb.isTrue(root.get("ativo")), possuiTurma(root.get("turma").get("id"), query, cb, u.getId()));
    }

    public Specification<Acompanhamento> acompanhamentos() {
        Usuario u = usuarioAtual();
        return (root, query, cb) -> {
            if (u.getPerfil() == PerfilUsuario.ADMINISTRADOR) return cb.conjunction();
            if (u.getPerfil() == PerfilUsuario.PROFESSOR) return cb.equal(root.get("professor").get("usuario").get("id"), u.getId());
            return cb.and(cb.equal(root.get("status"), StatusAcompanhamento.PUBLICADO), possuiAluno(root.get("aluno").get("id"), query, cb, u.getId()));
        };
    }
}
