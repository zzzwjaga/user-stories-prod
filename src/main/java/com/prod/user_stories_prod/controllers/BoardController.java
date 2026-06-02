
package com.prod.user_stories_prod.controllers;

import com.prod.user_stories_prod.entities.Board;
import com.prod.user_stories_prod.requests.CreateBoardRequest;
import com.prod.user_stories_prod.requests.UpdateBoardRequest;
import com.prod.user_stories_prod.responses.PageResponce;
import com.prod.user_stories_prod.security.BoardSecurityService;
import com.prod.user_stories_prod.services.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BoardController {

    private final BoardService boardService;
    private final BoardSecurityService boardSecurityService;

    public BoardController(BoardService boardService, BoardSecurityService boardSecurityService) {
        this.boardService = boardService;
        this.boardSecurityService = boardSecurityService;
    }

    @GetMapping("/boards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponce<Board>> getAllBoards(
                                                            Authentication authentication,
                                                            @RequestParam(defaultValue = "0") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer size){
        PageResponce<Board> boards = boardService.findAllBoards(authentication.getName(), page, size);
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/boards/{board_id}")
    @PreAuthorize("@boardSecurityService.canView(authentication.name, #board_id)")
    public ResponseEntity<Board> getBoardById(
                                                @PathVariable UUID board_id){
        Optional<Board> board = boardService.findBoardById(board_id);
        return ResponseEntity.ok(board.get());
    }

    @GetMapping("/owner/{owner_id}/boards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponce<Board>> getBoardsByOwner(@PathVariable UUID owner_id,
                                                                @RequestParam(defaultValue = "0") Integer page,
                                                                @RequestParam(defaultValue = "10") Integer size){
        PageResponce<Board> boards = boardService.findBoardsByOwner(owner_id, page, size);
        return ResponseEntity.ok(boards);
    }

    @PostMapping("/boards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Board> createBoard(@RequestBody CreateBoardRequest request){
        Board created = boardService.createBoard(request);
        URI location = URI.create(String.format("/api/boards/%s",
               created.id().toString()));
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(created);
    }

    @PutMapping("/boards/{board_id}")
    @PreAuthorize("@boardSecurityService.canEdit(authentication.name, #board_id)")
    public ResponseEntity<Board> updateBoard(@PathVariable UUID board_id, @RequestBody UpdateBoardRequest request){
        Board updated = boardService.updateBoard(board_id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/boards/{board_id}")
    @PreAuthorize("@boardSecurityService.isOwner(authentication.name, #board_id)")
    public ResponseEntity<Board> deleteBoard(@PathVariable UUID board_id){
        boardService.deleteBoard(board_id);
        return ResponseEntity.noContent().build();
    }




}
