package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.Role;
import com.prod.user_stories_prod.entities.UserBoard;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
                "role", userBoard.role()
        );

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected > 0;
    }

    private static final RowMapper<UserBoard> USER_BOARD_ROW_MAPPER = (rs,rowNum)->
            new UserBoard(
                    rs.getObject("user_id", UUID.class),
                    rs.getObject("board_id", UUID.class),
                    rs.getObject("role", Role.class)
            );
}
