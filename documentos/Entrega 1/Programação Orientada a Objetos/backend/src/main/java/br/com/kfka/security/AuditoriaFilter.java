package br.com.kfka.security;

import br.com.kfka.service.AuditoriaService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

@Component
public class AuditoriaFilter extends OncePerRequestFilter {
    private static final Set<String> MUTACOES = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final AuditoriaService service;
    public AuditoriaFilter(AuditoriaService service) { this.service = service; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        chain.doFilter(req, res);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (MUTACOES.contains(req.getMethod()) && !req.getRequestURI().equals("/usuarios/logar") && auth != null && auth.getPrincipal() instanceof UserDetailsImpl usuario)
            service.registrar(usuario, req.getMethod(), req.getRequestURI(), res.getStatus());
    }
}
