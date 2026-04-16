package com.prod.user_stories_prod.entities;

import java.sql.Timestamp;
import java.util.UUID;

public record InvestResults(UUID story_id,
                            Timestamp checked_at,
                            int independent_score,
                            int negotiable_score,
                            int valuable_score,
                            int estimable_score,
                            int small_score,
                            int testable_score,
                            String issues,
                            String suggestions) {
}
