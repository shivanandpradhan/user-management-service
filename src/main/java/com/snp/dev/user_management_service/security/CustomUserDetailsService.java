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
//        // Create a dummy user //todo will remove below
//        UserDetails dummyUser = User.builder()
//                .username("dummy_user")
//                .password("{noop}password") // Use "{noop}" for plain text password
//                .roles("USER") // You can set roles like "USER", "ADMIN", etc.
//                .accountExpired(false)
//                .accountLocked(false)
//                .credentialsExpired(false)
//                .disabled(false)
//                .build();
//
//        // Return Mono.just(dummyUser)
//        return Mono.just(dummyUser);

    }
}
