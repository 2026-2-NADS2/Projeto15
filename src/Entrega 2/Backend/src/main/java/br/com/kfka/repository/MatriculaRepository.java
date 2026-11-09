package br.com.kfka.repository;

import br.com.kfka.model.Matricula;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface MatriculaRepository extends JpaRepository<Matricula, Long>, JpaSpecificationExecutor<Matricula> {
    boolean existsByAlunoIdAndTurmaId(Long alunoId, Long turmaId);
    boolean existsByAlunoIdAndTurmaIdAndIdNot(Long alunoId, Long turmaId, Long id);
    boolean existsByAlunoIdAndTurmaIdAndAtivoTrue(Long alunoId, Long turmaId);
}
