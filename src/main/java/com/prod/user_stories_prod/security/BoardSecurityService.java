package com.prod.user_stories_prod.security;

import com.prod.user_stories_prod.entities.Role;
import com.prod.user_stories_prod.entities.User;
import com.prod.user_stories_prod.entities.UserBoard;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.UserBoardRepository;
import com.prod.user_stories_prod.repositories.UserRepository;
import com.prod.user_stories_prod.responses.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BoardSecurityService {

    private final UserBoardRepository userBoardRepository;
    private final UserRepository userRepository;


    public BoardSecurityService(UserBoardRepository userBoardRepository, UserRepository userRepository) {
        this.userBoardRepository = userBoardRepository;
        this.userRepository = userRepository;
    }


    public boolean canView(String email, UUID board_id) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        return userBoardRepository.findByKey(board_id, user.get().id()).isPresent();
    }


    public boolean canEdit(String email, UUID board_id) {
        Role role = getRole(email, board_id);
        return role == Role.OWNER || role == Role.EDITOR;
    }


    public boolean isOwner(String email, UUID board_id) {
        return getRole(email, board_id) == Role.OWNER;
    }


    private Role getRole(String email, UUID board_id) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        return (Role) userBoardRepository
                .findByKey(board_id,user.get().id())
                .map(UserBoard::getRole)
                .orElse(null);
    }
}