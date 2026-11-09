package br.com.kfka.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Permite que frontend e API fiquem no mesmo endereço (deploy único).
 * - Requisições em /api/... são repassadas para a API sem o prefixo /api.
 * - Navegação do navegador (Accept text/html) em rotas do React recebe o index.html
 *   empacotado em static/, quando o frontend foi incluído no build.
 */
public class FrontendFilter extends OncePerRequestFilter {
    private static final String PREFIXO_API = "/api";
    private final byte[] indexHtml;

    public FrontendFilter() {
        this.indexHtml = carregarIndex();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        if (caminho.equals(PREFIXO_API) || caminho.startsWith(PREFIXO_API + "/")) {
            String semPrefixo = caminho.substring(PREFIXO_API.length());
            chain.doFilter(new RequisicaoSemPrefixo(request, semPrefixo.isEmpty() ? "/" : semPrefixo), response);
            return;
        }
        if (indexHtml != null && ehNavegacaoDoFrontend(request, caminho)) {
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.getOutputStream().write(indexHtml);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean ehNavegacaoDoFrontend(HttpServletRequest request, String caminho) {
        String aceita = request.getHeader("Accept");
        return "GET".equals(request.getMethod())
            && aceita != null && aceita.contains("text/html")
            && !caminho.substring(caminho.lastIndexOf('/') + 1).contains(".")
            && !caminho.startsWith("/swagger-ui") && !caminho.startsWith("/v3/api-docs")
            && !caminho.equals("/status") && !caminho.equals("/error");
    }

    private static byte[] carregarIndex() {
        ClassPathResource index = new ClassPathResource("static/index.html");
        if (!index.exists()) return null;
        try (InputStream entrada = index.getInputStream()) {
            return entrada.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    private static class RequisicaoSemPrefixo extends HttpServletRequestWrapper {
        private final String caminho;

        RequisicaoSemPrefixo(HttpServletRequest request, String caminho) {
            super(request);
            this.caminho = caminho;
        }

        @Override public String getRequestURI() { return getContextPath() + caminho; }
        @Override public String getServletPath() { return caminho; }
        @Override public String getPathInfo() { return null; }
        @Override public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer(getScheme()).append("://").append(getServerName());
            int porta = getServerPort();
            if (porta != 80 && porta != 443) url.append(':').append(porta);
            return url.append(getRequestURI());
        }
    }
}
