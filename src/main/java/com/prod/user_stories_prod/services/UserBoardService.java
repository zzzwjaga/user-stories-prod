package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.Board;
import com.prod.user_stories_prod.entities.Role;
import com.prod.user_stories_prod.entities.UserBoard;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.BoardRepository;
import com.prod.user_stories_prod.repositories.UserBoardRepository;
import com.prod.user_stories_prod.repositories.UserRepository;
import com.prod.user_stories_prod.responses.BoardRoleResponce;
import com.prod.user_stories_prod.responses.ErrorCode;
import com.prod.user_stories_prod.responses.PageResponce;
import com.prod.user_stories_prod.responses.UserRoleResponce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserBoardService {

    private final UserBoardRepository userBoardRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private static final Logger log =
            LoggerFactory.getLogger(UserBoardService.class);


    public UserBoardService(UserBoardRepository userBoardRepository, BoardRepository boardRepository, UserRepository userRepository) {
        this.userBoardRepository = userBoardRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserBoard addUserBoard(UUID board_id, UUID user_id, Role role)
    {
        if(userRepository.findById(user_id).isEmpty())
        {
            log.warn("User not found userId={}", user_id);
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        if(boardRepository.findById(board_id).isEmpty())
        {
            log.warn("Board not found boardId={}", board_id);
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        UserBoard userBoard = new UserBoard(user_id, board_id, role);

        if(!userBoardRepository.createUserRecord(userBoard))
        {
            log.error("Failed to add user to board boardId={} userId={}",
                    board_id, user_id);
            throw new ValidationException("Record could not be created");
        }
        log.info("User added to board boardId={} userId={} role={}",
                board_id, user_id, role);
        return userBoard;
    }

    @Transactional
    public PageResponce<UserBoard> findByUserId(UUID user_id, int page, int pageSize)
    {
        if(userRepository.findById(user_id).isEmpty())
        {
            log.warn("User not found userId={}", user_id);
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }

        List<UserBoard> links = userBoardRepository.findAllByUserId(user_id, page, pageSize);
        long total = userBoardRepository.countAllByUserId(user_id);
        int totalPages = (int) Math.ceil((double)total/pageSize);
        log.info("Found user-board links userId={} count={}",
                user_id, links.size());
        return new PageResponce<>(
                links,
                page,
                pageSize,
                total,
                totalPages
        );
    }

    @Transactional
    public PageResponce<UserBoard> findByBoardId(UUID board_id, int page, int pageSize)
    {
        if(boardRepository.findById(board_id).isEmpty())
        {
            log.warn("Board not found boardId={}", board_id);
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        List<UserBoard> links = userBoardRepository.findAllByBoardId(board_id,page, pageSize);
        long total = userBoardRepository.countAllByBoardId(board_id);
        int totalPages = (int) Math.ceil((double)total/pageSize);
        log.info("Found board-user links boardId={} count={}",
                board_id, links.size());
        return new PageResponce<>(
                links,
                page,
                pageSize,
                total,
                totalPages
        );

    }

    @Transactional
    public boolean deleteByUserId(UUID user_id)
    {
        if(userRepository.findById(user_id).isEmpty())
        {
            log.warn("User not found userId={}", user_id);
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        if(!userBoardRepository.deleteByUserId(user_id))
        {
            log.error("Failed to delete user-board links userId={}", user_id);
            throw new ValidationException("Record could not be deleted");
        }
        return true;
    }

    @Transactional
    public boolean deleteByBoardId(UUID board_id)
    {
        if(boardRepository.findById(board_id).isEmpty()){
            log.warn("Board not found boardId={}", board_id);
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }
        if(!userBoardRepository.deleteByBoardId(board_id))
        {
            log.error("Failed to delete board links boardId={}", board_id);
            throw new ValidationException("Record could not be deleted");
        }
        return true;
    }

    @Transactional
    public boolean deleteByKey(UUID board_id, UUID user_id){
        if(userRepository.findById(user_id).isEmpty())
        {
            log.warn("User not found userId={}", user_id);
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        if(boardRepository.findById(board_id).isEmpty())
        {
            log.warn("Board not found boardId={}", board_id);
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        if(!userBoardRepository.deleteByKey(board_id, user_id))
            {
                log.error("Failed to remove user from board boardId={} userId={}",
                        board_id, user_id);
                throw new ValidationException("Record could not be deleted");
            }
        return true;
    }

    @Transactional
    public boolean updateRole(UUID board_id, UUID user_id, Role role)
    {
        if(userRepository.findById(user_id).isEmpty())
        {
            log.warn("User not found userId={}", user_id);
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        if(boardRepository.findById(board_id).isEmpty())
        {
            log.warn("Board not found boardId={}", board_id);
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        if(!userBoardRepository.updateRole(new UserBoard(board_id, user_id, role))){
            log.error("Failed to update user role boardId={} userId={}",
                    board_id, user_id);
            throw new ValidationException("Record could not be updated");
        }
        return true;
    }
}
