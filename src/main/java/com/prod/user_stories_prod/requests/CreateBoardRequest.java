package com.prod.user_stories_prod.requests;

import java.util.UUID;

public record CreateBoardRequest(UUID owner_id,String boardname, String description) {
}
