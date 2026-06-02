package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.User;
import com.prod.user_stories_prod.repositories.UserRepository;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.prod.user_stories_prod.responses.ErrorCode.USER_NOT_FOUND;

@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger log =
            LoggerFactory.getLogger(UserDetailService.class);

    public UserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found email={}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });
    }
}