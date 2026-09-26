package br.com.kfka.repository;

import br.com.kfka.model.Professor;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProfessorRepository extends JpaRepository<Professor, Long>, JpaSpecificationExecutor<Professor> {
    Optional<Professor> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByUsuarioIdAndIdNot(Long usuarioId, Long id);
}
