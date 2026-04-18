package requests;

import java.util.UUID;

public record CreateStoryRequest(
        String number,
        String story_text,
        int story_points,
        UUID board_id,
        UUID author_id ) {
}


