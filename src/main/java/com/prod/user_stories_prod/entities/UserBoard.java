package com.prod.user_stories_prod.entities;

import java.util.UUID;

public record UserBoard(
        UUID user_id,
        UUID board_id,
        Role role
) {
    public Object getRole() {
        return role;
    }
}
