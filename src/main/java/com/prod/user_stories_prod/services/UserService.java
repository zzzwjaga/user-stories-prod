package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.User;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.UserRepository;
import com.prod.user_stories_prod.requests.CreateUserRequest;
import com.prod.user_stories_prod.requests.UpdateUserRequest;
import com.prod.user_stories_prod.responses.PageResponce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.prod.user_stories_prod.responses.ErrorCode.USER_ALREADY_EXISTS;
import static com.prod.user_stories_prod.responses.ErrorCode.USER_NOT_FOUND;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(CreateUserRequest request)
    {
        userRepository.lockOnValue(request.username());
        userRepository.lockOnValue(request.email());
        log.debug("Lock acquired for username={} email={}",
                request.username(), request.email());
        Optional<User> maybeUser = userRepository.findByUsername(request.username());
        if(maybeUser.isPresent())
        {
            log.warn("Username already exists username={}", request.username());
            throw new ValidationException(USER_ALREADY_EXISTS);
        }
        maybeUser =  userRepository.findByEmail(request.email());
        if(maybeUser.isPresent())
        {
            log.warn("Email already exists email={}", request.email());
            throw new ValidationException(USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User newUser = new User(
                UUID.randomUUID(),
                request.username(),
                request.email(),
                encodedPassword
        );
        if(!userRepository.createUser(newUser))
        {
            log.error("Failed to create user username={}", request.username());
            throw new ValidationException("User could not be created");
        }
        return newUser;
    }

    @Transactional
    public User findById(UUID id)
    {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty())
        {
            log.warn("User not found id={}", id);
           throw new ValidationException(USER_NOT_FOUND);
        }
        log.info("User found id={}", id);
        return user.get();
    }

    @Transactional
    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty())
        {
            log.warn("User not found username={}", username);
           throw new ValidationException(USER_NOT_FOUND);
        }
        return user.get();
    }

    @Transactional
    public User findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty())
        {
            log.warn("User not found email={}", email);
           throw new ValidationException(USER_NOT_FOUND);
        }
        return user.get();
    }

    @Transactional
    public PageResponce<User> findAll(int page, int pageSize)
    {
        List<User> users = userRepository.findAll(page, pageSize);
        long total = userRepository.countAll();
        int totalPages = (int) Math.ceil(total/(double)pageSize);
        log.info("Fetched users count={}", users.size());
        return new PageResponce<>(users, page, pageSize, total, totalPages);
    }

    @Transactional
    public User update(UUID id, UpdateUserRequest request)
    {
        Optional<User> existingUser = userRepository.findById(id);
        if(existingUser.isEmpty())
        {
            log.warn("User not found id={}", id);
            throw new ValidationException(USER_NOT_FOUND);
        }
        User updatedUser = new User(
                existingUser.get().id(),
                request.username(),
                request.email(),
                existingUser.get().password_hash()
        );
        if(!userRepository.updateUser(updatedUser))
        {
            log.error("User don't updated id={} username={}",
                    id, request.username());
            throw new ValidationException("User could not be updated");
        }
        log.info("User updated id={} username={}",
                id, request.username());
        return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID id)
    {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty())
        {
            log.warn("User not found id={}", id);
            throw new ValidationException(USER_NOT_FOUND);
        }
        if(!userRepository.deleteUser(id))
        {
            log.error("Failed to delete user id={}", id);
            throw new ValidationException("User could not be deleted");
        }
    }




}
