package com.prod.user_stories_prod.entities;

import java.sql.Timestamp;
import java.util.UUID;

public record Story(UUID id,
                    String number,
                    String story_text,
                    int story_points,
                    UUID board_id,
                    UUID author_id,
                    Timestamp created_at,
                    Timestamp updated_at ) {
    public Story(UUID id, String number, int story_points, String story_text, UUID board_id, UUID author_id) {
        this(id, number, story_text, story_points, board_id, author_id, null, null);
    }
}

