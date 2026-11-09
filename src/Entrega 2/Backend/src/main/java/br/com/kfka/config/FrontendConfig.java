package br.com.kfka.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;

@Configuration
public class FrontendConfig {
    @Bean public FilterRegistrationBean<FrontendFilter> frontendFilter() {
        FilterRegistrationBean<FrontendFilter> registro = new FilterRegistrationBean<>(new FrontendFilter());
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registro.addUrlPatterns("/*");
        return registro;
    }
}
