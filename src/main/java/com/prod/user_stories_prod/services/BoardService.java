package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.Board;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.BoardRepository;
import com.prod.user_stories_prod.repositories.StoryRepository;
import com.prod.user_stories_prod.requests.CreateBoardRequest;
import com.prod.user_stories_prod.requests.UpdateBoardRequest;
import com.prod.user_stories_prod.responses.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.prod.user_stories_prod.responses.ErrorCode.BOARD_NOT_FOUND;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional
    public Board createBoard(CreateBoardRequest request)
    {
        boardRepository.lockOnValue(request.boardname());
        Optional<Board> maybeBoard = boardRepository.findByName(request.owner_id(),  request.boardname());
        if(maybeBoard.isPresent())
        {
            throw new ValidationException(String.valueOf(ErrorCode.BOARD_ALREADY_EXISTS));
        }
        Board newBoard = new Board(
                UUID.randomUUID(),
                request.owner_id(),
                request.boardname(),
                request.description()
        );
        if(!boardRepository.CreateBoard(newBoard))
        {
            throw new ValidationException("Board could not be created");
        }
        return newBoard;
    }

    @Transactional
    public List<Board> findAllBoards()
    {
        return boardRepository.findAll();
    }

    @Transactional
    public Board findBoardById(UUID id)
    {
        Optional<Board> maybeBoard = boardRepository.findById(id);
        if(maybeBoard.isEmpty())
        {
            throw new ValidationException(BOARD_NOT_FOUND);
        }
        return maybeBoard.get();
    }

    @Transactional
    public List<Board> findBoardsByOwner(UUID owner_id)
    {
        //добавить проверку существования овнера
        return boardRepository.findByOwner(owner_id);
    }

    public Board updateBoard(UUID id, UpdateBoardRequest request)
    {
        Optional<Board> existingBoard = boardRepository.findById(id);
        if(existingBoard.isEmpty())
        {
            throw new ValidationException(BOARD_NOT_FOUND);
        }
        Board updatedBoard = new Board(
                existingBoard.get().id(),
                existingBoard.get().owner_id(),
                request.boardname(),
                request.description()
        );
        if(!boardRepository.UpdateBoard(updatedBoard))
        {
            throw new ValidationException("Board could not be updated");
        }
        return updatedBoard;
    }

    public  void deleteBoard(UUID id)
    {
        Optional<Board> maybeBoard = boardRepository.findById(id);
        if(maybeBoard.isEmpty())
        {
            throw new ValidationException(BOARD_NOT_FOUND);
        }
        if(!boardRepository.DeleteBoard(id))
        {
            throw new ValidationException("Board could not be deleted");
        }
    }


}
