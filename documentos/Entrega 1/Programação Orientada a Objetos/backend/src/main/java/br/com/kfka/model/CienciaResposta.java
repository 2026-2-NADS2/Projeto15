package br.com.kfka.model;

import java.time.LocalDateTime;

public record CienciaResposta(Long id, Long responsavelId, Long acompanhamentoId, LocalDateTime dataHoraCiencia, String observacao) {
    public static CienciaResposta de(CienciaResponsavel c) {
        return new CienciaResposta(c.getId(), c.getResponsavel().getId(), c.getAcompanhamento().getId(), c.getDataHoraCiencia(), c.getObservacao());
    }
}
