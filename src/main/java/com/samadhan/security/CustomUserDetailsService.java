package com.samadhan.security;

import com.samadhan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<com.samadhan.entity.UserDetails> userOpt = userRepository.findByUserContactNumber(username);

        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("User not found with contact number: " + username);
        }

        com.samadhan.entity.UserDetails user = userOpt.get();
        return buildUserDetails(user);
    }

    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        Optional<com.samadhan.entity.UserDetails> userOpt = userRepository.findById(userId);

        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("User not found with id: " + userId);
        }

        com.samadhan.entity.UserDetails user = userOpt.get();
        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(com.samadhan.entity.UserDetails user) {
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User account is disabled");
        }

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        String role = user.getUserRole() != null ? user.getUserRole() : "USER";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return User.builder()
                .username(user.getUserContactNumber())
                .password(user.getUserPassword() != null ? user.getUserPassword() : "")
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
}
