package services;

import com.prod.user_stories_prod.entities.Story;
import exseptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.StoryRepository;
import requests.CreateStoryRequest;
import requests.FindAllByBoardRequest;
import requests.FindStoryByNumberRequest;
import requests.UpdateStoryRequest;
import responses.ErrorCode;

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
    public Story createStory(CreateStoryRequest request) {
        storyRepository.lockOnValue(request.number());
        Optional<Story> maybeStory = storyRepository.findByNumber(request.board_id(), request.number());
        if (maybeStory.isPresent()) {
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

    @Transactional
    public Story findStoryByNumber(FindStoryByNumberRequest request) {
        Optional<Story> maybeStory = storyRepository.findByNumber(request.board_id(),request.number());
        if (maybeStory.isEmpty()) {throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));}
        return maybeStory.get();
    }

    @Transactional
    List<Story> findAllByBoard (FindAllByBoardRequest request) {
        return storyRepository.findAllByBoard(request.board_id());
    }

    @Transactional
    public Story updateStory(UpdateStoryRequest request) {
        Optional<Story> existingStory = storyRepository.findById(request.id();
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
