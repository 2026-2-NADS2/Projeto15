package br.com.kfka.security;

import br.com.kfka.model.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private final SecretKey chave;
    private final Duration expiracao;

    public JwtService(@Value("${kfka.jwt.secret}") String segredo,
                      @Value("${kfka.jwt.expiracao-minutos}") long minutos) {
        if (segredo.getBytes(StandardCharsets.UTF_8).length < 32 || minutos < 1)
            throw new IllegalArgumentException("JWT_SECRET precisa ter pelo menos 32 bytes e expiração positiva");
        chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        expiracao = Duration.ofMinutes(minutos);
    }

    public String generateToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder().issuer("kfka").subject(usuario.getEmail())
            .claim("id", usuario.getId()).claim("perfil", usuario.getPerfil().name())
            .issuedAt(Date.from(agora)).expiration(Date.from(agora.plus(expiracao)))
            .signWith(chave).compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(chave).requireIssuer("kfka").build()
            .parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) { return extractAllClaims(token).getSubject(); }

    public boolean validateToken(String token, Usuario usuario) {
        Claims dados = extractAllClaims(token);
        Number id = dados.get("id", Number.class);
        return usuario.getAtivo() && usuario.getEmail().equals(dados.getSubject())
            && id != null && usuario.getId().equals(id.longValue())
            && usuario.getPerfil().name().equals(dados.get("perfil", String.class));
    }
}
