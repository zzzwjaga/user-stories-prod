package com.prod.user_stories_prod.requests;

import java.util.UUID;

public record CreateStoryRequest(
        String story_text,
        int story_points,
        UUID author_id ) {
}


