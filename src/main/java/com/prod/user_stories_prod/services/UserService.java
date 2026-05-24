package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.User;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.UserRepository;
import com.prod.user_stories_prod.requests.CreateUserRequest;
import com.prod.user_stories_prod.requests.UpdateUserRequest;
import com.prod.user_stories_prod.responses.ErrorCode;
import com.prod.user_stories_prod.responses.PageResponce;
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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(CreateUserRequest request)
    {
        userRepository.lockOnValue(request.username());
        userRepository.lockOnValue(request.email());

        Optional<User> maybeUser = userRepository.findByUsername(request.username());
        if(maybeUser.isPresent())
        {
            throw new ValidationException(USER_ALREADY_EXISTS);
        }
        maybeUser =  userRepository.findByEmail(request.email());
        if(maybeUser.isPresent())
        {
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
           throw new ValidationException(USER_NOT_FOUND);
        }
        return user.get();
    }

    @Transactional
    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty())
        {
           throw new ValidationException(USER_NOT_FOUND);
        }
        return user.get();
    }

    @Transactional
    public User findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty())
        {
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
        return new PageResponce<>(users, page, pageSize, total, totalPages);
    }

    @Transactional
    public User update(UUID id, UpdateUserRequest request)
    {
        Optional<User> existingUser = userRepository.findById(id);
        if(existingUser.isEmpty())
        {
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
            throw new ValidationException("User could not be updated");
        }
        return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID id)
    {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty())
        {
            throw new ValidationException(USER_NOT_FOUND);
        }
        if(!userRepository.deleteUser(id))
        {
            throw new ValidationException("User could not be deleted");
        }
    }




}
