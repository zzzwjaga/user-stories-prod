package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.Status;
import com.prod.user_stories_prod.entities.Story;
import com.prod.user_stories_prod.entities.StoryStatus;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.BoardRepository;
import com.prod.user_stories_prod.repositories.StoryStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.prod.user_stories_prod.repositories.StoryRepository;
import com.prod.user_stories_prod.requests.CreateStoryRequest;
import com.prod.user_stories_prod.requests.UpdateStoryRequest;
import com.prod.user_stories_prod.responses.ErrorCode;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.prod.user_stories_prod.entities.Status.NEW;

@Service
public class StoryService {

    private final StoryRepository storyRepository;
    private final BoardRepository boardRepository;
    private final StoryStatusRepository storyStatusRepository;

    public StoryService(StoryRepository storyRepository, BoardRepository boardRepository, StoryStatusRepository storyStatusRepository) {
        this.storyRepository = storyRepository;
        this.boardRepository = boardRepository;
        this.storyStatusRepository = storyStatusRepository;
    }

    @Transactional
    public Story createStory(UUID board_id, CreateStoryRequest request) {
        Long sequence =
                boardRepository.getNextStoryNumber(board_id);

        String storyNumber = "US-" + sequence;
        Story newStory = new Story(
                UUID.randomUUID(),
                storyNumber,
                request.story_points(),
                request.story_text(),
                board_id,
                request.author_id()
        );
        if(!storyRepository.createStory(newStory))
        {
            throw new ValidationException("Story could not be created");
        }
        if(!storyStatusRepository.insertStatusRecord(new StoryStatus(UUID.randomUUID(), newStory.id(), NEW))){
            throw new ValidationException("Failed to insert StoryStatus record");
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
    public StoryStatus findLatestStatus(UUID story_id)
    {
        Optional<Story> maybeStory = storyRepository.findById(story_id);
        if (maybeStory.isEmpty()) {
            throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));
        }
        Optional<StoryStatus> latestStatus= storyStatusRepository.findLatestById(story_id);
        if(latestStatus.isEmpty())
        {
            throw new ValidationException(String.valueOf(ErrorCode.STATUS_RECORD_NOT_FOUND));
        }
        return latestStatus.get();
    }

    @Transactional
    public List<StoryStatus> findAllStatuses(UUID story_id)
    {
        Optional<Story> maybeStory = storyRepository.findById(story_id);
        if (maybeStory.isEmpty()) {
            throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));
        }
        List<StoryStatus> storyStatuses = storyStatusRepository.findAllById(story_id);
        return storyStatuses;
    }

    @Transactional
    public void changeStatus(UUID story_id, Status newStatus)
    {
        Optional<Story> story = storyRepository.findById(story_id);

        if (story.isEmpty()) {
            throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));
        }

        boolean inserted = storyStatusRepository.insertStatusRecord(
                new StoryStatus(
                        UUID.randomUUID(),
                        story_id,
                        newStatus
                )
        );
        if (!inserted) {
            throw new ValidationException("Status could not be updated");
        }
    }



    @Transactional
    public void deleteStory(UUID id) {
        Optional<Story> maybeStory = storyRepository.findById(id);
        if (maybeStory.isEmpty()) {throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));}
        if(!storyRepository.deleteStory(id))
        {
            throw new ValidationException("Story could not be deleted");
        }
    }


}
