package com.prod.user_stories_prod.entities;

import java.sql.Timestamp;
import java.util.UUID;

public record User(UUID id, String username, String email, String password_hash, Timestamp created_at, Timestamp updated_at) {
}
