package br.com.kfka.repository;
import br.com.kfka.model.*; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface RecuperacaoSenhaTokenRepository extends JpaRepository<RecuperacaoSenhaToken,Long>{Optional<RecuperacaoSenhaToken> findByTokenHash(String tokenHash);void deleteAllByUsuario(Usuario usuario);}
