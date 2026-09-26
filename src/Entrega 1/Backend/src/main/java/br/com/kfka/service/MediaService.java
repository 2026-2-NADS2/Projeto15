package br.com.kfka.service;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MediaService {
    private final BigDecimal minima;
    private final BigDecimal maxima;
    public MediaService(@Value("${kfka.media.minima}") BigDecimal minima, @Value("${kfka.media.maxima}") BigDecimal maxima) {
        if (minima.compareTo(maxima) >= 0 || maxima.precision() - maxima.scale() > 5 || minima.precision() - minima.scale() > 5)
            throw new IllegalArgumentException("Escala de média inválida");
        this.minima = minima; this.maxima = maxima;
    }
    public void validar(BigDecimal media) {
        if (media == null || media.compareTo(minima) < 0 || media.compareTo(maxima) > 0 || media.scale() > 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Média deve estar entre " + minima + " e " + maxima + ", com até duas casas decimais");
    }
}
