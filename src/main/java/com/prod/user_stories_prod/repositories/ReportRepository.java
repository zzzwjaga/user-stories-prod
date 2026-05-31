package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.Status;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class ReportRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public ReportRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int getTotalCount(UUID board_id){

        String sql = """
                SELECT COUNT(*)
                FROM stories
                WHERE board_id = :board_id
                """;

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, Map.of("board_id", board_id), Integer.class);
        return count != null ? count : 0;
    }

    public int getCompletedStoriesCount(UUID board_id){
        String sql = """
                SELECT COUNT(*)
                FROM stories s
                JOIN stories_statuses st
                ON s.id = st.story_id
                WHERE s.board_id = :board_id AND st.status = 'COMPLETED'
        """;

        Integer count =  namedParameterJdbcTemplate.queryForObject(sql, Map.of("board_id", board_id), Integer.class);
        return count != null ? count : 0;
    }

    public int getStoriesWithoutInvest(UUID board_id){
        String sql = """
                SELECT COUNT(*)
                FROM stories s
                WHERE board_id = :board_id
                AND NOT EXISTS (
                SELECT 1 FROM investresults i
                WHERE i.stories_id = s.id)
        """;

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, Map.of("board_id", board_id), Integer.class);
        return count != null ? count : 0;
    }

    public Double avgCompetitionHours(UUID board_id){

        String sql = """
                SELECT AVG(EXTRACT(EPOCH FROM(com_st.changed_at - new_st.changed_at)))
                FROM stories_statuses new_st
                JOIN stories_statuses com_st
                ON new_st.story_id = com_st.story_id
                JOIN stories s ON s.id = new_st.story_id
                WHERE com_st.status = 'COMPLETED'
                AND new_st.status = 'NEW'
                AND s.board_id = :board_id
                """;
        Double avg = namedParameterJdbcTemplate.queryForObject(sql,Map.of("board_id", board_id), double.class);
        return avg != null ? avg : 0.0;
    }

    public Double getAvgInvest(UUID board_id){
        String sql = """
                SELECT AVG(i.independent_score, i.negotiable_score, i.valuable_score, i.estimable_score, i.small_score, i.testable_score)
                FROM investresults i
                JOIN stories s ON s.id = i.stories_id
                WHERE s.board_id = :board_id
                """;

        Double avg =  namedParameterJdbcTemplate.queryForObject(sql,Map.of("board_id", board_id), Double.class);
        return avg != null ? avg : 0.0;
    }

    public Map<String, Double> getAvgInvestScores(UUID board_id){

        String sql = """
                SELECT AVG(i.independent_score) avg_independent,
                       AVG(i.negotiable_score) avg_negotiable,
                       AVG(i.valuable_score)  avg_valuable,
                       AVG(i.estimable_score) avg_estimable,
                       AVG(i.small_score) avg_small,
                       AVG(i.testable_score) avg_testable
                FROM investresults i
                JOIN stories s ON s.id = i.stories_id
                WHERE s.board_id = :board_id
                """;

        return namedParameterJdbcTemplate.queryForObject(sql, Map.of("board_id", board_id), (rs, rowNum) -> Map.of(
                "independent", rs.getDouble("avg_independent"),
                "negotiable", rs.getDouble("avg_negotiable"),
                "valuable", rs.getDouble("avg_valuable"),
                "estimable", rs.getDouble("avg_estimable"),
                "small", rs.getDouble("avg_small"),
                "testable", rs.getDouble("avg_testable")
        ));
    }

    public Double getAvgStoryPoints(UUID board_id){
        String sql = """
                SELECT AVG(stories.story_points) avg_story_points
                FROM stories
                WHERE board_id = :board_id
        """;

        Double avg = namedParameterJdbcTemplate.queryForObject(sql, Map.of("board_id", board_id), Double.class);
        return avg != null ? avg : 0.0;
    }

    public Map<Status, Double> getStatusDisribution(UUID board_id){
        String sql = """
            SELECT st.status,
            COUNT(*) * 100.0 / (SELECT COUNT(*) FROM stories WHERE board_id = :board_id) as percentage
            FROM stories_statuses st
            JOIN stories s ON s.id = st.story_id
            WHERE s.board_id = :board_id
            GROUP BY st.status
        """;

        return namedParameterJdbcTemplate.query(sql, Map.of("board_id", board_id), rs-> {
            Map<Status, Double> distribution = new HashMap<>();
            while (rs.next()) {
                distribution.put(Status.valueOf(rs.getString("status")), rs.getDouble("percentage"));
            }
            return distribution;
        });
    }

}
