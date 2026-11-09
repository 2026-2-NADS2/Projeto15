package br.com.kfka.exception;

import java.time.Instant;

public record ErroResposta(Instant timestamp, int status, String erro, String mensagem, String path) {}
