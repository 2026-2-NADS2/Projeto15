package br.com.kfka.repository;

import br.com.kfka.model.VinculoTurma;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface VinculoTurmaRepository extends JpaRepository<VinculoTurma, Long>, JpaSpecificationExecutor<VinculoTurma> {
    boolean existsByProfessorIdAndDisciplinaIdAndTurmaId(Long professorId, Long disciplinaId, Long turmaId);
    boolean existsByProfessorIdAndDisciplinaIdAndTurmaIdAndIdNot(Long professorId, Long disciplinaId, Long turmaId, Long id);
    boolean existsByProfessorUsuarioIdAndDisciplinaIdAndTurmaIdAndAtivoTrue(Long usuarioId, Long disciplinaId, Long turmaId);
}
