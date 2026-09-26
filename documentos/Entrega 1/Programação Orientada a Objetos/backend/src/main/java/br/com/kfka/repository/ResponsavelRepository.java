package br.com.kfka.repository;

import br.com.kfka.model.Responsavel;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ResponsavelRepository extends JpaRepository<Responsavel, Long>, JpaSpecificationExecutor<Responsavel> {
    Optional<Responsavel> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByUsuarioIdAndIdNot(Long usuarioId, Long id);
    boolean existsByUsuarioIdAndAlunosId(Long usuarioId, Long alunoId);
}
