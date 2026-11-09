package br.com.kfka.controller;

import br.com.kfka.model.Bimestre;
import br.com.kfka.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Bimestre")
@RestController
@RequestMapping("/bimestres")
public class BimestreController {
    private final CadastroService service;
    public BimestreController(CadastroService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<Bimestre>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarBimestre(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<Bimestre> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarBimestre(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Bimestre> cadastrar(@Valid @RequestBody Bimestre dados) {
        Bimestre criado = service.salvarBimestre(null, dados);
        return ResponseEntity.created(URI.create("/bimestres/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Bimestre> atualizar(@PathVariable Long id, @Valid @RequestBody Bimestre dados) {
        return ResponseEntity.ok(service.salvarBimestre(id, dados));
    }
}
