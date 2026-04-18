package responses;

import java.util.UUID;

public record CreateStoryResponse(
        UUID id,
        String story_text,
        UUID board_id,
        String boardname,    // ← для отображения
        UUID author_id,
        String username    // ← для отображения
) {
}
