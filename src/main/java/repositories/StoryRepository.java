package repositories;

import com.prod.user_stories_prod.entities.Story;
import exseptions.ValidationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import responses.ErrorCode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StoryRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public StoryRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<Story> findByNumber(UUID board_id, String number) {
        String sql = "SELECT * FROM stories WHERE board_id = :board_id AND number = :number FOR UPDATE;";
        List<Story> stories = namedParameterJdbcTemplate.query(sql, Map.of("board_id", board_id, "number", number), STORY_ROW_MAPPER);
        if (stories.size() > 1) {
            throw new RuntimeException("More than one profile found for this board and number: " + board_id + number);
        }
        if (stories.isEmpty()) {
            return Optional.empty();
        }
        return stories.stream().findFirst();
    }

    public List<Story> findAllByBoard(UUID board_id) {
        String sql = "SELECT * FROM stories WHERE board_id = :board_id;";
        List<Story> stories = namedParameterJdbcTemplate.query(sql, Map.of("board_id", board_id), STORY_ROW_MAPPER);
        if (stories.isEmpty()) {
            return List.of();
        }
        return stories;
    }

    public Optional<Story> findById (UUID id)
    {
        String sql = "SELECT * FROM stories WHERE id = :id;";
        return namedParameterJdbcTemplate.query(sql, Map.of("id", id), STORY_ROW_MAPPER);
    }

    public boolean createStory(Story story) {
        String sql = """
        INSERT INTO stories (id, number, story_text, story_points, board_id, author_id, created_at, updated_at)
        VALUES (:id, :number, :story_text, :story_points, :board_id, :author_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;
        Map<String, Object> params = Map.of(
                "id", story.id(),
                "number", story.number(),
                "story_text", story.story_text(),
                "story_points", story.story_points(),
                "board_id", story.board_id(),
                "author_id", story.author_id()
        );
        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected == 1;
    }


    public boolean updateStory(Story story) {
        String sql = """
        UPDATE stories 
        SET 
            story_text = :story_text,
            story_points = :story_points,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :id
        """;
        int rowsAffected = namedParameterJdbcTemplate.update(sql, Map.of(
                "id", story.id(),
                "number", story.number(),
                "story_text", story.story_text(),
                "story_points", story.story_points(),
                "board_id", story.board_id(),
                "author_id", story.author_id()
        ));
        return rowsAffected > 0;
    }

    public boolean deleteStory(UUID id) {
        String sql = """
        DELETE FROM stories
        WHERE id = :id
        """;
        int rowsAffected = namedParameterJdbcTemplate.update(sql, Map.of(
                "id", id));
        return rowsAffected > 0;
    }

    public void lockOnValue(Object value){
        String sql = "SELECT pg_advisory_xact_lock(hashtext(:lock));";
        namedParameterJdbcTemplate.queryForObject(sql, Map.of("lock", value.toString()), Object.class);
    }

    private static final RowMapper<Story> STORY_ROW_MAPPER = (rs, rowNum) -> new Story(
            rs.getObject("id", UUID.class),
            rs.getString("number"),
            rs.getString("story_text"),
            rs.getInt("story_points"),
            rs.getObject("board_id", UUID.class),
            rs.getObject("author_id", UUID.class),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
    );
    }







