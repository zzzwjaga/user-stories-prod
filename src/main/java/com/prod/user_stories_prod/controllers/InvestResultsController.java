package com.prod.user_stories_prod.controllers;

import com.prod.user_stories_prod.entities.InvestResults;
import com.prod.user_stories_prod.responses.PageResponce;
import com.prod.user_stories_prod.services.InvestResultsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/invest")
public class InvestResultsController {

    private final InvestResultsService investResultsService;

    public InvestResultsController(InvestResultsService investResultsService) {
        this.investResultsService = investResultsService;
    }

    @PostMapping("/story/{story_id}")
    @PreAuthorize("@storySecurityService.canEdit(authentication.name, #story_id)")
    public ResponseEntity<InvestResults> checkInvestResults(@PathVariable UUID story_id){
        InvestResults results = investResultsService.checkInvestResults(story_id);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/story/{story_id}/latest")
    @PreAuthorize("@storySecurityService.canView(authentication.name, #story_id)")
    public ResponseEntity<Optional<InvestResults>> getLatestInvestResults(@PathVariable UUID story_id){
        Optional<InvestResults> results = investResultsService.findLastInvestResultsByStoryId(story_id);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/story/{story_id}")
    @PreAuthorize("@storySecurityService.canView(authentication.name, #story_id)")
    public ResponseEntity<PageResponce<InvestResults>> getInvestResults(@PathVariable UUID story_id,
                                                                        @RequestParam(defaultValue = "0") Integer page,
                                                                        @RequestParam(defaultValue = "10") Integer size){
        PageResponce<InvestResults> results = investResultsService.findInvestResultsByStoryId(story_id,page,size);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/story/{story_id}")
    @PreAuthorize("@storySecurityService.canEdit(authentication.name, #story_id)")
    public ResponseEntity<InvestResults> deleteInvestResults(@PathVariable UUID story_id){
        investResultsService.DeleteInvestResultsByStoryId(story_id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/story/{story_id}/{result_id}")
    @PreAuthorize("@storySecurityService.canEdit(authentication.name, #story_id)")
    public ResponseEntity<InvestResults> deleteOneInvestResultsById(@PathVariable UUID story_id,
                                                                    @PathVariable UUID result_id,
                                                                    @RequestParam Timestamp checked_at){
        investResultsService.deleteOneInvestResultsByStoryId(story_id,checked_at);
        return ResponseEntity.noContent().build();
    }
}
