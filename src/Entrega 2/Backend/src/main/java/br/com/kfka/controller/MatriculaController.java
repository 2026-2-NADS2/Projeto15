package br.com.kfka.controller;

import br.com.kfka.model.Matricula;
import br.com.kfka.model.DadosCadastro.DadosMatricula;
import br.com.kfka.service.RelacionamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Matricula")
@RestController
@RequestMapping("/matriculas")
public class MatriculaController {
    private final RelacionamentoService service;
    public MatriculaController(RelacionamentoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<Matricula>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarMatricula(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<Matricula> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarMatricula(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Matricula> cadastrar(@Valid @RequestBody DadosMatricula dados) {
        Matricula criado = service.salvarMatricula(null, dados);
        return ResponseEntity.created(URI.create("/matriculas/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Matricula> atualizar(@PathVariable Long id, @Valid @RequestBody DadosMatricula dados) { return ResponseEntity.ok(service.salvarMatricula(id, dados)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarMatricula(id); return ResponseEntity.noContent().build(); }
}
