package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.Role;
import com.prod.user_stories_prod.entities.UserBoard;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserBoardRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserBoardRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public boolean createUserRecord(UserBoard userBoard)
    {
        String sql = """
                INSERT INTO user_boards(user_id,board_id, role)
                VALUES (:user_id,:board_id,:role)
                """;

        Map<String,Object> params = Map.of(
                "user_id", userBoard.user_id(),
                "board_id", userBoard.board_id(),
                "role", userBoard.role().name()
        );

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected > 0;
    }

    public List<UserBoard> findAllByUserId(UUID user_id, int page, int pageSize)
    {
        int offset = page * pageSize;
        String sql = """
                SELECT *  FROM user_boards
                WHERE user_id = :user_id
                LIMIT :limit
                OFFSET :offset
                """;

        return namedParameterJdbcTemplate.query(sql,Map.of(
                "user_id", user_id,
                "limit", pageSize,
                "offset", offset
        ), USER_BOARD_ROW_MAPPER);
    }

    public long countAllByUserId(UUID user_id) {

        String sql = """
        SELECT COUNT(*)
        FROM user_boards
        WHERE user_id = :user_id
        """;
        return namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of("user_id", user_id),
                Long.class
        );
    }

    public List<UserBoard> findAllByBoardId(UUID board_id, int page, int pageSize)
    {
        int offset = page * pageSize;
        String sql = """
                SELECT *  FROM user_boards
                WHERE board_id = :board_id
                LIMIT :limit
                OFFSET :offset
        """;
        return namedParameterJdbcTemplate.query(sql,Map.of(
                "board_id", board_id,
                "limit", pageSize,
                "offset", offset
        ), USER_BOARD_ROW_MAPPER);
    }

    public long countAllByBoardId(UUID board_id) {

        String sql = """
        SELECT COUNT(*)
        FROM user_boards
        WHERE board_id = :board_id
        """;
        return namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of("board_id", board_id),
                Long.class
        );
    }

    public Optional<UserBoard> findByKey(UUID board_id, UUID user_id)
    {
        String sql = """
                SELECT * FROM user_boards
                WHERE user_id = :user_id
                AND board_id = :board_id
        """;

        return namedParameterJdbcTemplate.query(sql,Map.of("user_id", user_id, "board_id", board_id), USER_BOARD_ROW_MAPPER).stream().findFirst();
    }

    public boolean deleteByKey(UUID board_id, UUID user_id)
    {
        String sql = """
                DELETE FROM user_boards
                WHERE user_id = :user_id
                AND board_id = :board_id
        """;

        return namedParameterJdbcTemplate.update(sql,Map.of("user_id", user_id, "board_id", board_id)) > 0;
    }

    public boolean deleteByBoardId(UUID board_id)
    {
        String sql = """
                DELETE FROM user_boards
                WHERE board_id = :board_id
        """;

        return namedParameterJdbcTemplate.update(sql,Map.of("board_id", board_id)) > 0;
    }

    public boolean deleteByUserId(UUID user_id)
    {
        String sql = """
                DELETE FROM user_boards
                WHERE user_id = :user_id
        """;

        return namedParameterJdbcTemplate.update(sql,Map.of("user_id", user_id)) > 0;
    }

    public boolean updateRole(UserBoard userBoard)
    {
        String sql = """
                UPDATE user_boards
                SET role = :role
                WHERE board_id = :board_id
                AND user_id = :user_id
                """;

        int rowsAffected = namedParameterJdbcTemplate.update(sql, Map.of(
                "user_id", userBoard.user_id(),
                "board_id", userBoard.board_id(),
                "role", userBoard.role().name()
        ));

        return rowsAffected == 1;
    }

    public Optional<String> findRoleByUserEmailAndBoardId(String userEmail, UUID boardId) {
        String sql = """
        SELECT ubr.role 
        FROM user_boards ubr
        JOIN users u ON u.id = ubr.user_id
        WHERE u.email = :email AND ubr.board_id = :board_id
        """;
        Map<String, Object> params = Map.of("email", userEmail, "board_id", boardId);
        try {
            String role = namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }



    private static final RowMapper<UserBoard> USER_BOARD_ROW_MAPPER = (rs,rowNum)->
            new UserBoard(
                    rs.getObject("user_id", UUID.class),
                    rs.getObject("board_id", UUID.class),
                    Role.valueOf(rs.getString("role"))
            );
}
