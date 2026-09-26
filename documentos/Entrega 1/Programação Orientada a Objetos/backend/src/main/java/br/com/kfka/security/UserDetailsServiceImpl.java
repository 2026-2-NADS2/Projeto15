package br.com.kfka.security;

import br.com.kfka.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UsuarioRepository repository;
    public UserDetailsServiceImpl(UsuarioRepository repository) { this.repository = repository; }
    @Override public UserDetailsImpl loadUserByUsername(String email) {
        return repository.findByEmail(email).map(UserDetailsImpl::new)
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }
}
