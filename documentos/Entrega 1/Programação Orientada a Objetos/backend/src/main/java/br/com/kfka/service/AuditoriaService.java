package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.repository.AuditLogRepository;
import br.com.kfka.security.UserDetailsImpl;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.LocalDateTime;

@Service
public class AuditoriaService {
    private final AuditLogRepository repository;
    public AuditoriaService(AuditLogRepository repository) { this.repository = repository; }
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void registrar(UserDetailsImpl usuario, String metodo, String recurso, int status) {
        AuditLog log = new AuditLog();
        log.setDataHora(LocalDateTime.now()); log.setUsuarioId(usuario.getUsuario().getId());
        log.setUsuario(usuario.getUsuario().getNome()); log.setMetodo(metodo); log.setRecurso(recurso); log.setStatus(status);
        repository.save(log);
    }
    @Transactional(readOnly=true)
    public Page<AuditLog> listar(Pageable pageable) {
        return repository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dataHora", "id")));
    }
}
