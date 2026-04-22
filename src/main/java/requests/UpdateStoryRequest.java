package requests;

import java.util.UUID;

public record UpdateStoryRequest(
        UUID id,
        String story_text,
        int story_points
) {
}
