package com.prod.user_stories_prod.controllers;

import com.prod.user_stories_prod.entities.Role;
import com.prod.user_stories_prod.entities.UserBoard;
import com.prod.user_stories_prod.responses.PageResponce;
import com.prod.user_stories_prod.services.UserBoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/boards/{board_id}/members")
public class UserBoardController {

    private final UserBoardService userBoardService;

    public UserBoardController(UserBoardService userBoardService) {
        this.userBoardService = userBoardService;
    }

    @PostMapping
    @PreAuthorize("@boardSecurityService.isOwner(authentication.name, #board_id)")
    public ResponseEntity<UserBoard> addUserBoard(@PathVariable UUID board_id,
                                                  @RequestParam UUID user_id,
                                                  @RequestParam Role role) {
        UserBoard userBoard = userBoardService.addUserBoard(board_id,user_id,role);
        return ResponseEntity.ok().body(userBoard);
    }

    @PutMapping
    @PreAuthorize("@boardSecurityService.isOwner(authentication.name, #board_id)")
    public ResponseEntity<Boolean> updateRole(@PathVariable UUID board_id,
                                              @RequestParam UUID user_id,
                                              @RequestParam Role role) {
        Boolean result = userBoardService.updateRole(board_id,user_id,role);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{user_id}")
    @PreAuthorize("@boardSecurityService.isOwner(authentication.name, #board_id)")
    public ResponseEntity<PageResponce<UserBoard>> getByUserId(@PathVariable UUID user_id,
                                                               @PathVariable UUID board_id,
                                                               @RequestParam(defaultValue = "0") Integer page,
                                                               @RequestParam(defaultValue = "10") Integer size){
        PageResponce<UserBoard> userBoards = userBoardService.findByUserId(user_id,page,size);
        return ResponseEntity.ok().body(userBoards);
    }

    @GetMapping
    @PreAuthorize("@boardSecurityService.isOwner(authentication.name, #board_id)")
    public ResponseEntity<PageResponce<UserBoard>> getByBoardId(@PathVariable UUID board_id,
                                                                @RequestParam(defaultValue = "10") Integer size,
                                                                @RequestParam(defaultValue = "0") Integer page){
        PageResponce<UserBoard> userBoards = userBoardService.findByBoardId(board_id,page,size);
        return ResponseEntity.ok().body(userBoards);
    }

    @DeleteMapping("/{user_id}")
    @PreAuthorize("@boardSecurityService.isOwner(authentication.name, #board_id)")
    public ResponseEntity<UserBoard> deleteByUserId(@PathVariable UUID user_id,
                                                    @PathVariable UUID board_id){
        userBoardService.deleteByUserId(user_id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @PreAuthorize("@boardSecurityService.isOwner(authentication.name, #board_id)")
    public ResponseEntity<UserBoard> deleteByBoardId(@PathVariable UUID board_id){
        userBoardService.deleteByBoardId(board_id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@boardSecurityService.isOwner(authentication.name, #board_id)")
    public ResponseEntity<UserBoard> deleteById(@PathVariable UUID id,
                                                @PathVariable UUID board_id){
        userBoardService.deleteByKey(board_id,id);
        return ResponseEntity.ok().build();
    }



    



}
