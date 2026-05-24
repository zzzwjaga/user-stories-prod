package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.Board;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.BoardRepository;
import com.prod.user_stories_prod.repositories.UserRepository;
import com.prod.user_stories_prod.requests.CreateBoardRequest;
import com.prod.user_stories_prod.requests.UpdateBoardRequest;
import com.prod.user_stories_prod.responses.ErrorCode;
import com.prod.user_stories_prod.responses.PageResponce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import static com.prod.user_stories_prod.responses.ErrorCode.BOARD_NOT_FOUND;
import static com.prod.user_stories_prod.responses.ErrorCode.USER_NOT_FOUND;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private static final Logger log =
            LoggerFactory.getLogger(BoardService.class);

    public BoardService(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Board createBoard(CreateBoardRequest request)
    {
        boardRepository.lockOnValue(request.boardname());
        Optional<Board> maybeBoard = boardRepository.findByName(request.owner_id(),  request.boardname());
        if(maybeBoard.isPresent())
        {
            log.warn("Board already exists name={} ownerId={}",
                    request.boardname(),
                    request.owner_id());
            throw new ValidationException(String.valueOf(ErrorCode.BOARD_ALREADY_EXISTS));
        }
        Board newBoard = new Board(
                UUID.randomUUID(),
                request.owner_id(),
                request.boardname(),
                request.description()
        );
        if(!boardRepository.createBoard(newBoard))
        {
            log.error("Failed to create board name={} ownerId={}",
                    request.boardname(),
                    request.owner_id());
            throw new ValidationException("Board could not be created");
        }
        log.info("Board created boardId={}", newBoard.id());
        return newBoard;
    }


    @Transactional
    public PageResponce<Board> findAllBoards(int page, int pageSize)
    {
        List<Board> boards = boardRepository.findAll(page, pageSize);
        long total = boardRepository.countAll();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        log.info("Fetched boards count={}", boards.size());
        return new PageResponce<>(boards, page, pageSize, total, totalPages);
    }

    @Transactional
    public Optional<Board> findBoardById(UUID id)
    {
        Optional<Board> maybeBoard = boardRepository.findById(id);
        if(maybeBoard.isEmpty())
        {
            log.warn("Board not found id={}", id);
            throw new ValidationException(BOARD_NOT_FOUND);
        }
        log.info("Board found id={}", id);
        return maybeBoard;
    }

    @Transactional
    public PageResponce<Board> findBoardsByOwner(UUID owner_id, int page, int pageSize)
    {
        if(userRepository.findById(owner_id).isEmpty()){
            log.warn("Owner not found id={}", owner_id);
            throw new ValidationException(USER_NOT_FOUND);
        }
        List<Board> boards = boardRepository.findByOwner(owner_id, page, pageSize);
        long total = boardRepository.countAllByOwner(owner_id);
        int totalPages = (int) Math.ceil((double) total / pageSize);
        log.info("Fetched boards for owner ownerId={} count={}",
                owner_id, boards.size());
        return new PageResponce<>(boards, page, pageSize, total, totalPages);
    }

    public Board updateBoard(UUID id, UpdateBoardRequest request)
    {
        Optional<Board> existingBoard = boardRepository.findById(id);
        if(existingBoard.isEmpty())
        {
            log.warn("Board not found for update id={}", id);
            throw new ValidationException(BOARD_NOT_FOUND);
        }
        Board updatedBoard = new Board(
                existingBoard.get().id(),
                existingBoard.get().owner_id(),
                request.boardname(),
                request.description()
        );
        if(!boardRepository.updateBoard(updatedBoard))
        {
            log.error("Failed to update board id={}", id);
            throw new ValidationException("Board could not be updated");
        }
        return updatedBoard;
    }

    public  void deleteBoard(UUID id)
    {
        Optional<Board> maybeBoard = boardRepository.findById(id);
        if(maybeBoard.isEmpty())
        {
            log.warn("Board not found for delete id={}", id);
            throw new ValidationException(BOARD_NOT_FOUND);
        }
        if(!boardRepository.deleteBoard(id))
        {
            log.error("Failed to delete board id={}", id);
            throw new ValidationException("Board could not be deleted");
        }
    }


}
