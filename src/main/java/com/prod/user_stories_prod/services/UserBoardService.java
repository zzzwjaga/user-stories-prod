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
import com.prod.user_stories_prod.responses.UserRoleResponce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserBoardService {

    private final UserBoardRepository userBoardRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;


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
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        if(boardRepository.findById(board_id).isEmpty())
        {
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        UserBoard userBoard = new UserBoard(user_id, board_id, role);

        if(!userBoardRepository.createUserRecord(userBoard))
        {
            throw new ValidationException("Record could not be created");
        }
        return userBoard;
    }

    @Transactional
    public List<BoardRoleResponce> findByUserId(UUID user_id)
    {
        if(userRepository.findById(user_id).isEmpty())
        {
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }

        List<UserBoard> links = userBoardRepository.findAllByUserId(user_id);

        return links.stream()
                .map(link -> new BoardRoleResponce(
                        link.board_id(),
                        link.role()
                ))
                .toList();
    }

    @Transactional
    public List<UserRoleResponce> findByBoardId(UUID board_id)
    {
        if(boardRepository.findById(board_id).isEmpty())
        {
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        List<UserBoard> links = userBoardRepository.findAllByBoardId(board_id);

        return links.stream()
                .map(link -> new UserRoleResponce(
                        link.user_id(),
                        link.role()
                ))
                .toList();
    }

    @Transactional
    public boolean deleteByUserId(UUID user_id)
    {
        if(userRepository.findById(user_id).isEmpty())
        {
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        if(!userBoardRepository.deleteByUserId(user_id))
        {
            throw new ValidationException("Record could not be deleted");
        }
        return true;
    }

    @Transactional
    public boolean deleteByBoardId(UUID board_id)
    {
        if(boardRepository.findById(board_id).isEmpty()){
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }
        if(!userBoardRepository.deleteByBoardId(board_id))
        {
            throw new ValidationException("Record could not be deleted");
        }
        return true;
    }

    @Transactional
    public boolean deleteByKey(UUID board_id, UUID user_id){
        if(userRepository.findById(user_id).isEmpty())
        {
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        if(boardRepository.findById(board_id).isEmpty())
        {
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        if(!userBoardRepository.deleteByKey(board_id, user_id))
            {
            throw new ValidationException("Record could not be deleted");
            }
        return true;
    }

    @Transactional boolean updateRole(UUID board_id, UUID user_id, Role role)
    {
        if(userRepository.findById(user_id).isEmpty())
        {
            throw new ValidationException(ErrorCode.USER_NOT_FOUND);
        }
        if(boardRepository.findById(board_id).isEmpty())
        {
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }

        if(!userBoardRepository.updateRole(new UserBoard(board_id, user_id, role))){
            throw new ValidationException("Record could not be updated");
        }
        return true;
    }







}
