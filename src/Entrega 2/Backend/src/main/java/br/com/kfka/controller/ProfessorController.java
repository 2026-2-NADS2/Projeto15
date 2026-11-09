package br.com.kfka.controller;

import br.com.kfka.model.Professor;
import br.com.kfka.model.DadosCadastro.DadosProfessor;
import br.com.kfka.service.RelacionamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Professor")
@RestController
@RequestMapping("/professores")
public class ProfessorController {
    private final RelacionamentoService service;
    public ProfessorController(RelacionamentoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<Professor>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarProfessor(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarProfessor(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Professor> cadastrar(@Valid @RequestBody DadosProfessor dados) {
        Professor criado = service.salvarProfessor(null, dados);
        return ResponseEntity.created(URI.create("/professores/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Professor> atualizar(@PathVariable Long id, @Valid @RequestBody DadosProfessor dados) { return ResponseEntity.ok(service.salvarProfessor(id, dados)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarProfessor(id); return ResponseEntity.noContent().build(); }
}
