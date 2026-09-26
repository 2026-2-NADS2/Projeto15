package br.com.kfka.repository;

import br.com.kfka.model.Usuario;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByPerfil(br.com.kfka.model.PerfilUsuario perfil);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    Page<Usuario> findAllByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
