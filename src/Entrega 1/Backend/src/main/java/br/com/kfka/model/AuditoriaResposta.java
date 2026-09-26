package br.com.kfka.model;

import java.time.LocalDateTime;

public record AuditoriaResposta(Long id, LocalDateTime dataHora, Long usuarioId,
    String usuario, String acao, Long acompanhamentoId) {
    public static AuditoriaResposta de(HistoricoAcompanhamento h) {
        return new AuditoriaResposta(h.getId(), h.getDataHora(),
            h.getUsuarioResponsavelPelaAcao().getId(), h.getUsuarioResponsavelPelaAcao().getNome(),
            h.getAcao(), h.getAcompanhamento().getId());
    }
}
