package br.com.kfka.repository;

import br.com.kfka.model.CienciaResponsavel;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface CienciaResponsavelRepository extends JpaRepository<CienciaResponsavel, Long> {
    boolean existsByResponsavelIdAndAcompanhamentoId(Long responsavelId, Long acompanhamentoId);
}
