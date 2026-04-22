package requests;

import java.util.UUID;

public record FindStoryByNumberRequest(UUID board_id, String number) {
}
