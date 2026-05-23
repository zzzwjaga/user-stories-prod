package com.prod.user_stories_prod.services;

import com.prod.user_stories_prod.entities.InvestResults;
import com.prod.user_stories_prod.entities.Story;
import com.prod.user_stories_prod.exseptions.ValidationException;
import com.prod.user_stories_prod.repositories.InvestResultsRepository;
import com.prod.user_stories_prod.repositories.StoryRepository;
import com.prod.user_stories_prod.responses.ErrorCode;
import com.prod.user_stories_prod.responses.InvestCheckResponce;
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

    public InvestResultsService(InvestResultsRepository investResultsRepository, StoryRepository storyRepository, AIService aiService) {
        this.investResultsRepository = investResultsRepository;
        this.storyRepository = storyRepository;
        this.aiService = aiService;
    }

    @Transactional
    public InvestResults checkInvestResults(UUID story_id) {

        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }

        InvestCheckResponce aiResponse = aiService.investCheck(story.get().story_text());

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
            throw new ValidationException("Invest results error");
        }
        return results;
    }

    @Transactional
    public InvestResults findLastInvestResultsByStoryId(UUID story_id) {
        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }

        Optional<InvestResults> investResults = investResultsRepository.findLastById(story_id);
        if (investResults.isEmpty()) {
            throw new ValidationException(ErrorCode.INVEST_RESULT_NOT_FOUND);
        }
        return investResults.get();
    }

    @Transactional
    public List<InvestResults> findInvestResultsByStoryId(UUID story_id) {
        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }
        List<InvestResults> results = investResultsRepository.findAllById(story_id);
        return results;
    }

    @Transactional
    public void DeleteInvestResultsByStoryId(UUID story_id) {
        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }
        if(!investResultsRepository.deleteAllById(story_id))
        {
            throw new ValidationException("Invest results delete error");
        }
    }

    @Transactional
    public void deleteOneInvestResultsByStoryId(UUID story_id, Timestamp checked_at) {
        Optional<Story> story = storyRepository.findById(story_id);
        if (story.isEmpty()) {
            throw new ValidationException(ErrorCode.STORY_NOT_FOUND);
        }
        if(!investResultsRepository.deleteOneById(story_id,checked_at))
            {
            throw new ValidationException("Invest results delete error");
            }
    }


}
