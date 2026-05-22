package com.prod.user_stories_prod.responses;

import com.prod.user_stories_prod.entities.Role;
import java.util.UUID;

public record UserRoleResponce(UUID user_id, Role role) {
}
