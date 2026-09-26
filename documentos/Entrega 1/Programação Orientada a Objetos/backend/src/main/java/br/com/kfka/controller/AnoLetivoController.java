package br.com.kfka.controller;

import br.com.kfka.model.AnoLetivo;
import br.com.kfka.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name="Ano letivo")
@RestController
@RequestMapping("/anos-letivos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AnoLetivoController {
    private final CadastroService service;
    public AnoLetivoController(CadastroService service) { this.service = service; }
    @GetMapping public List<AnoLetivo> listar() { return service.listarAnosLetivos(); }
    @GetMapping("/{ano}") public AnoLetivo buscar(@PathVariable Integer ano) { return service.buscarAnoLetivo(ano); }
    @PostMapping public ResponseEntity<AnoLetivo> criar(@Valid @RequestBody AnoLetivo dados) {
        AnoLetivo criado = service.criarAnoLetivo(dados.ano());
        return ResponseEntity.created(URI.create("/anos-letivos/" + criado.ano())).body(criado);
    }
}
