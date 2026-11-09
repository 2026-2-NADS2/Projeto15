package br.com.kfka.repository;

import br.com.kfka.model.Bimestre;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface BimestreRepository extends JpaRepository<Bimestre, Long> {
    boolean existsByAnoLetivoAndNumero(Integer anoLetivo, Integer numero);
    boolean existsByAnoLetivoAndNumeroAndIdNot(Integer anoLetivo, Integer numero, Long id);
    long countByAnoLetivo(Integer anoLetivo);
    java.util.List<Bimestre> findAllByAnoLetivoOrderByNumero(Integer anoLetivo);
    @Query("select distinct b.anoLetivo from Bimestre b order by b.anoLetivo desc")
    java.util.List<Integer> listarAnos();
}
