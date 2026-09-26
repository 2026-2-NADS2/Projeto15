package br.com.kfka.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.http.HttpStatus;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsServiceImpl usuarios;
    private final ErroSeguranca erro;
    public JwtAuthFilter(JwtService jwt, UserDetailsServiceImpl usuarios, ErroSeguranca erro) {
        this.jwt = jwt; this.usuarios = usuarios; this.erro = erro;
    }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String rota = request.getServletPath();
        return rota.equals("/usuarios/logar") || rota.equals("/status") || rota.startsWith("/swagger-ui") || rota.startsWith("/v3/api-docs");
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null) {
            try {
                if (!cabecalho.startsWith("Bearer ")) throw new IllegalArgumentException();
                String token = cabecalho.substring(7);
                UserDetailsImpl usuario = usuarios.loadUserByUsername(jwt.extractUsername(token));
                if (!jwt.validateToken(token, usuario.getUsuario())) throw new IllegalArgumentException();
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
                erro.responder(request, response, HttpStatus.UNAUTHORIZED, "Token inválido ou expirado");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
