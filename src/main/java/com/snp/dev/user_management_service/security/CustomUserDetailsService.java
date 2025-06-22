package com.snp.dev.user_management_service.security;

import com.snp.dev.user_management_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRoles().stream()
                                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role) // Remove "ROLE_" prefix if it exists
                                .toArray(String[]::new))
                        .accountExpired(!user.isAccountNonExpired())
                        .accountLocked(!user.isAccountNonLocked())
                        .credentialsExpired(!user.isCredentialsNonExpired())
                        .disabled(!user.isEnabled())
                        .build());

    }
}
