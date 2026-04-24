package com.prod.user_stories_prod.controllers;

import com.prod.user_stories_prod.entities.Story;
import com.prod.user_stories_prod.requests.CreateStoryRequest;
import com.prod.user_stories_prod.requests.UpdateStoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.prod.user_stories_prod.services.StoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @GetMapping("/boards/{board_id}/stories")
    public ResponseEntity<List<Story>> getAllStories(@PathVariable UUID board_id) {
        List<Story> stories = storyService.findAllByBoard(board_id);

        if (stories.isEmpty()) {
            return ResponseEntity.noContent().build();  // 204 No Content
        }

        return ResponseEntity.ok(stories);  // 200 OK
    }

    @GetMapping("/boards/{board_id}/stories/{number}")
    public ResponseEntity<Story>  getStoryByNumber(@PathVariable UUID board_id,@PathVariable String number)
    {
        Story story = storyService.findStoryByNumber(board_id, number);
        if(story == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(story);
    }

    @PostMapping("/boards/{board_id}/stories")
    public ResponseEntity<Story> createStory(
            @PathVariable UUID board_id,
            @RequestBody CreateStoryRequest request) {
        Story created = storyService.createStory(board_id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/stories/{story_id}")
    public ResponseEntity<Story> updateStory(
            @PathVariable UUID story_id,
            @RequestBody UpdateStoryRequest request
    ) {
        Story updated = storyService.updateStory(story_id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/stories/{story_id}")
    public ResponseEntity<Story> deleteStory(
            @PathVariable UUID story_id
    ){
       storyService.deleteStory(story_id);
       return ResponseEntity.noContent().build();
    }






}
