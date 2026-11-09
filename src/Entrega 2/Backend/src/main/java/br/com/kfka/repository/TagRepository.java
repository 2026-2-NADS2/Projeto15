package br.com.kfka.repository;

import br.com.kfka.model.Tag;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface TagRepository extends JpaRepository<Tag, Long> {
    
}
