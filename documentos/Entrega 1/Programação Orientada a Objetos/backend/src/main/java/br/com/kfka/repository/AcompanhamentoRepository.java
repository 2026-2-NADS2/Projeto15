package br.com.kfka.repository;

import br.com.kfka.model.Acompanhamento;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface AcompanhamentoRepository extends JpaRepository<Acompanhamento, Long>, JpaSpecificationExecutor<Acompanhamento> {
    boolean existsByAlunoIdAndTurmaIdAndDisciplinaIdAndProfessorIdAndBimestreId(Long alunoId, Long turmaId, Long disciplinaId, Long professorId, Long bimestreId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Acompanhamento a where a.id = :id")
    Optional<Acompanhamento> buscarParaAlterar(@Param("id") Long id);
}
