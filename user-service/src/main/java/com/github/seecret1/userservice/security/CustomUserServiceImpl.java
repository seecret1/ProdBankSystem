package com.github.seecret1.userservice.security;

import com.github.seecret1.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        var user =  userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException(
                        "User not found by id: " + id
                )
        );

        return new CustomUserDetails(user);
    }
}
