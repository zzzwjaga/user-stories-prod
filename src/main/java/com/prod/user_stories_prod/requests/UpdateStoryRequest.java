package com.prod.user_stories_prod.requests;

import java.util.UUID;

public record UpdateStoryRequest(
        String story_text,
        int story_points
) {
}
