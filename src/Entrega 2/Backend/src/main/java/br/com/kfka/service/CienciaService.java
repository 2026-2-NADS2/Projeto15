package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.repository.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CienciaService {
    private final CienciaResponsavelRepository repository;
    private final ResponsavelRepository responsaveis;
    private final AcompanhamentoRepository acompanhamentos;
    private final AcompanhamentoService acompanhamentoService;
    private final AcessoService acesso;
    private final Clock clock;
    public CienciaService(CienciaResponsavelRepository repository, ResponsavelRepository responsaveis,
                         AcompanhamentoRepository acompanhamentos, AcompanhamentoService acompanhamentoService, AcessoService acesso, Clock clock) {
        this.repository = repository; this.responsaveis = responsaveis; this.acompanhamentos = acompanhamentos;
        this.acompanhamentoService = acompanhamentoService; this.acesso = acesso; this.clock = clock;
    }
    @Transactional
    public CienciaResposta registrar(Long id, CienciaCadastro dados) {
        acesso.exigirPerfil(PerfilUsuario.RESPONSAVEL);
        acompanhamentoService.buscarPermitido(id);
        Acompanhamento a = acompanhamentos.buscarParaAlterar(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acompanhamento não encontrado"));
        if (a.getStatus() != StatusAcompanhamento.PUBLICADO) acesso.proibido();
        Responsavel r = responsaveis.findByUsuarioId(acesso.usuarioAtual().getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Responsável não cadastrado"));
        if (!responsaveis.existsByUsuarioIdAndAlunosId(acesso.usuarioAtual().getId(), a.getAluno().getId())) acesso.proibido();
        if (repository.existsByResponsavelIdAndAcompanhamentoId(r.getId(), id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ciência já registrada para este acompanhamento");
        CienciaResponsavel c = new CienciaResponsavel();
        c.setResponsavel(r); c.setAcompanhamento(a); c.setDataHoraCiencia(LocalDateTime.now(clock)); c.setObservacao(dados.observacao());
        return CienciaResposta.de(repository.saveAndFlush(c));
    }
}
