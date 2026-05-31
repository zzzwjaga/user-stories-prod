package com.prod.user_stories_prod.entities;

import java.util.Map;
import java.util.UUID;

public record Report(
        UUID board_id,
        String boardname,

        int totalStories,

        Map<Status, Double> statusDistribution,

        double avgComprtetitionHours,

        double avgInvestScore,
        double avgIndependentScore,
        double avgNegoitableScore,
        double avgValuableScore,
        double avgEstimableScore,
        double avgSmallScore,
        double avgTestableScore,

        int completedStories,
        int storiesWithoutInvest,

        double avgStoryPoints
) {

}
