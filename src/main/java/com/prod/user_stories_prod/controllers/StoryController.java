package com.prod.user_stories_prod.controllers;

import com.prod.user_stories_prod.entities.Status;
import com.prod.user_stories_prod.entities.Story;
import com.prod.user_stories_prod.entities.StoryStatus;
import com.prod.user_stories_prod.requests.CreateStoryRequest;
import com.prod.user_stories_prod.requests.UpdateStoryRequest;
import com.prod.user_stories_prod.responses.PageResponce;
import com.prod.user_stories_prod.services.StoryTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.prod.user_stories_prod.services.StoryService;

import javax.naming.ldap.PagedResultsResponseControl;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class StoryController {

    private final StoryService storyService;
    private final StoryTemplateService storyTemplateService;

    public StoryController(StoryService storyService, StoryTemplateService storyTemplateService) {
        this.storyService = storyService;
        this.storyTemplateService = storyTemplateService;
    }

    @GetMapping("/stories/template")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getStoriesTemplate() {
        String template = storyTemplateService.getTemplate();
        return ResponseEntity.ok(template);
    }

    @GetMapping("/boards/{board_id}/stories")
    @PreAuthorize("@boardSecurityService.canView(authentication.name, #board_id)")
    public ResponseEntity<PageResponce<Story>> getAllStories(@PathVariable UUID board_id,
                                                             @RequestParam(defaultValue = "0") Integer page,
                                                             @RequestParam(defaultValue = "15") Integer size) {
        PageResponce<Story> stories = storyService.findAllByBoard(board_id, page, size);
        return ResponseEntity.ok(stories);

    }

    @GetMapping("/boards/{board_id}/stories/{number}")
    @PreAuthorize("@boardSecurityService.canView(authentication.name, #board_id)")
    public ResponseEntity<Story>  getStoryByNumber(@PathVariable UUID board_id,@PathVariable String number)
    {
       Optional<Story> story = storyService.findStoryByNumber(board_id, number);
       return ResponseEntity.ok(story.get());
    }

    @GetMapping("/stories/{story_id}")
    @PreAuthorize("@storySecurityService.canView(authentication.name, #story_id)")
    public ResponseEntity<Story> getStoryById(@PathVariable UUID story_id){
        Optional<Story> story = storyService.findStoryById(story_id);
        return ResponseEntity.ok(story.get());
    }

    @GetMapping("/stories/{story_id}/statuses")
    @PreAuthorize("@storySecurityService.canView(authentication.name, #story_id)")
     ResponseEntity<PageResponce<StoryStatus>> getAllStatuses(
            @PathVariable UUID story_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponce<StoryStatus> statuses = storyService.findAllStatuses(story_id, page, size);
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/stories/{story_id}/statuses/latest")
    @PreAuthorize("@storySecurityService.canView(authentication.name, #story_id)")
    public ResponseEntity<StoryStatus> getLatestStatus(@PathVariable UUID story_id) {
        StoryStatus latestStatus = storyService.findLatestStatus(story_id);
        return ResponseEntity.ok(latestStatus);
    }

    @PostMapping("/boards/{board_id}/stories")
    @PreAuthorize("@boardSecurityService.canEdit(authentication.name, #board_id)")
    public ResponseEntity<Story> createStory(
            @PathVariable UUID board_id,
            @RequestBody CreateStoryRequest request) {
        Story created = storyService.createStory(board_id, request);
        URI location = URI.create(String.format("/api/boards/%s/stories/%s",
                board_id, created.number()));
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(created);
    }

    @PutMapping("/stories/{story_id}")
    @PreAuthorize("@storySecurityService.canEdit(authentication.name, #story_id)")
    public ResponseEntity<Story> updateStory(
            @PathVariable UUID story_id,
            @RequestBody UpdateStoryRequest request
    ) {
        Story updated = storyService.updateStory(story_id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/stories/{story_id}/status")
    @PreAuthorize("@storySecurityService.canEdit(authentication.name, #story_id)")
    public ResponseEntity<Void> changeStatus(
            @PathVariable UUID story_id,
            @RequestParam String status) {

        storyService.changeStatus(story_id, Status.valueOf(status));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/stories/{story_id}")
    @PreAuthorize("@storySecurityService.canEdit(authentication.name, #story_id)")
    public ResponseEntity<Story> deleteStory(
            @PathVariable UUID story_id) {
       storyService.deleteStory(story_id);
       return ResponseEntity.noContent().build();
    }






}
