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
}

