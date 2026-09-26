package br.com.kfka.controller;

import br.com.kfka.model.*;
import br.com.kfka.service.AcompanhamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Acompanhamento")
@RestController
@RequestMapping("/acompanhamentos")
public class AcompanhamentoController {
    private final AcompanhamentoService service;
    public AcompanhamentoController(AcompanhamentoService service) { this.service = service; }
    @GetMapping public ResponseEntity<Page<AcompanhamentoResposta>> listar(Pageable pageable) { return ResponseEntity.ok(service.listar(pageable)); }
    @GetMapping("/{id}") public ResponseEntity<AcompanhamentoResposta> buscar(@PathVariable Long id) { return ResponseEntity.ok(AcompanhamentoResposta.de(service.buscarPermitido(id))); }
    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<AcompanhamentoResposta> criar(@Valid @RequestBody AcompanhamentoCadastro dados) {
        var criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/acompanhamentos/" + criado.id())).body(criado);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<AcompanhamentoResposta> editar(@PathVariable Long id, @Valid @RequestBody AcompanhamentoEdicao dados) { return ResponseEntity.ok(service.editar(id, dados)); }
    @PatchMapping("/{id}/enviar")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<AcompanhamentoResposta> enviar(@PathVariable Long id) { return ResponseEntity.ok(service.enviar(id)); }
    @PatchMapping("/{id}/iniciar-revisao")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AcompanhamentoResposta> iniciarRevisao(@PathVariable Long id) { return ResponseEntity.ok(service.revisar(id, StatusAcompanhamento.EM_REVISAO)); }
    @PatchMapping("/{id}/devolver")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AcompanhamentoResposta> devolver(@PathVariable Long id) { return ResponseEntity.ok(service.revisar(id, StatusAcompanhamento.DEVOLVIDO_AJUSTES)); }
    @PatchMapping("/{id}/publicar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AcompanhamentoResposta> publicar(@PathVariable Long id) { return ResponseEntity.ok(service.revisar(id, StatusAcompanhamento.PUBLICADO)); }
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AcompanhamentoResposta> cancelar(@PathVariable Long id) { return ResponseEntity.ok(service.revisar(id, StatusAcompanhamento.CANCELADO)); }

    @PutMapping("/{id}/revisao")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AcompanhamentoResposta> editarRevisao(@PathVariable Long id, @Valid @RequestBody AcompanhamentoEdicao dados) {
        return ResponseEntity.ok(service.editarRevisao(id, dados));
    }
    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'PROFESSOR')")
    public ResponseEntity<java.util.List<HistoricoResposta>> historico(@PathVariable Long id) { return ResponseEntity.ok(service.historico(id)); }
}
