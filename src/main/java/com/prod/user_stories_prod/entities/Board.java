package com.prod.user_stories_prod.entities;

import java.sql.Timestamp;
import java.util.UUID;

public record Board(UUID id, UUID owner_id, String boardname, String description, Timestamp created_at, Timestamp updated_at) {
}
