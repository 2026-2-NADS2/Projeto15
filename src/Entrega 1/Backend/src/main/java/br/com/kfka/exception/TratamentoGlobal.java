package br.com.kfka.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.dao.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.data.mapping.PropertyReferenceException;

@RestControllerAdvice
public class TratamentoGlobal {
    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem, HttpServletRequest request) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
            .body(new ErroResposta(Instant.now(), status.value(), status.getReasonPhrase(), mensagem, request.getRequestURI()));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResposta> negocio(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return resposta(status, ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(), req);
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErroResposta> validacao(BindException ex, HttpServletRequest req) {
        String mensagem = String.join("; ", ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage()).distinct().sorted().toList());
        return resposta(HttpStatus.BAD_REQUEST, mensagem.isBlank() ? "Dados inválidos" : mensagem, req);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResposta> restricao(ConstraintViolationException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, String.join("; ", ex.getConstraintViolations().stream().map(v -> v.getMessage()).distinct().sorted().toList()), req);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, PropertyReferenceException.class})
    public ResponseEntity<ErroResposta> entrada(Exception ex, HttpServletRequest req) { return resposta(HttpStatus.BAD_REQUEST, "Corpo, parâmetro ou ordenação inválidos", req); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> conflito(DataIntegrityViolationException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Registro duplicado ou vínculo existente impede esta operação", req);
    }
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ErroResposta> concorrencia(Exception ex, HttpServletRequest req) { return resposta(HttpStatus.CONFLICT, "Registro foi alterado por outra operação. Atualize e tente novamente", req); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResposta> proibido(AccessDeniedException ex, HttpServletRequest req) { return resposta(HttpStatus.FORBIDDEN, "Acesso proibido", req); }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResposta> autenticacao(AuthenticationException ex, HttpServletRequest req) { return resposta(HttpStatus.UNAUTHORIZED, "Autenticação necessária", req); }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> inexistente(NoResourceFoundException ex, HttpServletRequest req) { return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", req); }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> metodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) { return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido", req); }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResposta> formato(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) { return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Utilize o formato de conteúdo esperado", req); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> inesperado(Exception ex, HttpServletRequest req) {
        org.slf4j.LoggerFactory.getLogger(TratamentoGlobal.class).error("Falha interna: {}", ex.getClass().getSimpleName());
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível concluir a operação", req);
    }
}
