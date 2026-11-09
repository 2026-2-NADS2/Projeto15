package br.com.kfka.controller;

import br.com.kfka.model.*;
import br.com.kfka.service.CienciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Ciencia")
@RestController
@RequestMapping("/acompanhamentos")
public class CienciaController {
    private final CienciaService service;
    public CienciaController(CienciaService service) { this.service = service; }
    @PostMapping("/{id}/ciencia")
    @PreAuthorize("hasRole('RESPONSAVEL')")
    public ResponseEntity<CienciaResposta> registrar(@PathVariable Long id, @Valid @RequestBody(required=false) CienciaCadastro dados) {
        var ciencia = service.registrar(id, dados == null ? new CienciaCadastro(null) : dados);
        return ResponseEntity.created(URI.create("/acompanhamentos/" + id + "/ciencia")).body(ciencia);
    }
}
