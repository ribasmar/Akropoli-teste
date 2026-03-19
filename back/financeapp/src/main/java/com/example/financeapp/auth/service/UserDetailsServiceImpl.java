package com.example.financeapp.auth.service;

import com.example.financeapp.auth.repository.BankerRepository;
import com.example.financeapp.config.encryption.DeterministicHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**

 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final BankerRepository repository;
    private final DeterministicHashService hashService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username = email em plaintext — calcular hash para busca no MongoDB
        String emailHash = hashService.hashEmail(username);
        return repository.findByEmailHash(emailHash)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Banker not found: " + username));
    }
}