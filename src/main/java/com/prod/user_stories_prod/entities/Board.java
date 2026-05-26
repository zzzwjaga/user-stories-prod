package com.prod.user_stories_prod.entities;

import java.sql.Timestamp;
import java.util.UUID;

public record Board(UUID id, UUID owner_id, String boardname, String description, Timestamp created_at, Timestamp updated_at, Long version) {

    public Board(UUID owner_id, UUID id, String boardname, String description) {
        this(id, owner_id, boardname, description, null, null, null);
    }
}
