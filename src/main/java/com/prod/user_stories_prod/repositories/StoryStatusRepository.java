package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.Status;
import com.prod.user_stories_prod.entities.StoryStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class StoryStatusRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public StoryStatusRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.namedParameterJdbcTemplate = jdbcTemplate;
    }

    public boolean insertStatusRecord(StoryStatus storyStatus) {

        String sql = """
                INSERT INTO stories_statuses
                (id, story_id, status, changed_at)
                VALUES (:id, :story_id, :status, CURRENT_TIMESTAMP)
                """;

        Map<String, Object> params = Map.of(
                "id", storyStatus.id(),
                "story_id", storyStatus.story_id(),
                "status", storyStatus.status()
        );

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected > 0;
    }

    public List<StoryStatus> findAllById(UUID story_id, int page, int pageSize)
    {
        int offset = page * pageSize;
        String sql = """
                SELECT *
                FROM stories_statuses
                WHERE story_id = :story_id
                ORDER BY changed_at
                LIMIT :limit
                OFFSET :offset
                """;

        return namedParameterJdbcTemplate.query(sql,Map.of(
                "story_id", story_id,
                "limit", pageSize,
                "offset", offset
        ), STORY_STATUS_ROW_MAPPER);
    }

    public Optional<StoryStatus> findLatestById(UUID story_id)
    {
        String  sql = """
                SELECT *
                FROM stories_statuses
                WHERE story_id = :story_id
                ORDER BY changed_at
                LIMIT 1
        """;

        return namedParameterJdbcTemplate.query(sql, Map.of("story_id", story_id), STORY_STATUS_ROW_MAPPER).stream().findFirst();
    }

    public Optional<StoryStatus> findById(UUID id)
    {
        String  sql = """
                SELECT *
                FROM stories_statuses
                WHERE id = :id
        """;

        return namedParameterJdbcTemplate.query(sql, Map.of("id", id), STORY_STATUS_ROW_MAPPER).stream().findFirst();
    }

    public long countAllById(UUID story_id) {

        String sql = """
        SELECT COUNT(*)
        FROM stories_statuses
        WHERE story_id = :story_id
        
        """;
        return namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of(),
                Long.class
        );
    }

    public boolean deleteRecordById(UUID id)
    {
        String sql = """
                DELETE FROM stories_statuses
                WHERE id=: id
        """;

        int rowsAffected = namedParameterJdbcTemplate.update(sql, Map.of("id", id));
        return rowsAffected > 0;
    }

    public boolean deleteById(UUID story_id)
    {
        String sql = """
                DELETE FROM stories_statuses
                WHERE story_id = :story_id
        """;
        return namedParameterJdbcTemplate.update(sql, Map.of("story_id", story_id)) > 0;
    }

    private static final RowMapper<StoryStatus> STORY_STATUS_ROW_MAPPER = (rs, rowNum) ->
            new StoryStatus(
                    rs.getObject("id", UUID.class),
                    rs.getObject("story_id", UUID.class),
                    rs.getObject("status", Status.class),
                    rs.getTimestamp("changed_at")
            );






}
