package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.InvestResults;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InvestResultsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public InvestResultsRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<InvestResults> findLastById(UUID story_id)
    {
        String sql = """
                SELECT * FROM investresults
                WHERE stories_id = :story_id
                ORDER BY checked_at
                LIMIT 1
                """;

        Optional<InvestResults> lastInvestResult = namedParameterJdbcTemplate.query(sql, Map.of("story_id", story_id), INVEST_RESULTS_ROW_MAPPER).stream().findFirst();
        return lastInvestResult;
    }

    public List<InvestResults> findAllById(UUID story_id, int page, int pageSize)
    {
        int offset = page * pageSize;
        String sql = """
                SELECT * FROM investresults
                WHERE stories_id = :story_id
                ORDER BY checked_at
                LIMIT :limit
                OFFSET :offset
                """;
        List<InvestResults> investResults = namedParameterJdbcTemplate.query(sql, Map.of(
                "story_id", story_id,
                "limit", pageSize,
                "offset", offset
        ), INVEST_RESULTS_ROW_MAPPER).stream().toList();
        return investResults;
    }

    public boolean createInvestResults(InvestResults investResults) {
        String sql = """
            INSERT INTO investresults (stories_id, checked_at, independent_score, negotiable_score,
                                        valuable_score, estimable_score, small_score, testable_score,
                                        issues, suggestions)
            VALUES (:story_id, :checked_at, :independent_score, :negotiable_score,
                    :valuable_score, :estimable_score, :small_score, :testable_score,
                    :issues, :suggestions)
            """;

        Map<String, Object> params = Map.of(
                "story_id", investResults.story_id(),
                "checked_at", investResults.checked_at(),
                "independent_score", investResults.independent_score(),
                "negotiable_score", investResults.negotiable_score(),
                "valuable_score", investResults.valuable_score(),
                "estimable_score", investResults.estimable_score(),
                "small_score", investResults.small_score(),
                "testable_score", investResults.testable_score(),
                "issues", investResults.issues(),
                "suggestions", investResults.suggestions()
        );

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected ==1;
    }

    public boolean deleteAllById(UUID story_id)
    {
        String sql = "DELETE FROM investresults WHERE stories_id = :story_id";
        Map<String, Object> params = Map.of("story_id", story_id);
        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected == 1;
    }

    public boolean deleteOneById(UUID story_id, Timestamp checked_at)
    {
        String sql = "DELETE FROM investresults WHERE stories_id = :story_id AND checked_at = :checked_at";
        Map<String, Object> params = Map.of("story_id", story_id, "checked_at", checked_at);
        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected == 1;
    }

    public long countAllById(UUID story_id) {

        String sql = """
        SELECT COUNT(*)
        FROM investresults
        WHERE stories_id = :story_id
        """;
        return namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of("story_id", story_id),
                Long.class
        );
    }

    private static final RowMapper<InvestResults> INVEST_RESULTS_ROW_MAPPER = (rs, rowNum) -> new InvestResults(
            rs.getObject("stories_id", UUID.class),
            rs.getTimestamp("checked_at"),
            rs.getInt("independent_score"),
            rs.getInt("negotiable_score"),
            rs.getInt("valuable_score"),
            rs.getInt("estimable_score"),
            rs.getInt("small_score"),
            rs.getInt("testable_score"),
            rs.getString("issues"),
            rs.getString("suggestions")
    );
}
