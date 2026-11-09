package br.com.kfka.model;

import java.time.LocalDateTime;

public record HistoricoResposta(Long id, Long usuarioId, String usuarioNome, LocalDateTime dataHora,
    StatusAcompanhamento statusAnterior, StatusAcompanhamento statusNovo, String acao,
    String dadosAnteriores, String dadosNovos) {
    public static HistoricoResposta de(HistoricoAcompanhamento h) {
        return new HistoricoResposta(h.getId(), h.getUsuarioResponsavelPelaAcao().getId(),
            h.getUsuarioResponsavelPelaAcao().getNome(), h.getDataHora(), h.getStatusAnterior(),
            h.getStatusNovo(), h.getAcao(), h.getDadosAnteriores(), h.getDadosNovos());
    }
}
