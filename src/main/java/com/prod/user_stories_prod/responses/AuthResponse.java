package com.prod.user_stories_prod.responses;

import java.util.UUID;

public record AuthResponse(UUID id, String username, String email) {
}
