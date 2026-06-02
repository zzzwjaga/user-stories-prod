package com.prod.user_stories_prod.security;

import com.prod.user_stories_prod.repositories.StoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StorySecurityService {

    private final StoryRepository storyRepository;
    private final BoardSecurityService boardSecurityService;

    public StorySecurityService(
            StoryRepository storyRepository,
            BoardSecurityService boardSecurityService
    ) {
        this.storyRepository = storyRepository;
        this.boardSecurityService = boardSecurityService;
    }

    public boolean canView(String email, UUID story_id) {

        UUID boardId = storyRepository.findById(story_id).get().board_id();

        return boardSecurityService.canView(email, boardId);
    }

    public boolean canEdit(String email, UUID story_id) {

        UUID boardId = storyRepository.findById(story_id).get().board_id();

        return boardSecurityService.canEdit(email, boardId);
    }
}