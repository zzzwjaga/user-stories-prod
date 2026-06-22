package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.InvestResults;
import com.prod.user_stories_prod.entities.Story;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.InvestResultsRepository;
import com.prod.user_stories_prod.repositories.StoryRepository;
import com.prod.user_stories_prod.responses.ErrorCode;
import com.prod.user_stories_prod.responses.InvestCheckResponce;
import com.prod.user_stories_prod.responses.PageResponce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvestResultsService {

    private final InvestResultsRepository investResultsRepository;
    private final StoryRepository storyRepository;
    private final AIService aiService;
    private static final Logger log =
            LoggerFactory.getLogger(InvestResultsService.class);

    public InvestResultsService(InvestResultsRepository investResultsRepository, StoryRepository storyRepository, AIService aiService) {
        this.investResultsRepository = investResultsRepository;
        this.storyRepository = storyRepository;
        this.aiService = aiService;
    }

    @Transactional
    public InvestResults checkInvestResults(UUID story_id) {

        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }
        log.info("Calling AI storyId={}", story_id);
        InvestCheckResponce aiResponse = aiService.investCheck(story.get().story_text());
        log.info("AI response received storyId={}", story_id);
        InvestResults results = new InvestResults(
                story_id,
                Timestamp.valueOf(LocalDateTime.now()),
                aiResponse.independentScore(),
                aiResponse.negotiableScore(),
                aiResponse.valuableScore(),
                aiResponse.estimableScore(),
                aiResponse.smallScore(),
                aiResponse.testableScore(),
                aiResponse.issues(),
                aiResponse.suggestions()
        );

        if(!investResultsRepository.createInvestResults(results))
        {
            log.error("Failed to save invest results storyId={}", story_id);
            throw new ValidationException("Invest results error");
        }
        return results;
    }

    @Transactional
    public Optional<InvestResults> findLastInvestResultsByStoryId(UUID story_id) {
        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }

        Optional<InvestResults> investResults = investResultsRepository.findLastById(story_id);
        if (investResults.isEmpty()) {
            log.info("No INVEST results found storyId={}", story_id);
            return Optional.empty();  // Возвращаем empty вместо исключения
        }

        log.info("Last INVEST result fetched storyId={}", story_id);
        return investResults;
    }


    @Transactional
    public PageResponce<InvestResults> findInvestResultsByStoryId(UUID story_id, int page, int pageSize) {
        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }
        List<InvestResults> results = investResultsRepository.findAllById(story_id, page, pageSize);
        long total = investResultsRepository.countAllById(story_id);
        int totalPages = (int) Math.ceil((double) total/pageSize);
        log.info("Fetched INVEST results storyId={} count={}",
                story_id, results.size());
        return new PageResponce<>(
                results,
                page,
                pageSize,
                total,
                totalPages
        );
    }

    @Transactional
    public void DeleteInvestResultsByStoryId(UUID story_id) {
        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }
        if(!investResultsRepository.deleteAllById(story_id))
        {
            log.error("Failed to delete INVEST results storyId={}", story_id);
            throw new ValidationException("Invest results delete error");
        }
    }

    @Transactional
    public void deleteOneInvestResultsByStoryId(UUID story_id, Timestamp checked_at) {
        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            log.warn("Story not found storyId={}", story_id);
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }
        if(!investResultsRepository.deleteOneById(story_id,checked_at))
            {
                log.error("Failed to delete INVEST result storyId={} checkedAt={}",
                        story_id, checked_at);
                throw new ValidationException("Invest results delete error");
            }
    }


}
