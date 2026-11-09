package br.com.kfka.controller;

import br.com.kfka.model.VinculoTurma;
import br.com.kfka.model.DadosCadastro.DadosVinculo;
import br.com.kfka.service.RelacionamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="VinculoTurma")
@RestController
@RequestMapping("/vinculos")
public class VinculoTurmaController {
    private final RelacionamentoService service;
    public VinculoTurmaController(RelacionamentoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<VinculoTurma>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarVinculoTurma(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<VinculoTurma> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarVinculoTurma(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VinculoTurma> cadastrar(@Valid @RequestBody DadosVinculo dados) {
        VinculoTurma criado = service.salvarVinculoTurma(null, dados);
        return ResponseEntity.created(URI.create("/vinculos/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<VinculoTurma> atualizar(@PathVariable Long id, @Valid @RequestBody DadosVinculo dados) { return ResponseEntity.ok(service.salvarVinculoTurma(id, dados)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarVinculoTurma(id); return ResponseEntity.noContent().build(); }
}
