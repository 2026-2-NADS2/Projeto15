package br.com.kfka.security;

import br.com.kfka.exception.ErroResposta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;

@Component
public class ErroSeguranca {
    private final ObjectMapper mapper;
    public ErroSeguranca(ObjectMapper mapper) { this.mapper = mapper; }
    public void responder(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (status == HttpStatus.UNAUTHORIZED) response.setHeader("WWW-Authenticate", "Bearer");
        mapper.writeValue(response.getOutputStream(), new ErroResposta(Instant.now(), status.value(), status.getReasonPhrase(), mensagem, request.getRequestURI()));
    }
}
