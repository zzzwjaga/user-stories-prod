package com.prod.user_stories_prod.entities;

import java.sql.Timestamp;
import java.util.UUID;

public record StoryStatus(
        UUID id,
        UUID story_id,
        Status status,
        Timestamp changed_at
) {

    public StoryStatus(UUID id, UUID story_id, Status status) {
        this(id, story_id, status, null);
    }
}
