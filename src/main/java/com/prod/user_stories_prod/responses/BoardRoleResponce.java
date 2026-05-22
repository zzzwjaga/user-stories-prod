package com.prod.user_stories_prod.responses;

import com.prod.user_stories_prod.entities.Role;

import java.io.Serializable;
import java.util.UUID;

public record BoardRoleResponce(UUID board_id, Role role) {
}
