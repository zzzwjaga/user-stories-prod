package services;

import com.prod.user_stories_prod.entities.Story;
import exseptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.StoryRepository;
import requests.CreateStoryRequest;
import responses.ErrorCode;

import java.util.Optional;
import java.util.UUID;

@Service
public class StoryService {

    private final StoryRepository storyRepository;

    public StoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    @Transactional
    public Story createStory(CreateStoryRequest request) {
        storyRepository.lockOnValue(request.number());
        Optional<Story> maybeProfile = storyRepository.findByNumber(request.board_id(), request.number());
        if (maybeProfile.isPresent()) {
            throw new ValidationException(String.valueOf(ErrorCode.STORY_ALREADY_EXISTS));
        }
        Story newStory = new Story(
                UUID.randomUUID(),
                request.number(),
                request.story_points(),
                request.story_text(),
                request.board_id(),
                request.author_id()
        );
        if(!storyRepository.createStory(newStory))
        {
            throw new ValidationException("Story could not be created");
        }
        return newStory;
    }
}
