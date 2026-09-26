package br.com.kfka.controller;

import br.com.kfka.model.Responsavel;
import br.com.kfka.model.DadosCadastro.DadosResponsavel;
import br.com.kfka.service.RelacionamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Responsavel")
@RestController
@RequestMapping("/responsaveis")
public class ResponsavelController {
    private final RelacionamentoService service;
    public ResponsavelController(RelacionamentoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<Responsavel>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarResponsavel(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<Responsavel> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarResponsavel(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Responsavel> cadastrar(@Valid @RequestBody DadosResponsavel dados) {
        Responsavel criado = service.salvarResponsavel(null, dados);
        return ResponseEntity.created(URI.create("/responsaveis/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Responsavel> atualizar(@PathVariable Long id, @Valid @RequestBody DadosResponsavel dados) { return ResponseEntity.ok(service.salvarResponsavel(id, dados)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarResponsavel(id); return ResponseEntity.noContent().build(); }
}
