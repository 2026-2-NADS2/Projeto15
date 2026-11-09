package br.com.kfka.controller;

import br.com.kfka.model.AuditLog;
import br.com.kfka.service.AuditoriaService;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name="Auditoria")
@RestController
@RequestMapping("/auditoria")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaController {
    private final AuditoriaService service;
    public AuditoriaController(AuditoriaService service) { this.service = service; }
    @GetMapping public Page<AuditLog> listar(Pageable pageable) { return service.listar(pageable); }
}
