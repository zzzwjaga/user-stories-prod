package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.Status;
import com.prod.user_stories_prod.entities.Story;
import com.prod.user_stories_prod.entities.StoryStatus;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.BoardRepository;
import com.prod.user_stories_prod.repositories.StoryStatusRepository;
import com.prod.user_stories_prod.responses.PageResponce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log =
            LoggerFactory.getLogger(StoryService.class);

    public StoryService(StoryRepository storyRepository, BoardRepository boardRepository, StoryStatusRepository storyStatusRepository) {
        this.storyRepository = storyRepository;
        this.boardRepository = boardRepository;
        this.storyStatusRepository = storyStatusRepository;
    }

    @Transactional
    public Story createStory(UUID board_id, CreateStoryRequest request) {
        if(boardRepository.findById(board_id).isEmpty()) {
            log.warn("Board not found boardId={}", board_id);
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }
        Long sequence =
                boardRepository.getNextStoryNumber(board_id);

        String storyNumber = "US-" + sequence;
        log.info("Generated story number={} boardId={}", storyNumber, board_id);
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
            log.error("Failed to create story boardId={}", board_id);
            throw new ValidationException("Story could not be created");
        }
        if(!storyStatusRepository.insertStatusRecord(new StoryStatus(UUID.randomUUID(), newStory.id(), NEW))){
            log.error("Failed to insert initial status storyId={}", newStory.id());
            throw new ValidationException("Failed to insert StoryStatus record");
        }
        log.info("Story fully initialized storyId={} status=NEW", newStory.id());
        return newStory;
    }

    @Transactional
    public Optional<Story> findStoryByNumber(UUID board_id, String number) {
        if(boardRepository.findById(board_id).isEmpty()) {
            log.warn("Board not found boardId={}", board_id);
            throw new ValidationException(ErrorCode.BOARD_NOT_FOUND);
        }
        Optional<Story> maybeStory = storyRepository.findByNumber(board_id, number);
        if (maybeStory.isEmpty()) {
            log.warn("Story not found boardId={}, number={}", board_id, number);
            throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));}
        return maybeStory;
    }

    @Transactional
    public Optional<Story> findStoryById(UUID story_id) {

        Optional<Story> maybeStory = storyRepository.findById(story_id);
        if (maybeStory.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }
        return maybeStory;
    }

    @Transactional
    public PageResponce<Story> findAllByBoard(
            UUID board_id,
            int page,
            int size
    )
    {
        List<Story> stories =
                storyRepository.findAllByBoard(board_id, page, size);

        long total =
                storyRepository.countByBoard(board_id);

        int totalPages =
                (int) Math.ceil((double) total / size);
        log.info("Fetched stories boardId={} count={}",
                board_id, stories.size());
        return new PageResponce<>(
                stories,
                page,
                size,
                total,
                totalPages
        );
    }

    @Transactional
    public Story updateStory(UUID story_id, UpdateStoryRequest request) {
        Optional<Story> existingStory = storyRepository.findById(story_id);
        if (existingStory.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
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
            log.error("Failed to update story storyId={}", story_id);
            throw new ValidationException("Story could not be updated");
        }
        log.info("Story updated storyId={}", story_id);
        return updatedStory;
    }

    @Transactional
    public StoryStatus findLatestStatus(UUID story_id)
    {
        Optional<Story> maybeStory = storyRepository.findById(story_id);
        if (maybeStory.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
            throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));
        }
        Optional<StoryStatus> latestStatus= storyStatusRepository.findLatestById(story_id);
        if(latestStatus.isEmpty())
        {
            log.warn("No status found storyId={}", story_id);
            throw new ValidationException(String.valueOf(ErrorCode.STATUS_RECORD_NOT_FOUND));
        }
        log.info("Latest status fetched storyId={}",
                story_id);
        return latestStatus.get();
    }

    @Transactional
    public PageResponce<StoryStatus> findAllStatuses(UUID story_id, int page, int pageSize)
    {
        Optional<Story> maybeStory = storyRepository.findById(story_id);
        if (maybeStory.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
            throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));
        }
        List<StoryStatus> storyStatuses = storyStatusRepository.findAllById(story_id,page, pageSize);
        long total = storyStatusRepository.countAllById(story_id);
        int totalPages = (int)Math.ceil((double) total / pageSize);
        log.info("Fetched statuses storyId={} count={}",
                story_id, storyStatuses.size());
        return new PageResponce<>(
                storyStatuses,
                page,
                pageSize,
                total,
                totalPages

        );
    }

    @Transactional
    public void changeStatus(UUID story_id, Status newStatus)
    {
        Optional<Story> story = storyRepository.findById(story_id);

        if (story.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
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
            log.error("Failed to insert status storyId={} status={}",
                    story_id, newStatus);
            throw new ValidationException("Status could not be updated");
        }
        log.info("Status changed storyId={} newStatus={}",
                story_id, newStatus);
    }

    @Transactional
    public void deleteStory(UUID id) {
        Optional<Story> maybeStory = storyRepository.findById(id);
        if (maybeStory.isEmpty()) {
            log.warn("Story not found storyId={}", id);
            throw new ValidationException(String.valueOf(ErrorCode.STORY_NOT_FOUND));}
        if(!storyRepository.deleteStory(id))
        {
            log.error("Failed to delete story storyId={}", id);
            throw new ValidationException("Story could not be deleted");
        }
    }


}
