package br.com.kfka.repository;

import br.com.kfka.model.AreaDisciplina;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface AreaDisciplinaRepository extends JpaRepository<AreaDisciplina, Long> {
    
}
