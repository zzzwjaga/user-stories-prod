package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.Story;
import com.prod.user_stories_prod.exseptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.prod.user_stories_prod.repositories.StoryRepository;
import com.prod.user_stories_prod.requests.CreateStoryRequest;
import com.prod.user_stories_prod.requests.UpdateStoryRequest;
import com.prod.user_stories_prod.responses.ErrorCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StoryService {

    private final StoryRepository storyRepository;

    public StoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    @Transactional
    public Story createStory(UUID board_id, CreateStoryRequest request) {
        storyRepository.lockOnValue(request.number());
        Optional<Story> maybeStory = storyRepository.findByNumber(board_id, request.number());
        if (maybeStory.isPresent()) {
            throw new ValidationException(String.valueOf(ErrorCode.STORY_ALREADY_EXISTS));
        }
        Story newStory = new Story(
                UUID.randomUUID(),
                request.number(),
                request.story_points(),
                request.story_text(),
                board_id,
                request.author_id()
        );
        if(!storyRepository.createStory(newStory))
        {
            throw new ValidationException("Story could not be created");
        }
        return newStory;
    }

    @Transactional
    public Story findStoryByNumber(UUID board_id, String number) {
        Optional<Story> maybeStory = storyRepository.findByNumber(board_id, number);
        if (maybeStory.isEmpty()) {throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));}
        return maybeStory.get();
    }

    @Transactional
    public List<Story> findAllByBoard (UUID board_id) {
        return storyRepository.findAllByBoard(board_id);
    }

    @Transactional
    public Story updateStory(UUID story_id, UpdateStoryRequest request) {
        Optional<Story> existingStory = storyRepository.findById(story_id);
        if (existingStory.isEmpty()) {
            throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));
        }
        Story updatedStory = new Story(existingStory.get().id(),
                existingStory.get().number(),
                request.story_points(),
                request.story_text(),
                existingStory.get().board_id(),
                existingStory.get().author_id());

        if(!storyRepository.updateStory(updatedStory))
        {
            throw new ValidationException("Story could not be updated");
        }
        return updatedStory;
    }

    @Transactional
    public boolean deleteStory(UUID id) {
        Optional<Story> maybeStory = storyRepository.findById(id);
        if (maybeStory.isEmpty()) {throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));}
        if(!storyRepository.deleteStory(id))
            {
            throw new ValidationException("Story could not be deleted");
            }
        return true;
    }


}
